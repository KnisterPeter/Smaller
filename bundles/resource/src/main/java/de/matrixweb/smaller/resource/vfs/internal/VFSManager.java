package de.matrixweb.smaller.resource.vfs.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import de.matrixweb.smaller.resource.vfs.VFS;

/**
 * Registers a vfs protocol handler.<br>
 * Inspired by {@link "https://github.com/brianm/url-scheme-registry"}.
 * 
 * @author markusw
 */
public final class VFSManager {

  private static final VFSManager INSTANCE = new VFSManager();

  private final Map<String, VFS> active = new HashMap<String, VFS>();

  /**
   * @param vfs
   * @return Returns a host for use in the given {@link VFS} {@link URL}s.
   */
  public static String register(final VFS vfs) {
    final String host = UUID.randomUUID().toString();
    getInstance().active.put(host, vfs);
    return host;
  }

  /**
   * @param vfs
   */
  public static void unregister(final VFS vfs) {
    getInstance().active.remove(vfs);
  }

  private static VFSManager getInstance() {
    return INSTANCE;
  }

  private VFSManager() {
    final String key = "java.protocol.handler.pkgs";
    final String pkg = "de.matrixweb.smaller.resource.vfs.internal.generated";
    final String current = System.getProperty(key);
    if (current != null) {
      if (!current.contains(pkg)) {
        System.setProperty(key, current + "|" + pkg);
      }
    } else {
      System.setProperty(key, pkg);
    }
    final Enhancer e = new Enhancer();
    e.setNamingPolicy(new NamingPolicy() {
      @Override
      public String getClassName(final String prefix, final String source,
          final Object key, final Predicate names) {
        return pkg + ".vfs.Handler";
      }
    });
    e.setSuperclass(VFSURLStreamHandler.class);
    e.setCallbackType(NoOp.class);
    e.createClass();
  }

  /** */
  public static class VFSURLStreamHandler extends URLStreamHandler {

    /** */
    @Override
    public URLConnection openConnection(final URL u) throws IOException {
      return new URLConnection(u) {

        /**
         * @see java.net.URLConnection#connect()
         */
        @Override
        public void connect() throws IOException {
        }

        /**
         * @see java.net.URLConnection#getInputStream()
         */
        @Override
        public InputStream getInputStream() throws IOException {
          final String host = getURL().getHost();
          final VFS vfs = getInstance().active.get(host);
          if (vfs == null) {
            throw new IOException("Host '" + host
                + "' is not known in the registry.");
          }
          return vfs.find(getURL().getFile()).getInputStream();
        }

      };
    }

  }

}
