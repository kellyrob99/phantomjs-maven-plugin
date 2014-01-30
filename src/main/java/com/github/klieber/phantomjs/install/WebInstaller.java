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
package com.github.klieber.phantomjs.install;

import com.github.klieber.phantomjs.archive.PhantomJSArchive;
import com.github.klieber.phantomjs.archive.PhantomJSArchiveBuilder;
import com.github.klieber.phantomjs.cache.CachedFile;
import com.github.klieber.phantomjs.config.Configuration;
import com.github.klieber.phantomjs.download.Downloader;
import com.github.klieber.phantomjs.extract.Extractor;
import com.github.klieber.phantomjs.extract.PhantomJsExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class WebInstaller implements Installer {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebInstaller.class);

  private static final String UNABLE_TO_EXTRACT = "Unable to extract phantomjs binary from %s";
  private static final String UNABLE_TO_CREATE_DIRECTORY = "Unable to create directory: %s";
  private static final String EXTRACTING = "Extracting {} to {}s";

  private final CachedFile cachedFile;
  private final Downloader downloader;

  private final PhantomJSArchive phantomJSArchive;
  private final File outputDirectory;

  public WebInstaller(Configuration config, CachedFile cachedFile, Downloader downloader) {
    this.phantomJSArchive = config.getPhantomJsArchive();
    this.outputDirectory = config.getOutputDirectory();
    this.cachedFile = cachedFile;
    this.downloader = downloader;
  }

  @Override
  public String install() throws InstallationException {

    if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
      throw new InstallationException(String.format(UNABLE_TO_CREATE_DIRECTORY,outputDirectory));
    }

    File extractTo = new File(outputDirectory, phantomJSArchive.getExtractToPath());

    if (!extractTo.exists()) {
      File archive = cachedFile.getFile();

      if (!archive.exists()) {
        downloader.download(phantomJSArchive, archive);
      }

      Extractor extractor = new PhantomJsExtractor(extractTo,phantomJSArchive);
      extractor.extract(archive);
    }
    return extractTo.getAbsolutePath();
  }
}
