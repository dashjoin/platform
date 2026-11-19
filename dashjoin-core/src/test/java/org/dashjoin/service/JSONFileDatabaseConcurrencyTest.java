package org.dashjoin.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.dashjoin.model.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;

/**
 * Stress tests for concurrent access to JSONFileDatabase. Each test asserts the correct behavior,
 * so a failure means the race was observed.
 */
public class JSONFileDatabaseConcurrencyTest {

  static final int THREADS = 8;
  static final long DURATION_MS = 3000;

  /**
   * strict parser, so trailing garbage is detected. Note: JSONDatabase.objectMapper (and any default
   * ObjectMapper) stops after the first JSON value, so Dashjoin itself reads a torn file
   * "successfully" while git, editors and other tools see invalid JSON.
   */
  static final ObjectMapper strict =
      new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

  @TempDir
  Path tmp;

  JSONFileDatabase db;
  ExecutorService pool;
  final Table table = Table.ofName("stress");

  @BeforeEach
  void setup() {
    db = new JSONFileDatabase() {
      @Override
      protected File getFile(String path) {
        return tmp.resolve(path).toFile();
      }
    };
    pool = Executors.newFixedThreadPool(THREADS * 2);
  }

  @AfterEach
  void teardown() throws Exception {
    pool.shutdownNow();
    pool.awaitTermination(10, TimeUnit.SECONDS);
  }

  static Map<String, Object> record(String id, int writer, int size) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("ID", id);
    m.put("writer", writer);
    m.put("payload", "x".repeat(size));
    return m;
  }

  /**
   * Writers overwrite the same record with short and long payloads while readers parse the file.
   * Expect: every read sees a complete object, and the final file is valid JSON.
   */
  @Test
  public void concurrentWritesSameRecord() throws Exception {
    File file = tmp.resolve("model/stress/rec.json").toFile();
    db.create(table, record("rec", -1, 10));

    AtomicBoolean stop = new AtomicBoolean();
    AtomicInteger writes = new AtomicInteger();
    AtomicInteger reads = new AtomicInteger();
    ConcurrentLinkedQueue<String> readErrors = new ConcurrentLinkedQueue<>();
    ConcurrentLinkedQueue<String> writeErrors = new ConcurrentLinkedQueue<>();
    CountDownLatch done = new CountDownLatch(THREADS * 2);

    for (int t = 0; t < THREADS; t++) {
      final int writer = t;
      pool.submit(() -> {
        try {
          int i = 0;
          while (!stop.get()) {
            // alternate short and long content so a short write over a long one leaves a tail
            int size = (i++ % 2 == 0) ? 10 : 20000 + writer * 1000;
            db.create(table, record("rec", writer, size));
            writes.incrementAndGet();
          }
        } catch (Throwable e) {
          writeErrors.add(e.toString());
        } finally {
          done.countDown();
        }
      });
      pool.submit(() -> {
        try {
          while (!stop.get()) {
            try {
              Map<String, Object> m = db.read(table, Collections.singletonMap("ID", "rec"));
              if (m == null)
                readErrors.add("read returned null (file missing)");
              reads.incrementAndGet();
            } catch (Throwable e) {
              readErrors.add(e.getClass().getSimpleName());
            }
          }
        } finally {
          done.countDown();
        }
      });
    }

    Thread.sleep(DURATION_MS);
    stop.set(true);
    done.await(30, TimeUnit.SECONDS);

    String finalError = null;
    try {
      strict.readTree(Files.readString(file.toPath()));
    } catch (Exception e) {
      finalError = e.getMessage();
    }

    System.out.println("concurrentWritesSameRecord: writes=" + writes + " successfulReads=" + reads
        + " readErrors=" + readErrors.size() + " writeErrors=" + writeErrors.size()
        + " finalFileValid=" + (finalError == null));
    if (!readErrors.isEmpty())
      System.out.println("  sample read errors: " + sample(readErrors));
    if (!writeErrors.isEmpty())
      System.out.println("  sample write errors: " + sample(writeErrors));

    Assertions.assertTrue(writeErrors.isEmpty(), "write errors: " + sample(writeErrors));
    Assertions.assertTrue(readErrors.isEmpty(),
        readErrors.size() + " reads failed (" + reads + " succeeded): " + sample(readErrors));
    Assertions.assertNull(finalError, "final file is invalid JSON: " + finalError);
  }

  /**
   * Repeat many short bursts and check the file after each burst, since the final state of one long
   * run only gives a single sample of whether a torn write was left behind.
   */
  @Test
  public void fileValidAfterEachBurst() throws Exception {
    File file = tmp.resolve("model/stress/burst.json").toFile();
    // create the folder up front; concurrent first writes to a new table hit a separate mkdirs race
    // (see concurrentCreateNewTable)
    db.create(table, record("burst", -1, 5));
    int bursts = 500;
    int invalid = 0;
    String firstError = null;
    java.util.Random rnd = new java.util.Random(42);
    for (int b = 0; b < bursts; b++) {
      CountDownLatch start = new CountDownLatch(1);
      List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
      for (int t = 0; t < THREADS; t++) {
        // build the record before the start signal so all writers open the file at the same time;
        // random sizes from tiny to ~500 KB so a shorter write can land over a longer one
        Map<String, Object> rec = record("burst", t, rnd.nextBoolean() ? 5 : rnd.nextInt(500000));
        futures.add(pool.submit(() -> {
          start.await();
          db.create(table, rec);
          return null;
        }));
      }
      start.countDown();
      for (var f : futures)
        f.get();
      try {
        strict.readTree(Files.readString(file.toPath()));
      } catch (Exception e) {
        invalid++;
        if (firstError == null)
          firstError = e.getMessage();
      }
    }
    System.out.println("fileValidAfterEachBurst: invalid files=" + invalid + " of " + bursts);
    Assertions.assertEquals(0, invalid, "first error: " + firstError);
  }

  /**
   * Each thread updates its own field of the same record via update(search, delta). Expect: all
   * fields are present at the end.
   */
  @Test
  public void lostUpdates() throws Exception {
    Map<String, Object> init = new LinkedHashMap<>();
    init.put("ID", "lu");
    db.create(table, init);

    int perThread = 50;
    CountDownLatch start = new CountDownLatch(1);
    List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
    for (int t = 0; t < THREADS; t++) {
      final int writer = t;
      futures.add(pool.submit(() -> {
        start.await();
        for (int i = 0; i < perThread; i++) {
          Map<String, Object> delta = new LinkedHashMap<>();
          delta.put("f" + writer + "_" + i, i);
          // a torn file makes update() throw; retry so we measure lost updates, not parse errors
          for (int attempt = 0;; attempt++)
            try {
              db.update(table, Collections.singletonMap("ID", "lu"), delta);
              break;
            } catch (Exception e) {
              if (attempt > 100)
                throw e;
            }
        }
        return null;
      }));
    }
    start.countDown();
    for (var f : futures)
      f.get();

    Map<String, Object> res = db.read(table, Collections.singletonMap("ID", "lu"));
    int expected = THREADS * perThread;
    int actual = res.size() - 1; // minus ID
    System.out.println("lostUpdates: fields expected=" + expected + " present=" + actual);
    Assertions.assertEquals(expected, actual, (expected - actual) + " updates were lost");
  }

  /**
   * Same as lostUpdates, but through UnionDatabase, which is the path the config DB uses. Its
   * update() creates the record if missing, updates one key at a time and deletes records that only
   * contain the ID, so it needs the record lock across all of these steps.
   */
  @Test
  public void lostUpdatesThroughUnion() throws Exception {
    UnionDatabase union = new UnionDatabase();
    union._user = db;
    union._dbs = new ArrayList<>();

    int perThread = 50;
    CountDownLatch start = new CountDownLatch(1);
    ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
    List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
    for (int t = 0; t < THREADS; t++) {
      final int writer = t;
      futures.add(pool.submit(() -> {
        start.await();
        for (int i = 0; i < perThread; i++) {
          Map<String, Object> delta = new LinkedHashMap<>();
          delta.put("f" + writer + "_" + i, i);
          try {
            union.update(table, new LinkedHashMap<>(Map.of("ID", "union")), delta);
          } catch (Exception e) {
            errors.add(e.toString());
          }
        }
        return null;
      }));
    }
    start.countDown();
    for (var f : futures)
      f.get();

    Map<String, Object> res = db.read(table, Collections.singletonMap("ID", "union"));
    int expected = THREADS * perThread;
    int actual = res == null ? 0 : res.size() - 1;
    System.out.println("lostUpdatesThroughUnion: fields expected=" + expected + " present="
        + actual + " errors=" + errors.size());
    Assertions.assertTrue(errors.isEmpty(), "errors: " + sample(errors));
    Assertions.assertEquals(expected, actual, (expected - actual) + " updates were lost");
  }

  /**
   * The shared pretty printer keeps its indent level in a field. Serialize nested objects from
   * many threads and compare to the single-threaded output.
   */
  /**
   * Jackson only gives each generator its own pretty printer if the printer implements
   * Instantiatable. Otherwise all threads share one instance and its indent field.
   */
  @Test
  public void prettyPrinterNotShared() {
    SerializationConfig cfg = JSONDatabase.objectMapper.getSerializationConfig();
    Assertions.assertNotSame(cfg.constructDefaultPrettyPrinter(),
        cfg.constructDefaultPrettyPrinter(),
        "all threads share one PrettyPrinter instance with a mutable indent field");
  }

  @Test
  public void prettyPrinterThreadSafety() throws Exception {
    Map<String, Object> nested = new LinkedHashMap<>();
    Map<String, Object> cur = nested;
    for (int i = 0; i < 20; i++) {
      Map<String, Object> child = new LinkedHashMap<>();
      List<Object> list = new ArrayList<>();
      for (int j = 0; j < 20; j++)
        list.add(Map.of("a", List.of(j, Map.of("b", j))));
      cur.put("k" + i, list);
      cur.put("c" + i, child);
      cur = child;
    }
    String expected = JSONDatabase.toJsonString(nested);

    AtomicInteger mismatches = new AtomicInteger();
    AtomicInteger total = new AtomicInteger();
    CountDownLatch start = new CountDownLatch(1);
    List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
    for (int t = 0; t < THREADS; t++)
      futures.add(pool.submit(() -> {
        start.await();
        for (int i = 0; i < 5000; i++) {
          total.incrementAndGet();
          if (!expected.equals(JSONDatabase.toJsonString(nested)))
            mismatches.incrementAndGet();
        }
        return null;
      }));
    start.countDown();
    for (var f : futures)
      f.get();

    // after the run, a single-threaded call should still match (indent counter not drifted)
    String after = JSONDatabase.toJsonString(nested);
    System.out.println("prettyPrinterThreadSafety: mismatches=" + mismatches + " of " + total
        + " driftAfterRun=" + !expected.equals(after));
    Assertions.assertEquals(0, mismatches.get(), "wrongly indented outputs");
    Assertions.assertEquals(expected, after, "indent counter drifted permanently");
  }

  /**
   * Several threads create the first records of a table that has no folder yet. Expect: no errors.
   * create() checks exists() and then mkdirs(); a thread that loses the race gets false from
   * mkdirs() and throws "Error creating model folder".
   */
  @Test
  public void concurrentCreateNewTable() throws Exception {
    int failures = 0;
    String firstError = null;
    int rounds = 200;
    for (int r = 0; r < rounds; r++) {
      Table t = Table.ofName("newtable" + r);
      CountDownLatch start = new CountDownLatch(1);
      List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
      for (int i = 0; i < THREADS; i++) {
        final int id = i;
        futures.add(pool.submit(() -> {
          start.await();
          db.create(t, record("r" + id, id, 5));
          return null;
        }));
      }
      start.countDown();
      for (var f : futures)
        try {
          f.get();
        } catch (java.util.concurrent.ExecutionException e) {
          failures++;
          if (firstError == null)
            firstError = e.getCause().toString();
        }
    }
    System.out.println("concurrentCreateNewTable: failed creates=" + failures + " of "
        + (rounds * THREADS));
    Assertions.assertEquals(0, failures, "first error: " + firstError);
  }

  static String sample(ConcurrentLinkedQueue<String> q) {
    Map<String, Integer> counts = new LinkedHashMap<>();
    for (String s : q)
      counts.merge(s.length() > 150 ? s.substring(0, 150) : s, 1, Integer::sum);
    return counts.toString();
  }
}
