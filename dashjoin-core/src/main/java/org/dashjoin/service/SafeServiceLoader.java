package org.dashjoin.service;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import lombok.extern.java.Log;

/**
 * ServiceLoader that is resilient against major errors (ClassNotFound, ErrorInInitializer etc.)
 *
 * @author uli
 */
@Log
public class SafeServiceLoader {

  /*
   * Loads the services of the given class. Ignores services that are not loadable.
   * 
   * @param <T> Class of the service
   * 
   * @return The iterable list of loaded services
   */
  public static <T> Iterable<T> load(Class<T> service) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        Iterator<T> it = ServiceLoader.load(service).iterator();
        return new SafeIterator<>(it);
      }
    };
  }

  /**
   * A safe iterator that ignores errors in next() and just skips those elements
   *
   * @param <T> Class of iterable elements
   */
  public static class SafeIterator<T> implements Iterator<T> {

    Iterator<T> it;

    SafeIterator(Iterator<T> it) {
      this.it = it;
    }

    T safeNext() {
      T next = null;
      while (it.hasNext())
        try {
          next = it.next();
          break;
        } catch (Throwable t) {
          log.log(Level.FINE, "Error in safeNext", t);
        }
      return next;
    }

    T obj = null;

    @Override
    public boolean hasNext() {
      if (obj == null)
        obj = safeNext();
      return obj != null;
    }

    @Override
    public T next() {
      if (obj == null)
        obj = safeNext();
      T res = obj;
      obj = null;
      return res;
    }
  }
}
