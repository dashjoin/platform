package org.dashjoin.function;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.dashjoin.model.JsonSchema;

/**
 * email action
 */
@JsonSchema(order = {"properties", "username", "password"})
public class Email extends AbstractConfigurableFunction<Email.Argument, String> {

  /**
   * email argument
   */
  public static class Argument {
    public String from;
    public String to;
    public String subject;
    public String text;
  }

  /**
   * username to log into the email service
   */
  @JsonSchema(style = {"width", "400px"})
  public String username;

  /**
   * password to log into the email service
   */
  @JsonSchema(widget = "password", style = {"width", "400px"})
  public String password;

  /**
   * javax.mail properties passed to the session
   */
  @JsonSchema(style = {"width", "400px"}, layout = "vertical")
  public Map<String, Object> properties;

  /**
   * sends an email
   */
  @Override
  public String run(Email.Argument arg) throws Exception {

    Properties prop = new Properties();
    if (properties != null)
      for (Entry<String, Object> e : properties.entrySet())
        prop.put(e.getKey(), e.getValue());

    Session session = Session.getInstance(prop, new jakarta.mail.Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        try {
          return new PasswordAuthentication(username, password());
        } catch (Exception e) {
          throw new RuntimeException();
        }
      }
    });

    if (arg == null)
      throw new Exception("Please provide an argument");
    if (arg.from == null)
      throw new Exception("Please provide a 'from' field in the argument");
    if (arg.to == null)
      throw new Exception("Please provide a 'to' field in the argument");
    if (arg.text == null)
      throw new Exception("Please provide a 'text' field in the argument");

    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress(arg.from));
    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(arg.to));
    message.setSubject(arg.subject);
    message.setText(arg.text);

    if (prop.isEmpty()) {
      String s = "From: " + arg.from + ", To: " + arg.to + ", Subject: " + arg.subject + ", Text: "
          + arg.text;
      return s;
    } else {
      Transport.send(message);
      return "Sent";
    }
  }

  @Override
  public Class<Argument> getArgumentClass() {
    return Argument.class;
  }
}
