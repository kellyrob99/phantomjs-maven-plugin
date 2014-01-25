/*
 * Copyright (c) 2014 Kyle Lieber
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
package com.github.klieber.phantomjs.resolver;

import com.github.klieber.phantomjs.archive.PhantomJSArchive;
import com.github.klieber.phantomjs.archive.PhantomJSArchiveBuilder;
import de.schlichtherle.truezip.file.TFile;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class WebPhantomJsResolver implements PhantomJsResolver {

  private final Log log;
  private final File outputDirectory;
  private final String baseUrl;
  private final String version;

  private static final ComparableVersion LEGACY_VERSION = new ComparableVersion("1.9.2");

  private static final String GOOGLE_CODE = "https://phantomjs.googlecode.com/files/";
  private static final String BITBUCKET = "http://cdn.bitbucket.org/ariya/phantomjs/downloads/";

  private static final String MSG_DOWNLOADING = "Downloading phantomjs binaries from %s";
  private static final String MSG_EXTRACTING = "Extracting %s to %s";
  private static final String MSG_UNABLE_TO_DOWNLOAD = "Unable to download phantomjs binary from %s";

  public WebPhantomJsResolver(Log log,
                              File outputDirectory,
                              String baseUrl,
                              String version) {
    this.log = log;
    this.outputDirectory = outputDirectory;
    this.baseUrl = baseUrl;
    this.version = version;
  }

  @Override
  public String resolve() throws MojoExecutionException {

    if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
      throw new MojoExecutionException("Unable to create directory: " + outputDirectory);
    }

    PhantomJSArchive phantomJSFile = new PhantomJSArchiveBuilder(version).build();

    File extractTo = new File(outputDirectory, phantomJSFile.getExtractToPath());

    if (!extractTo.exists()) {

      StringBuilder url = new StringBuilder();
      url.append(this.getBaseUrl());
      url.append(phantomJSFile.getArchiveName());

      try {
        URL downloadLocation = new URL(url.toString());

        log.info(String.format(MSG_DOWNLOADING, url));

        File outputFile = new File(outputDirectory, phantomJSFile.getArchiveName());
        FileUtils.copyURLToFile(downloadLocation, outputFile);

        if (outputFile.length() <= 0) {
          throw new MojoExecutionException(String.format(MSG_UNABLE_TO_DOWNLOAD, url));
        }
        TFile archive = new TFile(outputDirectory, phantomJSFile.getPathToExecutable());

        log.info(String.format(MSG_EXTRACTING, archive.getAbsolutePath(), extractTo.getAbsolutePath()));

        if (extractTo.getParentFile().mkdirs()) {
          archive.cp(extractTo);
          extractTo.setExecutable(true);
        }
      } catch (MalformedURLException e) {
        throw new MojoExecutionException(String.format(MSG_UNABLE_TO_DOWNLOAD, url), e);
      } catch (IOException e) {
        throw new MojoExecutionException(String.format(MSG_UNABLE_TO_DOWNLOAD, url), e);
      }
    }
    return extractTo.getAbsolutePath();
  }

  private String getBaseUrl() {
    String url = this.baseUrl;
    if (url == null) {
      url = LEGACY_VERSION.compareTo(new ComparableVersion(version)) >= 0 ? GOOGLE_CODE : BITBUCKET;
    }
    if (url != null && !url.endsWith("/")) {
      url += "/";
    }
    return url;
  }
}
