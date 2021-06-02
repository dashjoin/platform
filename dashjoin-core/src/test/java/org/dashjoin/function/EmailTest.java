package org.dashjoin.function;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class EmailTest {

  public static void main(String[] args) throws Exception {

    BufferedReader obj = new BufferedReader(new InputStreamReader(System.in));

    Email email = new Email();
    System.out.println("user");
    email.username = obj.readLine();
    System.out.println("password");
    email.password = obj.readLine();
    Map<String, Object> prop = new HashMap<String, Object>();
    prop.put("mail.smtp.host", "smtp.gmail.com");
    prop.put("mail.smtp.port", "587");
    prop.put("mail.smtp.auth", "true");
    prop.put("mail.smtp.starttls.enable", "true");
    email.properties = prop;

    Email.Argument arg = email.getArgumentClass().getDeclaredConstructor().newInstance();
    arg.from = email.username;
    arg.to = email.username;
    arg.subject = "test mail";
    arg.text = "content";
    email.run(arg);
  }
}
