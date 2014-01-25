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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Resolve location of PhantomJS on the system path.
 */
public class SystemPathPhantomJsResolver implements PhantomJsResolver {

  private static final String PHANTOMJS = "phantomjs";

  private static final String MSG_FOUND_PHANTOMJS = "Found phantomjs %s at %s";

  private final Log log;
  private final String version;
  private final boolean enforceVersion;

  public SystemPathPhantomJsResolver(Log log, String version) {
    this(log,version,true);
  }

  public SystemPathPhantomJsResolver(Log log, String version, boolean enforceVersion) {
    this.log = log;
    this.version = version;
    this.enforceVersion = enforceVersion;
  }

  @Override
  public String resolve() throws MojoExecutionException {
    String systemPath = System.getenv("PATH");
    String pathSeparator = System.getProperty("path.separator",":");
    String fileSeparator = System.getProperty("file.separator","/");
    String binary = null;
    for (String path : systemPath.split(pathSeparator)) {
      String absolutePath = path + fileSeparator + PHANTOMJS;
      if (FileUtils.fileExists(absolutePath)) {
        binary = absolutePath;
        String versionString = getVersion(binary);
        if (!enforceVersion || this.version.equals(versionString)) {
          log.info(String.format(MSG_FOUND_PHANTOMJS,versionString,binary))
          ;
          return binary;
        }
      }
    }
    return null;
  }

  private String getVersion(String binary) throws MojoExecutionException {
    Commandline commandline = new Commandline(binary);
    commandline.createArg().setValue("-v");
    try {
      Process process = new ProcessBuilder(commandline.getShellCommandline()).start();
      BufferedReader standardOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String versionString = StringUtils.trim(standardOut.readLine());
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new MojoExecutionException("Failed to check system path");
      }
      return versionString;
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to check system path",e);
    } catch (InterruptedException e) {
      throw new MojoExecutionException("Failed to check system path", e);
    }
  }
}
