package org.dashjoin.function;

import java.io.File;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.dashjoin.util.FileSystem;
import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

/**
 * email action: sends an email via SMTP. Supports Gmail and Office 365 presets as well as OAuth2
 * (XOAUTH2) login
 */
public class Email extends AbstractMailFunction<Email.Argument, Object> {

  /**
   * email argument
   */
  public static class Argument {

    /**
     * sender in RFC822 syntax (defaults to username)
     */
    public String from;

    /**
     * comma separated list of recipients in RFC822 syntax
     */
    public String to;

    /**
     * comma separated list of CC recipients
     */
    public String cc;

    /**
     * comma separated list of BCC recipients
     */
    public String bcc;

    /**
     * comma separated list of reply-to addresses
     */
    public String replyTo;

    public String subject;

    /**
     * plain text body
     */
    public String text;

    /**
     * HTML body. If text is also specified, both are sent as alternatives
     */
    public String html;

    /**
     * message ID of the email this is a reply to (sets In-Reply-To and References so the reply is
     * shown in the same thread)
     */
    public String inReplyTo;

    /**
     * space separated message IDs of the thread (defaults to inReplyTo)
     */
    public String references;

    /**
     * additional headers
     */
    public Map<String, String> headers;

    public List<Attachment> attachments;
  }

  /**
   * email attachment. Specify exactly one of path, content or text
   */
  public static class Attachment {

    /**
     * file name shown to the recipient (defaults to the file name of path)
     */
    public String filename;

    /**
     * MIME type (default: guessed from the file name)
     */
    public String contentType;

    /**
     * file in the upload folder, e.g. upload/invoice.pdf
     */
    public String path;

    /**
     * base64 encoded content
     */
    public String content;

    /**
     * text content
     */
    public String text;
  }

  /**
   * sends an email. Returns the message ID which can be used as inReplyTo of a later message. If no
   * server is configured, returns a description of the email without sending it
   */
  @Override
  public Object run(Email.Argument arg) throws Exception {

    if (arg == null)
      throw new Exception("Please provide an argument");
    if (arg.from == null && username == null)
      throw new Exception("Please provide a 'from' field in the argument");
    if (arg.to == null)
      throw new Exception("Please provide a 'to' field in the argument");
    if (arg.text == null && arg.html == null)
      throw new Exception("Please provide a 'text' or 'html' field in the argument");

    String from = arg.from != null ? arg.from : username;

    if (unconfigured() || readOnly)
      return "From: " + from + ", To: " + arg.to + ", Subject: " + arg.subject + ", Text: "
          + (arg.text != null ? arg.text : arg.html);

    Session session = session(true);
    MimeMessage message = message(session, from, arg);
    withLogin((user, secret) -> {
      if (user == null)
        Transport.send(message);
      else
        Transport.send(message, user, secret);
      return null;
    });

    Map<String, Object> res = new LinkedHashMap<>();
    res.put("status", "Sent");
    res.put("messageId", message.getMessageID());
    return res;
  }

  static MimeMessage message(Session session, String from, Email.Argument arg) throws Exception {
    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress(from));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(arg.to));
    if (arg.cc != null)
      message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(arg.cc));
    if (arg.bcc != null)
      message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(arg.bcc));
    if (arg.replyTo != null)
      message.setReplyTo(InternetAddress.parse(arg.replyTo));
    message.setSubject(arg.subject, "UTF-8");

    if (arg.headers != null)
      for (Entry<String, String> e : arg.headers.entrySet())
        message.setHeader(e.getKey(), e.getValue());
    if (arg.inReplyTo != null) {
      message.setHeader("In-Reply-To", arg.inReplyTo);
      message.setHeader("References", arg.references != null ? arg.references : arg.inReplyTo);
    } else if (arg.references != null)
      message.setHeader("References", arg.references);

    boolean hasAttachments = arg.attachments != null && !arg.attachments.isEmpty();
    if (!hasAttachments) {
      if (arg.html == null)
        message.setText(arg.text, "UTF-8");
      else if (arg.text == null)
        message.setText(arg.html, "UTF-8", "html");
      else
        message.setContent(alternative(arg));
      return message;
    }

    MimeBodyPart body = new MimeBodyPart();
    if (arg.html == null)
      body.setText(arg.text, "UTF-8");
    else if (arg.text == null)
      body.setText(arg.html, "UTF-8", "html");
    else
      body.setContent(alternative(arg));

    MimeMultipart mixed = new MimeMultipart("mixed");
    mixed.addBodyPart(body);
    for (Attachment a : arg.attachments)
      mixed.addBodyPart(attachment(a));
    message.setContent(mixed);
    return message;
  }

  /**
   * multipart/alternative with text and html
   */
  static MimeMultipart alternative(Email.Argument arg) throws Exception {
    MimeMultipart alt = new MimeMultipart("alternative");
    MimeBodyPart text = new MimeBodyPart();
    text.setText(arg.text, "UTF-8");
    alt.addBodyPart(text);
    MimeBodyPart html = new MimeBodyPart();
    html.setText(arg.html, "UTF-8", "html");
    alt.addBodyPart(html);
    return alt;
  }

  static MimeBodyPart attachment(Attachment a) throws Exception {
    byte[] data;
    String filename = a.filename;
    if (a.path != null) {
      File file = FileSystem.getUploadFile(a.path);
      data = Files.readAllBytes(file.toPath());
      if (filename == null)
        filename = file.getName();
    } else if (a.content != null)
      data = Base64.getMimeDecoder().decode(a.content);
    else if (a.text != null)
      data = a.text.getBytes(StandardCharsets.UTF_8);
    else
      throw new Exception("Please provide path, content or text for each attachment");
    if (filename == null)
      throw new Exception("Please provide a filename for each attachment");

    String type = a.contentType;
    if (type == null)
      type = URLConnection.guessContentTypeFromName(filename);
    if (type == null || type.isEmpty())
      type = a.text != null ? "text/plain; charset=UTF-8" : "application/octet-stream";

    MimeBodyPart part = new MimeBodyPart();
    part.setDataHandler(new DataHandler(new ByteArrayDataSource(data, type)));
    part.setFileName(filename);
    part.setDisposition(MimeBodyPart.ATTACHMENT);
    // keep the bytes as they are (no line ending conversion for text types)
    part.setHeader("Content-Transfer-Encoding", "base64");
    return part;
  }

  @Override
  public Class<Argument> getArgumentClass() {
    return Argument.class;
  }
}
