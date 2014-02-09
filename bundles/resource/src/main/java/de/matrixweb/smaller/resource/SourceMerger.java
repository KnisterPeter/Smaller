package de.matrixweb.smaller.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.matrixweb.vfs.VFS;
import de.matrixweb.vfs.VFSUtils;
import de.matrixweb.vfs.VFile;

/**
 * @author marwol
 */
public class SourceMerger {

  private final boolean uniqueFiles;

  /**
   * All source files are merged regardless of multiple occurrences.
   */
  public SourceMerger() {
    this(false);
  }

  /**
   * @param uniqueFiles
   *          flag to resolve a json source files just once
   */
  public SourceMerger(final boolean uniqueFiles) {
    this.uniqueFiles = uniqueFiles;
  }

  /**
   * @param resolver
   *          The {@link ResourceResolver} to lookup sources
   * @param in
   *          The list of input files
   * @return Returns the merged result
   * @throws IOException
   * @deprecated
   */
  @Deprecated
  public String merge(final ResourceResolver resolver, final List<String> in)
      throws IOException {
    return merge(getResources(resolver, in));
  }

  /**
   * @param resolver
   *          The {@link ResourceResolver} to lookup sources
   * @param in
   *          The list of input files
   * @return Returns a {@link List} of resolved {@link Resource}s
   * @throws IOException
   * @deprecated
   */
  @Deprecated
  public List<Resource> getResources(final ResourceResolver resolver,
      final List<String> in) throws IOException {
    return getSourceFiles(resolver, in);
  }

  /**
   * @param resources
   *          The {@link Resource}s to merge
   * @return Returns the merged resources
   * @throws IOException
   */
  public String merge(final List<Resource> resources) throws IOException {
    return this.merge(resources, "\n");
  }

  private String merge(final List<Resource> resources, final String separator)
      throws IOException {
    final List<String> contents = new ArrayList<String>();
    for (final Resource resource : resources) {
      contents.add(resource.getContents());
    }
    return StringUtils.join(contents, separator);
  }

  /**
   * Returns a merged temporary file with all contents listed in the given json
   * file paths.
   * 
   * @param vfs
   *          The {@link VFS} to use
   * @param resolver
   *          The {@link ResourceResolver} to use
   * @param in
   *          The input json file
   * @return Returns a file path into the {@link VFS} to the temp file
   * @throws IOException
   */
  public String getMergedJsonFile(final VFS vfs,
      final ResourceResolver resolver, final String in) throws IOException {
    final VFile file = vfs.find("/__temp__json__input");
    final List<Resource> resources = getJsonSourceFiles(resolver.resolve(in));

    // Hack which tries to replace all non-js sources with js sources in case of
    // mixed json-input file
    boolean foundJs = false;
    boolean foundNonJs = false;
    for (final Resource resource : resources) {
      foundJs |= FilenameUtils.isExtension(resource.getPath(), "js");
      foundNonJs |= !FilenameUtils.isExtension(resource.getPath(), "js");
    }
    if (foundJs && foundNonJs) {
      for (int i = 0, n = resources.size(); i < n; i++) {
        final Resource resource = resources.get(i);
        if (!FilenameUtils.isExtension(resource.getPath(), "js")) {
          final Resource jsResource = resource.getResolver().resolve(
              FilenameUtils.getName(FilenameUtils.removeExtension(resource
                  .getPath()) + ".js"));
          resources.add(resources.indexOf(resource), jsResource);
          resources.remove(resource);
        }
      }
    }

    VFSUtils.write(file, merge(resources));
    return file.getPath();
  }

  @Deprecated
  private List<Resource> getSourceFiles(final ResourceResolver resolver,
      final List<String> in) throws IOException {
    final List<Resource> inputs = new ArrayList<Resource>();
    for (final String s : in) {
      final Resource resource = resolver.resolve(s);
      if (resource.getPath().endsWith("json")) {
        inputs.addAll(getJsonSourceFiles(resource.getResolver(), resource));
      } else {
        inputs.add(resource);
      }
    }
    return inputs;
  }

  private List<Resource> getJsonSourceFiles(final Resource resource)
      throws IOException {
    final List<Resource> list = new ArrayList<Resource>();
    final Set<String> alreadyHandled = new HashSet<String>();
    for (final String s : new ObjectMapper().readValue(resource.getContents(),
        String[].class)) {
      if (!isUniqueFileResolved(alreadyHandled, s)) {
        list.add(resource.getResolver().resolve(s));
        alreadyHandled.add(s);
      }
    }
    return list;
  }

  @Deprecated
  private List<Resource> getJsonSourceFiles(final ResourceResolver resolver,
      final Resource resource) throws IOException {
    final List<Resource> list = new ArrayList<Resource>();
    final Set<String> alreadyHandled = new HashSet<String>();
    for (final String s : new ObjectMapper().readValue(resource.getContents(),
        String[].class)) {
      if (!isUniqueFileResolved(alreadyHandled, s)) {
        list.add(resolver.resolve(s));
        alreadyHandled.add(s);
      }
    }
    return list;
  }

  /**
   * Examines a source file whether it is already resolved when it should be
   * unique.
   * 
   * @param alreadyHandled
   *          all source files already resolved
   * @param s
   *          source file to resolve
   * @return true if source files should be unique and the source file already
   *         resolved
   */
  private boolean isUniqueFileResolved(final Set<String> alreadyHandled,
      final String s) {
    return this.uniqueFiles && alreadyHandled.contains(s);
  }

}
