package org.dashjoin.function;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.dashjoin.util.FileSystem;
import org.dashjoin.util.Home;

/**
 * runs a script in the upload folder
 */
@SuppressWarnings("rawtypes")
public class Exec extends AbstractVarArgFunction<Exec.Result> {

  public class Result {
    public Object out;
    public String err;
    public int code;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Result run(List arg) throws Exception {
    String script = (String) arg.get(0);
    Object obj = arg.get(1);
    List<Object> args;
    if (obj instanceof List)
      args = (List<Object>) obj;
    else if (obj == null)
      args = Arrays.asList();
    else
      args = Arrays.asList(obj + "");

    if (script == null)
      throw new IllegalArgumentException("Syntax: $exec(executable, arguments)");

    // get executable location relative to home dir and make sure it is in app/bin
    File sc = Home.get().getFile(script);
    FileSystem.checkFileAccess(sc, "bin");

    // prepare exec
    CommandLine cmdLine = CommandLine.parse(sc.getAbsolutePath());
    for (Object a : args)
      cmdLine.addArgument(a + "");
    DefaultExecutor executor = new DefaultExecutor();
    OutputStream out = new ByteArrayOutputStream();
    OutputStream err = new ByteArrayOutputStream();
    executor.setStreamHandler(new PumpStreamHandler(out, err));

    // run and copy results into Result object
    Result res = new Result();
    res.code = executor.execute(cmdLine,
        Map.of("DASHJOIN_HOME", Home.get().getFile("").getAbsolutePath()));
    res.out = out.toString();
    res.err = err.toString();
    return res;
  }

  @Override
  public String getID() {
    return "exec";
  }

  @Override
  public String getType() {
    return "write";
  }

  @Override
  public List<Class> getArgumentClassList() {
    return Arrays.asList(String.class, Object.class);
  }
}
