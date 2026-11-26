package org.dashjoin.function;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dashjoin.util.FileSystem;
import org.eclipse.angus.mail.imap.IMAPFolder;
import jakarta.mail.Address;
import jakarta.mail.FetchProfile;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.UIDFolder;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.ComparisonTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.FromStringTerm;
import jakarta.mail.search.ReceivedDateTerm;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.SubjectTerm;

/**
 * reads and manages a mailbox via IMAP. Supports Gmail and Office 365 presets as well as OAuth2
 * (XOAUTH2) login.
 *
 * Messages are identified by their IMAP UID which is stable within a folder as long as the folder's
 * uidValidity does not change. To process incoming mail, schedule an Invoke function that lists
 * messages with sinceUid set to the highest UID processed so far.
 */
public class EmailInbox extends AbstractMailFunction<EmailInbox.Argument, Object> {

  static final int DEFAULT_LIMIT = 50;

  /**
   * inbox argument
   */
  public static class Argument {

    /**
     * list (default), get, attachment, folders, markRead, markUnread, flag, unflag, move
     */
    public String action;

    /**
     * folder name (default INBOX)
     */
    public String folder;

    /**
     * list: only unread messages
     */
    public Boolean unread;

    /**
     * list: only messages received on or after this date (ISO date or date time)
     */
    public String since;

    /**
     * list: only messages whose sender contains this string
     */
    public String from;

    /**
     * list: only messages whose subject contains this string
     */
    public String subject;

    /**
     * list: only messages with a UID greater than this (incremental processing). The oldest
     * messages are returned first
     */
    public Long sinceUid;

    /**
     * list: maximum number of messages (default 50). Without sinceUid, the newest messages are
     * returned
     */
    public Integer limit;

    /**
     * list: include text and html bodies (default true)
     */
    public Boolean body;

    /**
     * list: mark the returned messages as read (default false)
     */
    public Boolean markRead;

    /**
     * message UID for get, attachment and the actions
     */
    public Long uid;

    /**
     * message UIDs for the actions
     */
    public List<Long> uids;

    /**
     * move: the target folder
     */
    public String target;

    /**
     * attachment: index of the attachment (see the attachments of the message, default 0)
     */
    public Integer index;

    /**
     * attachment: save the attachment to this path in the upload folder (e.g.
     * upload/inbox/invoice.pdf). If omitted, the content is returned base64 encoded
     */
    public String saveAs;
  }

  @Override
  public Object run(Argument arg) throws Exception {
    if (arg == null)
      arg = new Argument();
    if (unconfigured())
      throw new Exception("Please configure the mail server (server preset or properties)");

    String action = arg.action == null ? "list" : arg.action;
    boolean write = !Arrays.asList("list", "get", "attachment", "folders").contains(action)
        || Boolean.TRUE.equals(arg.markRead);
    if (write && readOnly)
      throw new Exception("EmailInbox action " + action + " is not allowed in read only mode");

    Argument a = arg;
    return withLogin((user, secret) -> {
      Store store = session(false).getStore();
      store.connect(user, secret);
      try {
        if (action.equals("folders"))
          return folders(store);
        Folder folder = store.getFolder(a.folder == null ? "INBOX" : a.folder);
        folder.open(write ? Folder.READ_WRITE : Folder.READ_ONLY);
        try {
          switch (action) {
            case "list":
              return list(folder, a);
            case "get":
              return toMap(folder, message(folder, a.uid), true);
            case "attachment":
              return attachment(message(folder, a.uid), a);
            case "markRead":
              return setFlag(folder, a, Flags.Flag.SEEN, true);
            case "markUnread":
              return setFlag(folder, a, Flags.Flag.SEEN, false);
            case "flag":
              return setFlag(folder, a, Flags.Flag.FLAGGED, true);
            case "unflag":
              return setFlag(folder, a, Flags.Flag.FLAGGED, false);
            case "move":
              return move(store, folder, a);
            default:
              throw new Exception("Unknown action: " + action);
          }
        } finally {
          if (folder.isOpen())
            folder.close(false);
        }
      } finally {
        store.close();
      }
    });
  }

  @Override
  Session session(boolean smtp) {
    // do not mark messages as read when reading their content
    java.util.Properties prop = properties(false);
    prop.putIfAbsent("mail.imap.peek", "true");
    prop.putIfAbsent("mail.imaps.peek", "true");
    return Session.getInstance(prop);
  }

  static List<String> folders(Store store) throws Exception {
    List<String> res = new ArrayList<>();
    for (Folder f : store.getDefaultFolder().list("*"))
      if ((f.getType() & Folder.HOLDS_MESSAGES) != 0)
        res.add(f.getFullName());
    return res;
  }

  static List<Map<String, Object>> list(Folder folder, Argument arg) throws Exception {
    UIDFolder uf = (UIDFolder) folder;
    int limit = arg.limit == null ? DEFAULT_LIMIT : arg.limit;

    // Message objects are lazy: nothing is fetched from the server until a property is accessed.
    // We select the messages by sequence number (which is in UID order within a folder) and only
    // fetch envelopes for the selected ones, so large mailboxes do not slow down the call
    SearchTerm term = term(arg);
    Message[] messages;
    if (arg.sinceUid != null) {
      // UID FETCH n:* (UID) - only the UIDs of the new messages are transferred
      messages = uf.getMessagesByUID(arg.sinceUid + 1, UIDFolder.LASTUID);
      if (term != null && messages.length > 0)
        messages = folder.search(term, messages);
    } else if (term != null)
      // server side search over the whole folder
      messages = folder.search(term);
    else
      messages = folder.getMessages();

    List<Message> selected = new ArrayList<>(Arrays.asList(messages));
    selected.sort((x, y) -> Integer.compare(x.getMessageNumber(), y.getMessageNumber()));
    if (arg.sinceUid != null) {
      // the IMAP range n:* always contains the last message, even if its UID is lower than n
      List<Message> newer = new ArrayList<>();
      for (Message m : selected)
        if (uf.getUID(m) > arg.sinceUid)
          newer.add(m);
      selected = newer;
    }
    if (selected.size() > limit)
      selected = arg.sinceUid != null ? selected.subList(0, limit)
          : selected.subList(selected.size() - limit, selected.size());

    FetchProfile fp = new FetchProfile();
    fp.add(UIDFolder.FetchProfileItem.UID);
    fp.add(FetchProfile.Item.ENVELOPE);
    fp.add(FetchProfile.Item.FLAGS);
    // MIME structure and thread headers, otherwise each costs a round trip per message
    fp.add(FetchProfile.Item.CONTENT_INFO);
    fp.add("Message-ID");
    fp.add("In-Reply-To");
    fp.add("References");
    folder.fetch(selected.toArray(new Message[0]), fp);

    boolean body = arg.body == null || arg.body;
    List<Map<String, Object>> res = new ArrayList<>();
    for (Message m : selected) {
      res.add(toMap(folder, m, body));
      if (Boolean.TRUE.equals(arg.markRead))
        m.setFlag(Flags.Flag.SEEN, true);
    }
    return res;
  }

  static SearchTerm term(Argument arg) {
    List<SearchTerm> terms = new ArrayList<>();
    if (Boolean.TRUE.equals(arg.unread))
      terms.add(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
    if (arg.since != null)
      terms.add(new ReceivedDateTerm(ComparisonTerm.GE, date(arg.since)));
    if (arg.from != null)
      terms.add(new FromStringTerm(arg.from));
    if (arg.subject != null)
      terms.add(new SubjectTerm(arg.subject));
    if (terms.isEmpty())
      return null;
    if (terms.size() == 1)
      return terms.get(0);
    return new AndTerm(terms.toArray(new SearchTerm[0]));
  }

  static Date date(String s) {
    if (s.length() <= 10)
      return Date.from(LocalDate.parse(s).atStartOfDay(ZoneId.systemDefault()).toInstant());
    try {
      return Date.from(Instant.parse(s));
    } catch (Exception e) {
      return Date.from(OffsetDateTime.parse(s).toInstant());
    }
  }

  static Message message(Folder folder, Long uid) throws Exception {
    if (uid == null)
      throw new Exception("Please provide the uid of the message");
    Message m = ((UIDFolder) folder).getMessageByUID(uid);
    if (m == null)
      throw new Exception("Message " + uid + " not found in folder " + folder.getFullName());
    return m;
  }

  static Message[] messages(Folder folder, Argument arg) throws Exception {
    List<Long> uids = new ArrayList<>();
    if (arg.uid != null)
      uids.add(arg.uid);
    if (arg.uids != null)
      uids.addAll(arg.uids);
    if (uids.isEmpty())
      throw new Exception("Please provide uid or uids");
    long[] u = uids.stream().mapToLong(Long::longValue).toArray();
    return Arrays.stream(((UIDFolder) folder).getMessagesByUID(u)).filter(m -> m != null)
        .toArray(Message[]::new);
  }

  static Map<String, Object> setFlag(Folder folder, Argument arg, Flags.Flag flag, boolean value)
      throws Exception {
    Message[] messages = messages(folder, arg);
    folder.setFlags(messages, new Flags(flag), value);
    return Map.of("updated", messages.length);
  }

  static Map<String, Object> move(Store store, Folder folder, Argument arg) throws Exception {
    if (arg.target == null)
      throw new Exception("Please provide the target folder");
    Folder target = store.getFolder(arg.target);
    if (!target.exists())
      throw new Exception("Folder " + arg.target + " does not exist");
    Message[] messages = messages(folder, arg);
    if (folder instanceof IMAPFolder && ((org.eclipse.angus.mail.imap.IMAPStore) store)
        .hasCapability("MOVE"))
      ((IMAPFolder) folder).moveMessages(messages, target);
    else {
      folder.copyMessages(messages, target);
      folder.setFlags(messages, new Flags(Flags.Flag.DELETED), true);
      folder.expunge();
    }
    return Map.of("moved", messages.length);
  }

  /**
   * converts the message to JSON
   */
  static Map<String, Object> toMap(Folder folder, Message m, boolean body) throws Exception {
    Map<String, Object> res = new LinkedHashMap<>();
    UIDFolder uf = (UIDFolder) folder;
    res.put("uid", uf.getUID(m));
    res.put("uidValidity", uf.getUIDValidity());
    res.put("folder", folder.getFullName());
    res.put("messageId", header(m, "Message-ID"));
    res.put("inReplyTo", header(m, "In-Reply-To"));
    res.put("references", header(m, "References"));
    Address[] from = m.getFrom();
    res.put("from", from == null || from.length == 0 ? null : address(from[0]));
    res.put("fromName", from == null || from.length == 0 ? null : name(from[0]));
    res.put("to", addresses(m.getRecipients(Message.RecipientType.TO)));
    res.put("cc", addresses(m.getRecipients(Message.RecipientType.CC)));
    res.put("replyTo", addresses(m.getReplyTo()));
    res.put("subject", m.getSubject());
    res.put("date", m.getSentDate() == null ? null : m.getSentDate().toInstant().toString());
    res.put("received",
        m.getReceivedDate() == null ? null : m.getReceivedDate().toInstant().toString());
    res.put("seen", m.isSet(Flags.Flag.SEEN));
    res.put("flagged", m.isSet(Flags.Flag.FLAGGED));
    res.put("answered", m.isSet(Flags.Flag.ANSWERED));

    Content c = new Content();
    c.walk(m);
    if (body) {
      res.put("text", c.text);
      res.put("html", c.html);
    }
    List<Map<String, Object>> attachments = new ArrayList<>();
    for (int i = 0; i < c.attachments.size(); i++) {
      Part p = c.attachments.get(i);
      Map<String, Object> a = new LinkedHashMap<>();
      a.put("index", i);
      a.put("filename", filename(p));
      a.put("contentType", baseType(p.getContentType()));
      a.put("size", p.getSize());
      attachments.add(a);
    }
    res.put("attachments", attachments);
    return res;
  }

  static Map<String, Object> attachment(Message m, Argument arg) throws Exception {
    Content c = new Content();
    c.walk(m);
    int index = arg.index == null ? 0 : arg.index;
    if (index < 0 || index >= c.attachments.size())
      throw new Exception("The message has no attachment with index " + index);
    Part p = c.attachments.get(index);
    byte[] data;
    try (InputStream in = p.getInputStream()) {
      data = in.readAllBytes();
    }
    Map<String, Object> res = new LinkedHashMap<>();
    res.put("filename", filename(p));
    res.put("contentType", baseType(p.getContentType()));
    res.put("size", data.length);
    if (arg.saveAs != null) {
      File file = FileSystem.getUploadFile(arg.saveAs);
      if (file.getParentFile() != null)
        file.getParentFile().mkdirs();
      Files.write(file.toPath(), data);
      res.put("path", arg.saveAs);
    } else
      res.put("content", Base64.getEncoder().encodeToString(data));
    return res;
  }

  /**
   * collects the first text and html body and the attachments of a message
   */
  static class Content {
    String text;
    String html;
    List<Part> attachments = new ArrayList<>();

    void walk(Part p) throws Exception {
      String disposition = p.getDisposition();
      boolean attachment = Part.ATTACHMENT.equalsIgnoreCase(disposition)
          || (filename(p) != null && !p.isMimeType("multipart/*"));
      if (p.isMimeType("multipart/*")) {
        Multipart mp = (Multipart) p.getContent();
        for (int i = 0; i < mp.getCount(); i++)
          walk(mp.getBodyPart(i));
      } else if (attachment || p.isMimeType("message/rfc822"))
        attachments.add(p);
      else if (p.isMimeType("text/plain") && text == null)
        text = string(p);
      else if (p.isMimeType("text/html") && html == null)
        html = string(p);
      else if (!p.isMimeType("text/*"))
        attachments.add(p);
    }

    static String string(Part p) throws Exception {
      Object o = p.getContent();
      return o instanceof String ? (String) o : null;
    }
  }

  static String filename(Part p) throws Exception {
    String name = p.getFileName();
    return name == null ? null : MimeUtility.decodeText(name);
  }

  static String baseType(String contentType) {
    if (contentType == null)
      return null;
    int i = contentType.indexOf(';');
    return (i < 0 ? contentType : contentType.substring(0, i)).trim().toLowerCase();
  }

  static String header(Message m, String name) throws Exception {
    String[] h = m.getHeader(name);
    return h == null || h.length == 0 ? null : h[0];
  }

  static String address(Address a) {
    return a instanceof InternetAddress ? ((InternetAddress) a).getAddress() : a.toString();
  }

  static String name(Address a) {
    return a instanceof InternetAddress ? ((InternetAddress) a).getPersonal() : null;
  }

  static List<String> addresses(Address[] a) {
    List<String> res = new ArrayList<>();
    if (a != null)
      for (Address x : a)
        res.add(address(x));
    return res;
  }

  @Override
  public Class<Argument> getArgumentClass() {
    return Argument.class;
  }
}
