/* $Id: FilesTest.java,v 1.9 2007/12/04 13:22:01 mke Exp $
 * $Revision: 1.9 $
 * $Date: 2007/12/04 13:22:01 $
 * $Author: mke $
 *
 * The SB Util Library.
 * Copyright (C) 2005-2007  The State and University Library of Denmark
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dk.kb.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"DuplicateStringLiteralInspection"})
public class FilesTest {

    String inputDir; // build dir for the kb-util installation is used for test input
    String inputFile; // test input file
    @TempDir
    File tmpDir; // tmp dir for the kb-util installation
    String outputFile; // test output zip file

    File sub1;
    File sub2;
    File subFile1;
    File subFile2;

    @BeforeEach
    public void setUp() throws Exception {

        URL placeholder = Thread.currentThread().getContextClassLoader().getResource("textfile.txt");
        assertNotNull(placeholder);
        File resources = new File(placeholder.toURI()).getParentFile();
        File myFolder = resources.getParentFile().getParentFile();
        File rootProject = resources.getParentFile().getParentFile().getParentFile();

        inputDir = myFolder.getAbsolutePath() + File.separator + "target";
        inputFile = rootProject.getAbsolutePath()+ File.separator + "README.md";
        outputFile = tmpDir + File.separator + "test";

        sub1 = new File(tmpDir, "sub1");
        sub2 = new File(sub1, "sub2");
        subFile1 = new File(sub2, "foo.bar");
        subFile2 = new File(sub2, "baz.bar");

        // Make test targets
        assertTrue(sub1.mkdirs());
        assertTrue(sub2.mkdirs());
        subFile1.createNewFile();
        subFile2.createNewFile();
    }

    @Test
    public void testDeleteFile() throws Exception {
        Files.copy(new File(inputDir), tmpDir, true);
        Files.delete(tmpDir);
    }

    @Test
    public void testDeleteFileByName() throws Exception {
        Files.copy(new File(inputDir), tmpDir, true);
        Files.delete(tmpDir.getAbsolutePath());
    }

    @Test
    public void testCopyDir() throws Exception {
        Files.copy(new File(inputDir), tmpDir, true);
    }

    @Test
    public void testNoOverwrite() throws Exception {
        Files.copy(new File(inputDir), tmpDir, true);
        assertThrows(FileAlreadyExistsException.class, () -> Files.copy(new File(inputDir), tmpDir, false),
                "Overwriting with overwrite=false should throw an exception");
    }


    //FIXME This test has been disabled because we can't test for equality of zip contents via the checksums. The timestamps may differ.
    /*
    public void testIntegrity () throws Exception {
        File altTmp = new File("tmp_alt");

        String origZip = new File(altTmp, "orig.zip").toString();
        String copyZip = new File(altTmp, "copy.zip").toString();

        Files.copy(new File(inputDir), new File(tmpDir), true);
        Zips.zip(new File(tmpDir, "classes").toString(), copyZip, true);
        Zips.zip(new File(inputDir).toString(), origZip, true);

        assertTrue("Checksums of zipped original and copy should match",
                        Arrays.equals( Checksums.md5(new File(origZip)), Checksums.md5(new File(copyZip))) );

    } */

    @Test
    public void testBytesToString() throws Exception {
        String aString = "This is a string ���";
        byte[] someBytes = aString.getBytes(StandardCharsets.UTF_8);
        String afterConversion = new String(someBytes, StandardCharsets.UTF_8);
        assertEquals(aString, afterConversion,
                     "The strings should be equal before and after conversion");

    }

    @Test
    public void testSaveString() throws Exception {
        File tempDir =
                new File(System.getProperties().getProperty("java.io.tmpdir"));
        assertTrue(tempDir.isDirectory(),
                   "The temp dir should be a dir");
        String testString = "Hello darkness my old friend";
        File testFile = new File(tempDir, "Sound.silence.tmp");
        Files.saveString(testString, testFile);
        assertTrue(testFile.exists(),
                   "The test file should exist");

        String loadedString = Files.loadString(testFile);
        assertEquals(testString, loadedString,
                     "The string should be the same before and after storage");
    }

    @Test
    public void testSaveStringMultiple(@TempDir Path tempDir) throws Exception {
        File tempFile = tempDir.resolve("foo-bar").toFile();
        int ITERATIONS = 5000;
        for (int i = 0; i < ITERATIONS; i++) {
            String content = "Zoo" + i;
            Files.saveString(content, tempFile);
            assertEquals(content, Files.loadString(tempFile),
                         "The string should be the same before and after storage #" + i);
        }
    }

    @Deprecated
    @Test
    public void testBaseNameOfDir() {
        File f = new File("uga" + File.separator + "buga");
        assertEquals("buga", Files.baseName(f));
    }

    @Deprecated
    @Test
    public void testBaseNameNoSep() {
        File f = new File("uga");
        assertEquals("uga", Files.baseName(f));
    }

    @Test
    public void testMoveToFolder() throws Exception {
        File destination = new File(tmpDir, "destination");
        assertTrue(destination.mkdirs());

        Files.move(sub1, destination, true);
        assertFalse(sub1.exists(),
                    "The folder '" + sub1 + "' should be deleted");
        File shouldExist = new File(destination, "sub1/sub2/foo.bar");
        assertTrue(shouldExist.exists(),
                   "The folder '" + shouldExist.getAbsoluteFile() + "' should be created");
    }

    @Test
    public void testMoveToFolderFail() throws Exception {
        File destination = new File(tmpDir, "destination");
        assertTrue(destination.mkdirs());
        assertTrue(new File(destination, "sub1/sub2").mkdirs());
        assertTrue(new File(destination, "sub1/sub2/foo.bar").mkdirs());

        assertThrows(IOException.class, () -> Files.move(sub1, destination),
                "Moving '" + sub1 + "' should fail");

        assertTrue(sub1.exists(),
                   "The file '" + subFile1 + "' should still exist");
    }

    @Test
    public void testMoveToNonExistingFolder() throws Exception {
        File destination = new File(tmpDir, "destination");

        Files.move(sub1, destination, true);
        assertFalse(sub1.exists(),
                    "The folder '" + sub1 + "' should be deleted");
        File shouldExist = new File(destination, "sub2/foo.bar");
        assertTrue(shouldExist.exists(),
                   "The folder '" + shouldExist.getAbsoluteFile() + "' should be created");
    }

    @Test
    public void testMoveFileToFile() throws Exception {
        File destination = new File(tmpDir, "destination");
        Files.move(subFile1, destination, true);
        assertFalse(subFile1.exists(),
                    "The source '" + subFile1 + "' should now be deleted");
        assertTrue(destination.exists(),
                   "The file '" + destination + "' should exist");
    }

    @Test
    public void testMoveFileToExistingFileOverwrite() throws Exception {
        File destination = new File(tmpDir, "destination");
        destination.createNewFile();
        Files.move(subFile1, destination, true);
        assertFalse(subFile1.exists(),
                    "The source '" + subFile1 + "' should now be deleted");
        assertTrue(destination.exists(),
                   "The file '" + destination + "' should exist");
    }

    @Test
    public void testMoveFileToExistingFileNotOverwrite() throws Exception {
        File destination = new File(tmpDir, "destination");
        destination.createNewFile();
        try {
            Files.move(subFile1, destination, false);
            fail("The moving of '" + subFile1 + "' to '" + destination + "' should fail");
        } catch (FileAlreadyExistsException e) {
            // Expected
        }
        assertTrue(subFile1.exists(),
                   "The source '" + subFile1 + "' should still exist");
    }

    @Test
    public void testCopyFileOverwrite() throws Exception {
        try {
            Files.copy(subFile1, subFile2, false);
            fail("Should not allow overwriting of exisiting file");
        } catch (FileAlreadyExistsException e) {
            // Expected
        }
    }

    @Test
    public void testDownloadSuccess() throws Exception {
        File result =
                Files.download(new File(inputFile).toURI().toURL(), tmpDir);

        String expectedFilename =
                new File(tmpDir, Files.baseName(inputFile)).toString();
        assertEquals(result.toString(), expectedFilename,
                     "Returned file should be in provided dir");

        assertArrayEquals(Checksums.md5(result),
                Checksums.md5(new File(expectedFilename)),
                "Checksums of source and downloaded file should match");
    }

    @Test
    public void testDownloadNoOverwrite() throws Exception {
        URL source = new File(inputFile).toURI().toURL();
        Files.download(source, tmpDir);

        try {
            Files.download(source, tmpDir);
        } catch (FileAlreadyExistsException e) {
            // this is the success criteria
            return;
        }
        fail("Should throw a FileAlreadyExistsException on overwrite");
    }
}


