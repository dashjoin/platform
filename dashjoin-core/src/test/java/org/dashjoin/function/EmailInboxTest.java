package org.dashjoin.function;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

@SuppressWarnings("unchecked")
public class EmailInboxTest {

  GreenMail greenMail;

  EmailInbox inbox;

  @BeforeEach
  public void before() throws Exception {
    greenMail = new GreenMail(new ServerSetup[] {new ServerSetup(0, "127.0.0.1", "smtp"),
        new ServerSetup(0, "127.0.0.1", "imap")});
    greenMail.start();
    GreenMailUser user = greenMail.setUser("inbox@example.com", "inbox@example.com", "pw");
    greenMail.getManagers().getImapHostManager().createMailbox(user, "Done");

    inbox = new EmailInbox() {
      @Override
      public String password() {
        return "pw";
      }
    };
    inbox.ID = "inbox";
    inbox.server = "custom";
    inbox.username = "inbox@example.com";
    inbox.properties = EmailTest.properties(greenMail);

    // send three mails, the second one with an attachment and as reply to the first
    Email email = EmailTest.email(greenMail, "pw");
    greenMail.setUser("sender@example.com", "sender@example.com", "pw");
    Email.Argument a = new Email.Argument();
    a.to = "inbox@example.com";
    a.subject = "Order 1";
    a.text = "first";
    String first = (String) ((Map<String, Object>) email.run(a)).get("messageId");

    a = new Email.Argument();
    a.to = "inbox@example.com";
    a.subject = "Re: Order 1";
    a.text = "second";
    a.html = "<p>second</p>";
    a.inReplyTo = first;
    Email.Attachment att = new Email.Attachment();
    att.filename = "invoice.pdf";
    att.content = Base64.getEncoder().encodeToString("%PDF-1.4".getBytes());
    a.attachments = List.of(att);
    email.run(a);

    a = new Email.Argument();
    a.to = "inbox@example.com";
    a.subject = "Newsletter";
    a.text = "third";
    email.run(a);
    Assertions.assertTrue(greenMail.waitForIncomingEmail(5000, 3));
  }

  @AfterEach
  public void after() {
    greenMail.stop();
  }

  List<Map<String, Object>> list(EmailInbox.Argument a) throws Exception {
    return (List<Map<String, Object>>) inbox.run(a);
  }

  @Test
  public void testList() throws Exception {
    List<Map<String, Object>> all = list(new EmailInbox.Argument());
    Assertions.assertEquals(3, all.size());
    Map<String, Object> second = all.get(1);
    Assertions.assertEquals("Re: Order 1", second.get("subject"));
    Assertions.assertEquals("sender@example.com", second.get("from"));
    Assertions.assertEquals(List.of("inbox@example.com"), second.get("to"));
    Assertions.assertEquals(all.get(0).get("messageId"), second.get("inReplyTo"));
    Assertions.assertEquals("second", second.get("text"));
    Assertions.assertEquals("<p>second</p>", second.get("html"));
    Assertions.assertEquals("INBOX", second.get("folder"));
    Assertions.assertEquals(false, second.get("seen"), "listing does not mark messages as read");
    List<Map<String, Object>> attachments = (List<Map<String, Object>>) second.get("attachments");
    Assertions.assertEquals(1, attachments.size());
    Assertions.assertEquals("invoice.pdf", attachments.get(0).get("filename"));
    Assertions.assertEquals("application/pdf", attachments.get(0).get("contentType"));

    // filters
    EmailInbox.Argument a = new EmailInbox.Argument();
    a.subject = "Order";
    Assertions.assertEquals(2, list(a).size());
    a.from = "nobody";
    Assertions.assertEquals(0, list(a).size());
    a = new EmailInbox.Argument();
    a.since = "2000-01-01";
    Assertions.assertEquals(3, list(a).size());

    // limit returns the newest messages
    a = new EmailInbox.Argument();
    a.limit = 1;
    a.body = false;
    List<Map<String, Object>> newest = list(a);
    Assertions.assertEquals("Newsletter", newest.get(0).get("subject"));
    Assertions.assertFalse(newest.get(0).containsKey("text"));
  }

  @Test
  public void testIncremental() throws Exception {
    List<Map<String, Object>> all = list(new EmailInbox.Argument());
    long firstUid = (Long) all.get(0).get("uid");
    long lastUid = (Long) all.get(2).get("uid");

    EmailInbox.Argument a = new EmailInbox.Argument();
    a.sinceUid = firstUid;
    a.limit = 1;
    List<Map<String, Object>> next = list(a);
    Assertions.assertEquals(1, next.size());
    Assertions.assertEquals("Re: Order 1", next.get(0).get("subject"), "oldest first");

    a = new EmailInbox.Argument();
    a.sinceUid = lastUid;
    Assertions.assertEquals(0, list(a).size(), "nothing new");
  }

  @Test
  public void testActions() throws Exception {
    List<Map<String, Object>> all = list(new EmailInbox.Argument());
    long uid = (Long) all.get(1).get("uid");

    // get
    EmailInbox.Argument a = new EmailInbox.Argument();
    a.action = "get";
    a.uid = uid;
    Assertions.assertEquals("Re: Order 1", ((Map<String, Object>) inbox.run(a)).get("subject"));

    // attachment
    a.action = "attachment";
    a.index = 0;
    Map<String, Object> att = (Map<String, Object>) inbox.run(a);
    Assertions.assertEquals("invoice.pdf", att.get("filename"));
    Assertions.assertEquals("%PDF-1.4",
        new String(Base64.getDecoder().decode((String) att.get("content")), StandardCharsets.UTF_8));
    a.index = 5;
    Assertions.assertThrows(Exception.class, () -> inbox.run(a));

    // flags
    EmailInbox.Argument f = new EmailInbox.Argument();
    f.action = "markRead";
    f.uids = List.of(uid);
    Assertions.assertEquals(Map.of("updated", 1), inbox.run(f));
    f.action = "flag";
    inbox.run(f);
    EmailInbox.Argument unread = new EmailInbox.Argument();
    unread.unread = true;
    Assertions.assertEquals(2, list(unread).size());
    Map<String, Object> m = list(new EmailInbox.Argument()).get(1);
    Assertions.assertEquals(true, m.get("seen"));
    Assertions.assertEquals(true, m.get("flagged"));

    // list with markRead
    unread.markRead = true;
    Assertions.assertEquals(2, list(unread).size());
    Assertions.assertEquals(0, list(unread).size());

    // move
    EmailInbox.Argument mv = new EmailInbox.Argument();
    mv.action = "move";
    mv.uid = uid;
    mv.target = "Done";
    Assertions.assertEquals(Map.of("moved", 1), inbox.run(mv));
    Assertions.assertEquals(2, list(new EmailInbox.Argument()).size());
    EmailInbox.Argument done = new EmailInbox.Argument();
    done.folder = "Done";
    List<Map<String, Object>> moved = list(done);
    Assertions.assertEquals(1, moved.size());
    Assertions.assertEquals("Re: Order 1", moved.get(0).get("subject"));
    mv.target = "Missing";
    Assertions.assertThrows(Exception.class, () -> inbox.run(mv));

    // folders
    EmailInbox.Argument folders = new EmailInbox.Argument();
    folders.action = "folders";
    List<String> names = (List<String>) inbox.run(folders);
    Assertions.assertTrue(names.contains("INBOX"), "" + names);
    Assertions.assertTrue(names.contains("Done"), "" + names);
  }

  /**
   * IMAP login via XOAUTH2 (greenmail accepts the user's password as token)
   */
  @Test
  public void testOAuth() throws Exception {
    OAuth2Credentials creds = new OAuth2Credentials() {
      @Override
      public String password() {
        return "secret";
      }

      @Override
      Token fetch(String mailbox, String secret) {
        Token t = new Token();
        t.accessToken = "pw";
        t.expires = now() + 3600;
        return t;
      }
    };
    creds.ID = "oauth";
    creds.provider = "microsoft";
    EmailInbox i = new EmailInbox() {
      @Override
      OAuth2Credentials oauthCredentials() {
        return creds;
      }
    };
    i.ID = "inbox";
    i.server = "custom";
    i.username = "inbox@example.com";
    i.oauth = "oauth";
    i.properties = EmailTest.properties(greenMail);
    Assertions.assertEquals("XOAUTH2", i.properties(false).get("mail.imap.auth.mechanisms"));
    Assertions.assertEquals(3, ((List<?>) i.run(null)).size());
  }

  /**
   * on a large mailbox, list only fetches the envelopes of the selected messages
   */
  @Test
  public void testLargeMailbox() throws Exception {
    GreenMailUser user = greenMail.getUserManager().getUserByEmail("inbox@example.com");
    for (int i = 0; i < 300; i++) {
      jakarta.mail.internet.MimeMessage m =
          new jakarta.mail.internet.MimeMessage((jakarta.mail.Session) null);
      m.setFrom("bulk@example.com");
      m.setRecipients(jakarta.mail.Message.RecipientType.TO, "inbox@example.com");
      m.setSubject("bulk " + i);
      m.setText("body " + i);
      user.deliver(m);
    }

    java.io.ByteArrayOutputStream debug = new java.io.ByteArrayOutputStream();
    EmailInbox traced = new EmailInbox() {
      @Override
      public String password() {
        return "pw";
      }

      @Override
      jakarta.mail.Session session(boolean smtp) {
        jakarta.mail.Session s = super.session(smtp);
        s.setDebug(true);
        s.setDebugOut(new java.io.PrintStream(debug, true));
        return s;
      }
    };
    traced.ID = "inbox";
    traced.server = "custom";
    traced.username = "inbox@example.com";
    traced.properties = EmailTest.properties(greenMail);

    // newest 3
    EmailInbox.Argument a = new EmailInbox.Argument();
    a.limit = 3;
    List<Map<String, Object>> res = (List<Map<String, Object>>) traced.run(a);
    Assertions.assertEquals(List.of("bulk 297", "bulk 298", "bulk 299"),
        res.stream().map(m -> m.get("subject")).toList());
    Assertions.assertEquals(3, envelopes(debug), "only the selected messages are fetched");

    // incremental: the 3 messages after the 10th
    a = new EmailInbox.Argument();
    a.limit = 1000;
    a.body = false;
    List<Object> uids = list(a).stream().map(m -> m.get("uid")).toList();
    Assertions.assertEquals(303, uids.size());
    debug.reset();
    a = new EmailInbox.Argument();
    a.limit = 3;
    a.sinceUid = (Long) uids.get(9);
    res = (List<Map<String, Object>>) traced.run(a);
    Assertions.assertEquals(uids.subList(10, 13), res.stream().map(m -> m.get("uid")).toList(),
        "oldest first");
    Assertions.assertEquals(3, envelopes(debug));

    // server side search
    debug.reset();
    a = new EmailInbox.Argument();
    a.subject = "bulk 1";
    a.limit = 2;
    res = (List<Map<String, Object>>) traced.run(a);
    Assertions.assertEquals(List.of("bulk 198", "bulk 199"),
        res.stream().map(m -> m.get("subject")).toList());
    Assertions.assertEquals(2, envelopes(debug));
  }

  /**
   * number of ENVELOPE items the server sent
   */
  static int envelopes(java.io.ByteArrayOutputStream debug) {
    String log = debug.toString(StandardCharsets.UTF_8);
    int count = 0;
    for (String line : log.split("\n"))
      if (line.startsWith("* ") && line.contains("ENVELOPE ("))
        count++;
    return count;
  }

  /**
   * a server that accepts the connection but never answers results in an error, not a hang
   */
  @Test
  @org.junit.jupiter.api.Timeout(20)
  public void testTimeout() throws Exception {
    try (java.net.ServerSocket silent = new java.net.ServerSocket(0)) {
      EmailInbox i = new EmailInbox() {
        @Override
        public String password() {
          return "pw";
        }
      };
      i.ID = "inbox";
      i.server = "custom";
      i.username = "inbox@example.com";
      i.properties = Map.of("mail.store.protocol", "imap", "mail.imap.host", "127.0.0.1",
          "mail.imap.port", silent.getLocalPort(), "mail.imap.timeout", 500);
      Assertions.assertEquals("60000", new EmailInbox().properties(false).get("mail.imaps.timeout"));
      Assertions.assertEquals("500", i.properties(false).get("mail.imap.timeout"));
      Assertions.assertThrows(jakarta.mail.MessagingException.class, () -> i.run(null));
    }
  }

  @Test
  public void testReadOnly() throws Exception {
    inbox.readOnly = true;
    EmailInbox.Argument a = new EmailInbox.Argument();
    Assertions.assertEquals(3, list(a).size());
    a.action = "markRead";
    a.uid = 1L;
    Assertions.assertThrows(Exception.class, () -> inbox.run(a));
  }

  @Test
  public void testUnconfigured() {
    EmailInbox i = new EmailInbox();
    Assertions.assertThrows(Exception.class, () -> i.run(null));
  }

  @Test
  public void testDate() {
    Assertions.assertNotNull(EmailInbox.date("2026-01-31"));
    Assertions.assertEquals(0, EmailInbox.date("2026-01-31T10:00:00Z").getTime() % 1000);
    Assertions.assertEquals(EmailInbox.date("2026-01-31T10:00:00Z"),
        EmailInbox.date("2026-01-31T11:00:00+01:00"));
  }
}
