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
import com.github.klieber.phantomjs.download.Downloader;
import de.schlichtherle.truezip.file.TFile;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.repository.RepositorySystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class WebInstaller implements Installer {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebInstaller.class);

  private static final String UNABLE_TO_EXTRACT = "Unable to extract phantomjs binary from %s";
  private static final String UNABLE_TO_CREATE_DIRECTORY = "Unable to create directory: %s";
  private static final String EXTRACTING = "Extracting {} to {}s";

  private final Downloader downloader;
  private File outputDirectory;

  private ArtifactRepository localRepository;
  private RepositorySystem repositorySystem;

  private String version;

  public WebInstaller(Downloader downloader) {
    this.downloader = downloader;
  }

  @Override
  public String install() {
    return null;
  }

  private String installBinaryFromWeb() throws MojoExecutionException {

    if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
      throw new MojoExecutionException(String.format(UNABLE_TO_CREATE_DIRECTORY,outputDirectory));
    }

    PhantomJSArchive phantomJSFile = new PhantomJSArchiveBuilder(version).build();

    File extractTo = new File(outputDirectory, phantomJSFile.getExtractToPath());

    if (!extractTo.exists()) {
      Artifact localArtifact = repositorySystem.createArtifactWithClassifier(
          "org.phantomjs",
          "phantomjs",
          version,
          phantomJSFile.getExtension(),
          phantomJSFile.getClassifier());
      File localFile = new File(localRepository.getBasedir(), localRepository.pathOf(localArtifact));
      if (!localFile.exists()) {
        downloader.download(phantomJSFile, localFile);
      }

      try {
        TFile archive = new TFile(localFile, phantomJSFile.getPathToExecutable());

        LOGGER.info(EXTRACTING, archive.getAbsolutePath(), extractTo.getAbsolutePath());
        if (extractTo.getParentFile().mkdirs()) {
          archive.cp(extractTo);
          extractTo.setExecutable(true);
        }
      } catch (IOException e) {
        throw new MojoExecutionException(String.format(UNABLE_TO_EXTRACT, localFile), e);
      }
    }
    return extractTo.getAbsolutePath();
  }
}
