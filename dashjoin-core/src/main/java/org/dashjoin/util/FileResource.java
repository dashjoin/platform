package org.dashjoin.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.poi.util.BoundedInputStream;

/**
 * Represents a file resource.
 * 
 * Optionally has a range. File ranges are encoded with file:// URLs using query parameters start and
 * size:
 * 
 * file:///path/to/file?start=<start>&size=<size>
 */
public class FileResource {
  public String file;
  public Long start;
  public Long size;

  public FileResource(String file) {
    if (file.startsWith("file:"))
      file = file.substring("file:".length());
    this.file = file;
  }

  public FileResource(String file, long start, long size) {
    this(file);

    assert size >= 0;
    assert start >= 0;

    this.start = start;
    this.size = size;
  }

  public static FileResource of(String file) {
    URL url;
    try {
      url = new URL(file);
      if ("file".equalsIgnoreCase(url.getProtocol())) {
        FileResource fr = new FileResource(url.getPath());
        for (NameValuePair pair : URLEncodedUtils.parse(url.getQuery(), Charset.defaultCharset())) {
          if ("start".equals(pair.getName()))
            fr.start = Long.parseLong(pair.getValue());
          else if ("size".equals(pair.getName()))
            fr.size = Long.parseLong(pair.getValue());
        }
        return fr;
      }
    } catch (MalformedURLException e) {
      // ignore
    }
    FileResource fr = new FileResource(file);
    fr.file = file;
    return fr;
  }

  public String toString() {
    String res = file;
    if (!res.startsWith("file:"))
      res = "file:" + res;
    if (start != null && size != null)
      res += "?start=" + start + "&size=" + size;
    return res;
  }

  public InputStream getInputStream() throws IOException {
    FileInputStream fin = new FileInputStream(FileSystem.getUploadFile(file));
    if (start == null || size == null)
      return fin;

    fin.skip(start);
    return new BoundedInputStream(fin, size);
  }
}
