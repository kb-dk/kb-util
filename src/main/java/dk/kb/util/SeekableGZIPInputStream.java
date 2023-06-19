/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package dk.kb.util;

import org.apache.commons.io.input.RandomAccessFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Random;
import java.util.zip.GZIPInputStream;

/**
 * Highly specialized InputStream that operates on Gzipped files with multiple blocks such as WARCs.
 */
public class SeekableGZIPInputStream extends InputStream {
    private static final Logger log = LoggerFactory.getLogger(SeekableGZIPInputStream.class);

    private final RandomAccessFile raf;
    private RandomAccessFileInputStream rafis;
    private GZIPInputStream gis = null;

    public SeekableGZIPInputStream(File gzipFile) throws IOException {
        if (!gzipFile.exists()) {
            throw new FileNotFoundException("Unable to locate '" + gzipFile + "'");
        }
        if (!gzipFile.canRead()) {
            throw new IOException("Cannot read '" + gzipFile + "'");
        }
        raf = new RandomAccessFile(gzipFile, "r");
    }

    /**
     * Seek to the given position.
     * The bytes at the given position must be the beginning of a valid GZIP-block.
     * @param pos the position in the file, measured in bytes.
     * @throws IOException if -1 is given or an IO error occurred.
     */
    public void seek(long pos) throws IOException {
        if (gis != null) {
            rafis.close();
            rafis = null;
            gis = null;
        }
        raf.seek(pos);
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException(
                "Mark is unreliably due to the buffered nature of GZIP streams. Use seek(long) instead");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() throws IOException {
        ensureStream();
        return gis.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        ensureStream();
        return gis.read(b, off, len);
    }

    /**
     * If a GZIPInputStream is available it is constructed from the current position in the underlying
     * {@link RandomAccessFile}. If already available nothing is done.
     */
    private void ensureStream() throws IOException {
        if (gis == null) {
            rafis = new RandomAccessFileInputStream(raf, false);
            gis = new GZIPInputStream(rafis);
        }
    }

}
