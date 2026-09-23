/* $Id: Files.java,v 1.11 2007/12/04 13:22:01 mke Exp $
 * $Revision: 1.11 $
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * General purpose methods to handle files
 * $Id: Files.java,v 1.11 2007/12/04 13:22:01 mke Exp $
 */
public class Files {
    private static final Logger log = LoggerFactory.getLogger(Files.class);

    // TODO: Add method for recursively copying directories (and just plain file copying)

    /**
     * Enumeration of the two different types of file objects; directories
     * and regular files.
     */
    public enum Type {
        /**
         * A file representing a directory
         */
        directory,

        /**
         * A file representing a regular file
         */
        file
    }

    /**
     * Enumeration of the standard file permission flags
     */
    public enum Permission {
        /**
         * The file is readable
         */
        readable,

        /**
         * The file is writable
         */
        writable,

        /**
         * The file may be run as an executable
         */
        executable
    }

    /**
     * Delete the file or directory given by <code>path</code> (recursively if
     * <code>path</code> is a directory).
     *
     * @param path a {@link File} representing the file or directory to be
     *             deleted.
     * @throws IOException if the path doesn't exist or could not be deleted.
     */
    public static void delete(File path) throws IOException {
        log.trace("delete(" + path + ") called");
        if (!path.exists()) {
            throw new FileNotFoundException(path.toString());
        }
        if (path.isFile()) {
            if (!path.delete()) {
                throw new IOException("Could not delete the file '" + path + "'");
            }
            return;
        }
        String[] list = path.list();
        assert list != null;
        for (String child : list) {
            delete(new File(path, child));
        }
        if (!path.delete()) {
            throw new IOException("Could not delete the folder '" + path + "'");
        }
    }

    /**
     * <p>Copy a file or directory (recursively) to a destination path. Generally
     * it behaves as a standard posix command line copy tool.</p>
     *
     * <p>If the input path is a directory and the destination path already
     * exists, the input directory will be copied as a subdir to the destination
     * directory.</p>
     *
     * <p>If the destination directory does not exist it will be created and the
     * contents of the input directory will be copied here.</p>
     *
     * <p>Note: The recursive copy is not transactional. If an error occurs halfway
     * through the copy, the already copied files will not be removed.</p>
     *
     * @param path      the file or directory to copy from.
     * @param toPath    the destination file or directory.
     * @param overwrite if false this method will throw a
     *                  {@link FileAlreadyExistsException} if the operation will
     *                  overwrite an existing file.
     * @throws IOException                if there was an error copying the file(s)
     * @throws FileAlreadyExistsException if {@code overwrite=false} and the
     *                                    method is about to overwrite an existing file.
     */
    public static void copy(File path, File toPath, boolean overwrite) throws IOException {
        log.trace("copy(" + path + ", " + toPath + ", " + overwrite + ")");
        if (path.isFile()) {
            copyFile(path, toPath, overwrite);
        } else if (path.isDirectory()) {
            if (!toPath.exists()) {
                copyDirectory(path, toPath, overwrite);
            } else {
                copyDirectory(path, new File(toPath, path.getName()), overwrite);
            }
        }
    }

    /**
     * <p>Move a file with the same semantics as the standard Unix {@code move}
     * command.</p>
     *
     * <p>In contrast to the standard Java {@link File#renameTo} this method
     * does extensive sanity checking and throws appropriate exceptions
     * if something is wrong.</p>
     *
     * <p>This method will cause quite a bit of {@code stat} dancing on the
     * file system, so don't use this method in performance critical regions.</p>
     *
     * <p>If {@code dest} is a directory, {@code source} will be moved there keeping
     * its base name.
     * If {@code dest} does not exist {@code source} will be renamed to
     * {@code dest}.</p>
     *
     * @param source    a writable file or directory
     * @param dest      either an existing writable directory, or non-existing file
     *                  with existing parent directory
     * @param overwrite if true and {@code dest} exists and is a regular
     *                  file it will be deleted before moving {@code source}
     *                  here
     * @throws FileNotFoundException      if either {@code source} or the parent
     *                                    directory of {@code dest} does not exist
     * @throws FileAlreadyExistsException if {@code dest} exists and is a
     *                                    regular file. If {@code overwrite}
     *                                    is {@code true} this exception will
     *                                    never be thrown
     * @throws FilePermissionException    if {@code source} or {@code dest} is not
     *                                    writable
     * @throws InvalidFileTypeException   if the parent of {@code dest} is a
     *                                    regular file
     * @throws IOException                if there is an unknown error during the move
     *                                    operation
     */
    public static void move(File source, File dest, boolean overwrite)
            throws IOException {
        if (source == null) {
            throw new NullPointerException("Move source location is null");
        }
        if (dest == null) {
            throw new NullPointerException("Move destination is null");
        }

        /* source checks */
        if (!source.exists()) {
            throw new FileNotFoundException(source.toString());
        }
        if (!source.canWrite()) {
            throw new FilePermissionException(source, Files.Permission.writable);
        }

        /* dest checks */
        File destParent = dest.getParentFile();
        if (dest.exists() && dest.isFile() && !overwrite) {
            throw new FileAlreadyExistsException(dest);
        }
        if (!destParent.exists()) {
            throw new FileNotFoundException("Parent directory of " + dest + " " + "does not exist");
        }
        if (destParent.isFile()) {
            throw new InvalidFileTypeException(destParent, Files.Type.file);
        }
        if (dest.isFile() && !destParent.canWrite()) {
            throw new FilePermissionException(destParent, Files.Permission.writable);
        }

        /* If dest is a dir, move the file into it, keeping the base name */
        if (dest.isDirectory()) {
            if (!dest.canWrite()) {
                throw new FilePermissionException(dest, Files.Permission.writable);
            }
            dest = new File(dest, source.getName());
        }

        if (dest.exists()) {
            if (!overwrite) {
                throw new FileAlreadyExistsException(dest);
            }
            log.trace("Overwriting " + dest);
            Files.delete(dest);
        }

        log.trace("Set to move " + source + " to " + dest);


        // On some platform File.renameTo fails on the first runs. See
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6213298
        boolean result = false;
        for (int i = 0; i < 10; i++) {
            result = source.renameTo(dest);
            if (result) {
                break;
            }
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // Abort the move operation
                break;
            }
            log.trace("Retrying move (" + i + ")");
        }

        if (!result) {
            log.debug("Atomic move failed. Falling back to copy/delete");
            copy(source, dest, overwrite);
            delete(source);
        }

        log.debug("Moved " + source + " to " + dest);
    }

    /**
     * Copy without clobbering (no overwrite if the destination exists).
     * As {@link #move(File, File, boolean)}, with {@code overwrite} set to {@code false}.
     *
     * @param source source file.
     * @param dest destination file.
     * @throws IOException if the file could not be copied.
     */
    public static void move(File source, File dest) throws IOException {
        move(source, dest, false);
    }

    /*
     * Used in recursive calls for copying directories. Prevents spawning of
     * nested subdirs. Otherwise behaves as {@link #copy}.
     */
    private static void innerCopy(File path, File toPath, boolean overwrite)
            throws IOException {
        if (path.isFile()) {
            copyFile(path, toPath, overwrite);
        } else if (path.isDirectory()) {
            copyDirectory(path, toPath, overwrite);
        }
    }

    private static void copyDirectory(File path, File toPath, boolean overwrite) throws IOException {
        log.trace("copyDirectory(" + path + ", " + toPath + ", " + overwrite
                  + ") called");
        if (!toPath.exists()) {
            if (!toPath.mkdirs()) {
                throw new IOException("Unable to create or verify the existence" + " of the destination folder '"
                                      + toPath.getAbsoluteFile() + "'");
            }
        }
        if (!toPath.canWrite()) {
            throw new IOException("The destination folder '" + toPath.getAbsoluteFile() + "' is not writable");
        }

        String[] list = path.list();
        if (list == null) {
            throw new IOException("Cannot list content of " + path + " (not a directory?)");
        }
        for (String filename : list) {
            File in = new File(path, filename);
            File out = new File(toPath, filename);
            innerCopy(in, out, overwrite);
        }
    }

    /**
     * Copies a file (not a folder).
     *
     * @param source      the file to copy.
     * @param destination where to copy the file to. If this is an existing
     *                    directory, {@code source} will be copied into it,
     *                    otherwise {@code source} will be copied to this file.
     * @param overwrite   whether to overwrite if the destination already exists.
     * @throws IOException                thrown if there was an error writing to the
     *                                    destination file, or if the input file doidn't exist
     *                                    or if the source was a directory.
     * @throws FileNotFoundException      thrown if the source file did not exist.
     * @throws FileAlreadyExistsException if there's already a file at
     *                                    {@code destination} and {@code overwrite} was
     *                                    {@code false}.
     */
    private static void copyFile(File source, File destination,
                                 boolean overwrite) throws IOException {
        log.trace("copyFile(" + source + ", " + destination + ", "
                  + overwrite + ") called");
        source = source.getAbsoluteFile();
        destination = destination.getAbsoluteFile();
        if (!source.exists()) {
            throw new FileNotFoundException("The source '" + source
                                            + "' does not exist");
        }
        if (destination.isDirectory()) {
            throw new IOException("The destination '" + destination
                                  + "' is a directory");
        }

        if (destination.exists() && destination.isDirectory()) {
            destination = new File(destination, source.getName());
        }

        if (!overwrite && destination.exists()) {
            throw new FileAlreadyExistsException(destination.toString());
        }

        // BufferedInputStream is not used, as it chokes > 2GB

        try (InputStream in = new FileInputStream(source); OutputStream out = new FileOutputStream(destination)) {
            byte[] buf = new byte[2028];
            int count;
            while ((count = in.read(buf)) != -1) {
                out.write(buf, 0, count);
            }
        }
        if (! destination.setExecutable(source.canExecute())) {
            log.error("Could not set executable " + source.canExecute() + " to " + destination);
        }
    }

    /**
     * Move the source file or directory recursively to the destination.
     * Generally it behaves as a standard posix command line copy tool,
     * with the addendum that files are moved by performing a complete copy
     * of all files followed by a delete.
     *
     * If the source is a directory and the destination already exists as a
     * directory, the source directory will be copied as a subdir to the
     * destination directory.
     *
     * If the destination directory does not exist it will be created and the
     * contents of the input directory will be copied here.
     *
     * Note: The recursive move is partly transactional. If an error occurs
     *       halfway through the move, the already copied files will not be
     *       removed, but no files from the source will be deleted.
     * @param source      the file or directory to copy from.
     * @param destination the destination file or directory.
     * @param overwrite   if false this method will throw a {@link FileAlreadyExistsException} if the operation
     *                    will overwrite an existing file.
     * @throws IOException if there was an error moving the file(s).
     * @throws FileAlreadyExistsException if {@code overwrite=false} and the method is about to overwrite an
     *                                    existing file.
     */
    /*public static void move(File source, File destination, boolean overwrite) throws IOException {
        copy(source, destination, overwrite);
        delete(source);
    }*/

    /**
     * @param path to file or directory to be deleted
     * @throws java.io.FileNotFoundException if the path doesn't exist
     * @see #delete(java.io.File)
     */
    public static void delete(String path) throws IOException {
        delete(new File(path));
    }

    /*
     * Fetches whatever a given URL points at and returns it as an UTF-8 string
     *
     * @param url The resource to fetch
     * @return The resource as an UTF-8 string
     * @throws IOException
     */
    /*   public static String getTextResource(URL url) throws IOException {
         URLConnection uc = url.openConnection();
         InputStream in = uc.getInputStream();
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         pipeStream(in, out);
         return out.toString("UTF-8");
     }
    */

    /**
     * Store a String on the file system, using UTF-8.
     *
     * @param content     the content to be stored on disk.
     * @param destination where to store the content.
     * @throws IOException if the content could not be stored.
     */
    public static void saveString(String content, File destination) throws
                                                                    IOException {
        log.trace("saveString(String with length " + content.length() + ", " + destination + ") called");
        if (destination.isDirectory()) {
            throw new IOException("The destination '" + destination + "' is a folder, while it should be a file");
        }
        InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        FileOutputStream out = new FileOutputStream(destination);
        Streams.pipe(in, out);
    }

    /**
     * Read a String from the file system, assuming UTF-8.
     *
     * @param source where to load the String.
     * @return the String as stored in the source.
     * @throws IOException if the String could not be read.
     */
    public static String loadString(File source) throws IOException {
        if (source.isDirectory()) {
            throw new IOException("The source '" + source + "' is a folder, while it should be a file");
        }
        InputStream in = new FileInputStream(source);
        ByteArrayOutputStream out = new ByteArrayOutputStream((int) source.length());
        Streams.pipe(in, out);
        return out.toString(StandardCharsets.UTF_8);
    }

    /**
     * Return the base name of a file.
     *
     * @param file the file to extract the base name for
     * @return file's basename
     * @deprecated use {@link File#getName()} instead.
     */
    @Deprecated
    public static String baseName(File file) {
        return file.getName();
    }

    /**
     * Return the base name of a file. For example
     * <code>
     * "autoexec.bat" = baseName ("C:\autoexec.bat")
     * "fstab" = basename ("/etc/fstab")
     * </code>
     *
     * @param filename the filename to extract the base name for.
     * @return file's basename.
     */
    public static String baseName(String filename)
    {
        return new File(filename).getName();
    }

    /**
     * <p>Download the contents of an {@link URL} and store it on disk.</p>
     *
     * <p>if {@code target} argument is a directory the file will be stored
     * here with the basename as extracted from the url. If it points to
     * a non-existing file it will be written to a file with that name.</p>
     *
     * <p>If the {@code target} argument points to an already existing file
     * and {@code overwrite == false} a {@link FileAlreadyExistsException}
     * will be thrown. Otherwise the file will be overwritten.</p>
     *
     * @param url       where the data should be downloaded from.
     * @param target    the place to store the downloaded data. This can be either a file or a directory.
     * @param overwrite whether or not to overwrite the target file if it already exist.
     * @return the resulting file.
     * @throws ConnectException     if there was an error opening a stream to the url.
     * @throws IOException          if there was an error downloading the file or writing it to disk.
     * @throws NullPointerException if one of the input arguments are null.
     */
    public static File download(URL url, File target, boolean overwrite)
            throws IOException {
        log.trace("download(" + url + ", " + target + ", " + overwrite + ") called");
        if (url == null) {
            throw new NullPointerException("url is null");
        }
        if (target == null) {
            throw new NullPointerException("target is null");
        }

        File result;

        if (target.isDirectory()) {
            result = new File(target, new File(url.getFile()).getName());
        } else {
            result = target;
        }

        if (result.exists() && !overwrite) {
            throw new FileAlreadyExistsException(target);
        }

        InputStream con;
        try {
            // No BufferedInputStream as it does not support 2GB+.
            con = url.openStream();
        } catch (IOException e) {
            throw new IOException("Failed to open stream to '" + url + "'", e);
        }
        OutputStream out = new FileOutputStream(result);
        Streams.pipe(con, out);
        return result;
    }

    /**
     * See {@link #download(java.net.URL, java.io.File, boolean)}. This method
     * invokes the detailed {@code download} method with {@code overwrite=false}
     *
     * @param url    URL pointing to the data to be downloaded
     * @param target the place to store the downloaded data. A file or directory.
     * @return the resulting file.
     * @throws IOException if there was an error downloading the file or
     *                     writing it to disk.
     */
    public static File download(URL url, File target) throws IOException {
        return download(url, target, false);
    }

}
