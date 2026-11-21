package org.dashjoin.function;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dashjoin.function.Email.Argument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;

public class EmailTest {

  GreenMail greenMail;

  @BeforeEach
  public void before() {
    greenMail = new GreenMail(new ServerSetup[] {new ServerSetup(0, "127.0.0.1", "smtp"),
        new ServerSetup(0, "127.0.0.1", "imap")});
    greenMail.start();
    greenMail.setUser("sender@example.com", "sender@example.com", "pw");
    OAuth2Credentials.cache.clear();
  }

  @AfterEach
  public void after() {
    greenMail.stop();
  }

  /**
   * properties pointing to the greenmail test server
   */
  static Map<String, Object> properties(GreenMail greenMail) {
    Map<String, Object> prop = new HashMap<>();
    prop.put("mail.smtp.host", "127.0.0.1");
    prop.put("mail.smtp.port", greenMail.getSmtp().getPort());
    prop.put("mail.smtp.auth", "true");
    prop.put("mail.store.protocol", "imap");
    prop.put("mail.imap.host", "127.0.0.1");
    prop.put("mail.imap.port", greenMail.getImap().getPort());
    return prop;
  }

  static Email email(GreenMail greenMail, String pw) {
    Email e = new Email() {
      @Override
      public String password() {
        return pw;
      }
    };
    e.ID = "email";
    e.server = "custom";
    e.username = "sender@example.com";
    e.properties = properties(greenMail);
    return e;
  }

  @Test
  public void testMail() throws Exception {
    Email e = new Email();
    Argument a = new Email.Argument();
    a.from = "test@example.com";
    a.to = "test@example.com";
    a.subject = "subject";
    a.text = "email body";
    Assertions.assertEquals(
        "From: test@example.com, To: test@example.com, Subject: subject, Text: email body",
        e.run(a));
  }

  @Test
  public void testValidation() {
    Email e = new Email();
    Argument a = new Email.Argument();
    a.to = "x@example.com";
    a.text = "t";
    Assertions.assertThrows(Exception.class, () -> e.run(a), "from or username required");
    a.from = "y@example.com";
    a.text = null;
    Assertions.assertThrows(Exception.class, () -> e.run(a), "text or html required");
  }

  @Test
  public void testPresets() {
    Email e = new Email();
    e.server = "office365";
    Assertions.assertEquals("smtp.office365.com", e.properties(true).get("mail.smtp.host"));
    Assertions.assertEquals("outlook.office365.com", e.properties(false).get("mail.imaps.host"));
    e.server = "gmail";
    e.oauth = "oauth";
    e.properties = Map.of("mail.smtp.port", 465);
    Assertions.assertEquals("smtp.gmail.com", e.properties(true).get("mail.smtp.host"));
    Assertions.assertEquals("465", e.properties(true).get("mail.smtp.port"));
    Assertions.assertEquals("XOAUTH2", e.properties(true).get("mail.smtp.auth.mechanisms"));
    Assertions.assertFalse(e.unconfigured());
  }

  @Test
  public void testSend() throws Exception {
    Email e = email(greenMail, "pw");
    Argument a = new Email.Argument();
    a.to = "a@example.com, b@example.com";
    a.cc = "c@example.com";
    a.bcc = "d@example.com";
    a.replyTo = "support@example.com";
    a.subject = "Grüße";
    a.text = "plain";
    a.html = "<b>html</b>";
    a.inReplyTo = "<orig@example.com>";
    Email.Attachment att = new Email.Attachment();
    att.filename = "data.csv";
    att.content = Base64.getEncoder().encodeToString("a,b\n1,2".getBytes());
    Email.Attachment txt = new Email.Attachment();
    txt.filename = "note";
    txt.text = "hello";
    a.attachments = List.of(att, txt);

    @SuppressWarnings("unchecked")
    Map<String, Object> res = (Map<String, Object>) e.run(a);
    Assertions.assertEquals("Sent", res.get("status"));

    Assertions.assertTrue(greenMail.waitForIncomingEmail(5000, 4));
    MimeMessage m = greenMail.getReceivedMessagesForDomain("a@example.com")[0];
    Assertions.assertEquals(res.get("messageId"), m.getMessageID());
    Assertions.assertEquals("sender@example.com", m.getFrom()[0].toString());
    Assertions.assertEquals(2, m.getRecipients(Message.RecipientType.TO).length);
    Assertions.assertEquals("c@example.com", m.getRecipients(Message.RecipientType.CC)[0].toString());
    Assertions.assertNull(m.getRecipients(Message.RecipientType.BCC), "bcc is not visible");
    Assertions.assertEquals("support@example.com", m.getReplyTo()[0].toString());
    Assertions.assertEquals("Grüße", m.getSubject());
    Assertions.assertEquals("<orig@example.com>", m.getHeader("In-Reply-To")[0]);
    Assertions.assertEquals("<orig@example.com>", m.getHeader("References")[0]);

    Multipart mixed = (Multipart) m.getContent();
    Assertions.assertEquals(3, mixed.getCount());
    Multipart alt = (Multipart) mixed.getBodyPart(0).getContent();
    Assertions.assertEquals("plain", alt.getBodyPart(0).getContent());
    Assertions.assertEquals("<b>html</b>", alt.getBodyPart(1).getContent());
    Part csv = mixed.getBodyPart(1);
    Assertions.assertEquals("data.csv", csv.getFileName());
    Assertions.assertEquals("a,b\n1,2",
        new String(csv.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
    Assertions.assertTrue(mixed.getBodyPart(2).isMimeType("text/plain"));
  }

  @Test
  public void testHtmlOnly() throws Exception {
    Email e = email(greenMail, "pw");
    Argument a = new Email.Argument();
    a.to = "a@example.com";
    a.subject = "s";
    a.html = "<p>hi</p>";
    e.run(a);
    MimeMessage m = greenMail.getReceivedMessages()[0];
    Assertions.assertTrue(m.isMimeType("text/html"));
  }

  @Test
  public void testWrongPassword() {
    Email e = email(greenMail, "wrong");
    Argument a = new Email.Argument();
    a.to = "a@example.com";
    a.text = "t";
    Assertions.assertThrows(jakarta.mail.AuthenticationFailedException.class, () -> e.run(a));
  }

  /**
   * XOAUTH2 login: the first token is rejected, the function drops it and retries with a fresh
   * token
   */
  @Test
  public void testOAuth() throws Exception {
    greenMail.setUser("sender@example.com", "sender@example.com", "token-2");
    int[] calls = {0};
    OAuth2Credentials creds = new OAuth2Credentials() {
      @Override
      public String password() {
        return "secret";
      }

      @Override
      Token fetch(String mailbox, String secret) {
        Assertions.assertEquals("sender@example.com", mailbox);
        Token t = new Token();
        t.accessToken = "token-" + (++calls[0]);
        t.expires = now() + 3600;
        return t;
      }
    };
    creds.ID = "oauth";
    creds.provider = "google";

    Email e = new Email() {
      @Override
      OAuth2Credentials oauthCredentials() {
        return creds;
      }
    };
    e.ID = "email";
    e.server = "custom";
    e.username = "sender@example.com";
    e.oauth = "oauth";
    e.properties = properties(greenMail);

    Argument a = new Email.Argument();
    a.to = "a@example.com";
    a.text = "via oauth";
    e.run(a);
    Assertions.assertEquals(2, calls[0]);
    Assertions.assertEquals(1, greenMail.getReceivedMessages().length);

    // cached token is reused
    e.run(a);
    Assertions.assertEquals(2, calls[0]);
  }

  /**
   * manual test against a real server: java EmailTest gmail|office365
   */
  public static void main(String[] args) throws Exception {

    BufferedReader obj = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("user");
    String user = obj.readLine();
    System.out.println("password");
    String pw = obj.readLine();

    Email email = new Email() {
      @Override
      public String password() {
        return pw;
      }
    };
    email.server = args.length > 0 ? args[0] : "gmail";
    email.username = user;

    Email.Argument arg = new Email.Argument();
    arg.to = user;
    arg.subject = "test mail";
    arg.text = "content";
    System.out.println(email.run(arg));
  }
}
