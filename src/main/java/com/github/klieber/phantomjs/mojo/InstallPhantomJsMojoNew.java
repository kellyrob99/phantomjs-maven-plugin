/*
 * Copyright (c) 2013 Kyle Lieber
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.klieber.phantomjs.mojo;

import com.github.klieber.phantomjs.config.Configuration;
import com.github.klieber.phantomjs.locate.CompositeLocator;
import com.github.klieber.phantomjs.locate.Locator;
import com.github.klieber.phantomjs.locate.PathLocator;
import com.github.klieber.phantomjs.resolve.PhantomJsBinaryResolver;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.repository.RepositorySystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Maven plugin for downloading and installing phantomjs binaries.
 *
 * @since 0.1
 */
@Mojo(name = "install", defaultPhase = LifecyclePhase.PROCESS_TEST_SOURCES)
public class InstallPhantomJsMojoNew extends AbstractPhantomJsMojo implements Configuration {

  private static final String PHANTOMJS = "phantomjs";

  private static final ComparableVersion LEGACY_VERSION = new ComparableVersion("1.9.2");

  private static final String GOOGLE_CODE = "https://phantomjs.googlecode.com/files/";
  private static final String BITBUCKET = "http://cdn.bitbucket.org/ariya/phantomjs/downloads/";

  private static final String SYSTEM_CHECK_FAILURE = "Failed to check system path";
  private static final String UNABLE_TO_DOWNLOAD = "Unable to download phantomjs binary from %s";
  private static final String UNABLE_TO_EXTRACT = "Unable to extract phantomjs binary from %s";
  private static final String FOUND_PHANTOMJS = "Found phantomjs %s at %s";
  private static final String UNABLE_TO_CREATE_DIRECTORY = "Unable to create directory: %s";

  private static final String DOWNLOADING = "Downloading phantomjs binary from %s";
  private static final String EXTRACTING = "Extracting %s to %s";

  /**
   * The version of phantomjs to install.
   *
   * @since 0.1
   */
  @Parameter(
      property = "phantomjs.version",
      required = true
  )
  private String version;

  /**
   * The base url the phantomjs binary can be downloaded from.
   *
   * @since 0.1
   */
  @Parameter(
      property = "phantomjs.baseUrl"
  )
  private String baseUrl;

  /**
   * The directory the phantomjs binary should be installed.
   *
   * @since 0.1
   */
  @Parameter(
      defaultValue = "${project.build.directory}/phantomjs-maven-plugin",
      property = "phantomjs.outputDir",
      required = true
  )
  private File outputDirectory;

  /**
   * Check the system path for an existing phantomjs installation.
   *
   * @since 0.2
   */
  @Parameter(
      defaultValue = "false",
      property = "phantomjs.checkSystemPath",
      required = true
  )
  private boolean checkSystemPath;

  /**
   * Require that the correct version of phantomjs is on the system path.
   *
   * @since 0.2
   */
  @Parameter(
      defaultValue = "true",
      property = "phantomjs.enforceVersion",
      required = true
  )
  private boolean enforceVersion;

  @Parameter(
      defaultValue = "${localRepository}",
      readonly = true
  )
  private ArtifactRepository localRepository;

  @Component
  private RepositorySystem repositorySystem;


  @Override
  public String getVersion() {
    return this.version;
  }

  @Override
  public boolean enforceVersion() {
    return this.enforceVersion;
  }

  @Override
  public File getOutputDirectory() {
    return this.outputDirectory;
  }

  public void run() throws MojoExecutionException {
    List<Locator> locators = new ArrayList<Locator>();
    locators.add(getPathLocator());

    Locator locator = new CompositeLocator(locators);

    this.setPhantomJsBinary(locator.locate());
  }

  private Locator getPathLocator() {
    String systemPath = System.getenv("PATH");
    List<String> paths = Arrays.asList(systemPath.split(File.pathSeparator));
    return new PathLocator(new PhantomJsBinaryResolver(this),paths);
  }
}