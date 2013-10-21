package de.matrixweb.smaller.common;

import java.util.Arrays;
import java.util.List;

/**
 * @author markusw
 */
public enum Version {

  /** */
  UNDEFINED(0, 0, 0),
  /** */
  _1_0_0(1, 0, 0);

  /**
   * When used in http this is the header name for the version.
   */
  public static final String HEADER = "X-Smaller-Spec-Version";

  private int major;

  private int minor;

  private int patch;

  private Version(final int major, final int minor, final int patch) {
    this.major = major;
    this.minor = minor;
    this.patch = patch;
  }

  /**
   * @return Returns the latest spec version
   */
  public static Version getCurrentVersion() {
    return _1_0_0;
  }

  /**
   * @param str
   * @return Return the given str as {@link Version} of {@link #UNDEFINED} if
   *         not parsable.
   */
  public static Version getVersion(final String str) {
    try {
      final String[] parts = str.split("\\.");
      final String ver = "_" + parts[0] + "_" + parts[1] + "_" + parts[2];
      return valueOf(ver);
    } catch (final Exception e) {
      // In any error case return undefined
    }
    return UNDEFINED;
  }

  /**
   * @param version
   *          The {@link Version} to compare this to
   * @return Returns true if this {@link Version} is at least the given
   *         {@link Version}, false otherwise
   */
  public boolean isAtLeast(final Version version) {
    final List<Version> versions = Arrays.asList(Version.values());
    return versions.indexOf(this) >= versions.indexOf(version);
  }

  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return this.major + "." + this.minor + "." + this.patch;
  }

}
