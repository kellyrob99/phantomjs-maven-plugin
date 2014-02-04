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
package com.github.klieber.phantomjs.extract;

import com.github.klieber.phantomjs.archive.PhantomJSArchive;
import de.schlichtherle.truezip.file.TFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PhantomJsExtractor.class, PhantomJSArchive.class, TFile.class})
public class PhantomJsExtractorTest {

  private static final String PATH_TO_EXECUTABLE = "/path/to/executable";
  private static final String ARCHIVE_PATH = "/path/to/archive";
  private static final String EXTRACT_TO_PATH = "/extract/to/path";
  @Mock
  private PhantomJSArchive phantomJsArchive;

  @Mock
  private File archive;

  @Mock
  private File extractTo;

  @Mock
  private File extractToParent;

  @Mock
  private TFile tfile;

  private PhantomJsExtractor extractor;


  @Before
  public void before() {
    extractor = new PhantomJsExtractor(phantomJsArchive);
  }

  @Test
  public void shouldExtract() throws Exception {
    when(phantomJsArchive.getPathToExecutable()).thenReturn(PATH_TO_EXECUTABLE);
    when(extractTo.getParentFile()).thenReturn(extractToParent);
    when(extractTo.getAbsolutePath()).thenReturn(EXTRACT_TO_PATH);
    when(extractToParent.mkdirs()).thenReturn(true);
    when(tfile.getAbsolutePath()).thenReturn(ARCHIVE_PATH);

    whenNew(TFile.class).withArguments(archive, PATH_TO_EXECUTABLE).thenReturn(tfile);

    extractor.extract(archive, extractTo);

    verify(tfile).cp(extractTo);
    verify(extractTo).setExecutable(true);
  }

  @Test
  public void shouldNotExtract() throws Exception {
    when(phantomJsArchive.getPathToExecutable()).thenReturn(PATH_TO_EXECUTABLE);
    when(extractTo.getParentFile()).thenReturn(extractToParent);
    when(extractTo.getAbsolutePath()).thenReturn(EXTRACT_TO_PATH);
    when(extractToParent.mkdirs()).thenReturn(false);
    when(tfile.getAbsolutePath()).thenReturn(ARCHIVE_PATH);

    whenNew(TFile.class).withArguments(archive, PATH_TO_EXECUTABLE).thenReturn(tfile);

    extractor.extract(archive, extractTo);

    verify(tfile,never()).cp(extractTo);
    verify(extractTo, never()).setExecutable(true);
  }

  @Test
  public void shouldFailToExtract() throws Exception {
    when(phantomJsArchive.getPathToExecutable()).thenReturn(PATH_TO_EXECUTABLE);
    when(extractTo.getParentFile()).thenReturn(extractToParent);
    when(extractTo.getAbsolutePath()).thenReturn(EXTRACT_TO_PATH);
    when(extractToParent.mkdirs()).thenReturn(true);
    when(tfile.getAbsolutePath()).thenReturn(ARCHIVE_PATH);

    when(tfile.cp(extractTo)).thenThrow(new IOException());

    whenNew(TFile.class).withArguments(archive, PATH_TO_EXECUTABLE).thenReturn(tfile);

    catchException(extractor).extract(archive, extractTo);

    assertThat(caughtException(), is(instanceOf(ExtractionException.class)));
  }
}
