package de.matrixweb.smaller.osgi.maven.impl;

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

  /** */
  class CompoundFilter implements Filter {

    private final List<Filter> filters;

    /**
     * @param filters
     */
    public CompoundFilter(final Filter... filters) {
      this.filters = Arrays.asList(filters);
    }

    /**
     * @see de.matrixweb.smaller.osgi.maven.impl.Filter#accept(de.matrixweb.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(final Pom pom) {
      boolean accept = true;
      for (final Filter filter : filters) {
        accept &= filter.accept(pom);
      }
      return accept;
    }

  }

  /** */
  class AcceptAll implements Filter {

    /**
     * @see de.matrixweb.smaller.osgi.maven.impl.Filter#accept(de.matrixweb.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(final Pom pom) {
      return true;
    }

  }

  /** */
  class AcceptScopes implements Filter {

    private final List<String> scopes;

    /**
     * @param scopes
     */
    public AcceptScopes(final String... scopes) {
      this.scopes = Arrays.asList(scopes);
    }

    /**
     * @see de.matrixweb.smaller.osgi.maven.impl.Filter#accept(de.matrixweb.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(final Pom pom) {
      return scopes.contains(pom.getScope());
    }

  }

  /** */
  class AcceptTypes implements Filter {

    private final List<String> types;

    /**
     * @param types
     */
    public AcceptTypes(final String... types) {
      this.types = Arrays.asList(types);
    }

    /**
     * @see de.matrixweb.smaller.osgi.maven.impl.Filter#accept(de.matrixweb.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(final Pom pom) {
      return types.contains(pom.getType());
    }

  }

  /** */
  class NotAcceptTypes implements Filter {

    private final List<String> types;

    /**
     * @param types
     */
    public NotAcceptTypes(final String... types) {
      this.types = Arrays.asList(types);
    }

    /**
     * @see de.matrixweb.smaller.osgi.maven.impl.Filter#accept(de.matrixweb.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(final Pom pom) {
      return !types.contains(pom.getType());
    }

  }

  /** */
  class AcceptOptional implements Filter {

    private final Boolean optional;

    /**
     * @param optional
     */
    public AcceptOptional(final boolean optional) {
      this.optional = optional;
    }

    /**
     * @see de.matrixweb.smaller.osgi.maven.impl.Filter#accept(de.matrixweb.smaller.osgi.maven.impl.Pom)
     */
    @Override
    public boolean accept(final Pom pom) {
      return optional.equals(pom.isOptional());
    }

  }

}
