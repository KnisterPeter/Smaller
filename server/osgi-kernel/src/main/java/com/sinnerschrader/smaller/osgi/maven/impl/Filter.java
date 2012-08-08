package com.sinnerschrader.smaller.osgi.maven.impl;

import java.util.Arrays;
import java.util.List;

/**
 * @author markusw
 */
public interface Filter {

  /**
   * @param pom
   *          The {@link Pom} to apply the filter on
   * @return Returns true if the given {@link Pom} should be included, false
   *         otherwise.
   */
  boolean accept(Pom pom);

  public static class CompoundFilter implements Filter {

    private List<Filter> filters;

    public CompoundFilter(Filter... filters) {
      this.filters = Arrays.asList(filters);
    }

    /**
     * @see com.sinnerschrader.smaller.osgi.maven.impl.Filter#accept(com.sinnerschrader.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(Pom pom) {
      boolean accept = true;
      for (Filter filter : filters) {
        accept &= filter.accept(pom);
      }
      return accept;
    }

  }

  /** */
  public static class AcceptAll implements Filter {

    /**
     * @see com.sinnerschrader.smaller.osgi.maven.impl.Filter#accept(com.sinnerschrader.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(Pom pom) {
      return true;
    }

  }

  /** */
  public static class AcceptScopes implements Filter {

    private List<String> scopes;

    public AcceptScopes(String... scopes) {
      this.scopes = Arrays.asList(scopes);
    }

    /**
     * @see com.sinnerschrader.smaller.osgi.maven.impl.Filter#accept(com.sinnerschrader.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(Pom pom) {
      return scopes.contains(pom.getScope());
    }

  }

  /** */
  public static class AcceptTypes implements Filter {

    private List<String> types;

    public AcceptTypes(String... types) {
      this.types = Arrays.asList(types);
    }
    
    /**
     * @see com.sinnerschrader.smaller.osgi.maven.impl.Filter#accept(com.sinnerschrader.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(Pom pom) {
      return types.contains(pom.getType());
    }

  }

  /** */
  public static class NotAcceptTypes implements Filter {

    private List<String> types;

    public NotAcceptTypes(String... types) {
      this.types = Arrays.asList(types);
    }
    
    /**
     * @see com.sinnerschrader.smaller.osgi.maven.impl.Filter#accept(com.sinnerschrader.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(Pom pom) {
      return !types.contains(pom.getType());
    }

  }

  /** */
  public static class AcceptOptional implements Filter {

    private Boolean optional;

    public AcceptOptional(boolean optional) {
      this.optional = optional;
    }
    
    /**
     * @see com.sinnerschrader.smaller.osgi.maven.impl.Filter#accept(com.sinnerschrader.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(Pom pom) {
      return optional.equals(pom.isOptional());
    }

  }

}
