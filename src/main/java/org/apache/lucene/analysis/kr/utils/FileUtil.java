/*
 * Copyright 2011-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.analysis.kr.utils;

import org.apache.lucene.analysis.kr.morph.MorphException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * file utility class
 *
 * @author S.M.Lee
 */
public class FileUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    public static InputStream getResourceFileStream(String filename) {

        ClassLoader classLoader = FileUtil.class.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream(filename);
        if (stream == null) {
            stream = ClassLoader.getSystemResourceAsStream(filename);
        }
        return stream;
    }

    /**
     * Given a file name for a file that is located somewhere in the application
     * classpath, return a File object representing the file.
     *
     * @param filename The name of the file (relative to the classpath) that is
     *                 to be retrieved.
     * @return A file object representing the requested filename
     * @throws MorphException Thrown if the classloader can not be found or if
     *                        the file can not be found in the classpath.
     */
    public synchronized static File getClassLoaderFile(String filename) throws MorphException {
        // note that this method is used when initializing logging,
        // so it must not attempt to log anything.
        File file = null;
        ClassLoader loader = FileUtil.class.getClassLoader();
        URL url = loader.getResource(filename);
        if (url == null) {
            url = ClassLoader.getSystemResource(filename);
            if (url == null) {
                throw new MorphException("Unable to find " + filename);
            }
            file = toFile(url);
        } else {
            file = toFile(url);
        }
        if (file == null || !file.exists()) {
            return null;
        }
        return file;
    }

    /**
     * Reads the contents of a file line by line to a List of Strings.
     * The file is always closed.
     *
     * @param file     the file to read, must not be <code>null</code>
     * @param encoding the encoding to use, <code>null</code> means platform default
     * @return the list of Strings representing each line in the file, never <code>null</code>
     * @throws java.io.IOException in case of an I/O error
     * @throws java.io.UnsupportedEncodingException
     *                             if the encoding is not supported by the VM
     * @since Commons IO 1.1
     */
    public static List<String> readLines(File file, String encoding) throws IOException {
        if (log.isDebugEnabled())
            log.debug("파일 내용을 읽어드립니다. fName=[{}], encoding=[{}]", file, encoding);

        return Files.readAllLines(Paths.get(file.toURI()), Charset.forName(encoding));
    }

    /**
     * Reads the contents of a file line by line to a List of Strings.
     * The file is always closed.
     *
     * @param fName    the file to read, must not be <code>null</code>
     * @param encoding the encoding to use, <code>null</code> means platform default
     * @return the list of Strings representing each line in the file, never <code>null</code>
     * @throws org.apache.lucene.analysis.kr.morph.MorphException
     *
     * @throws java.io.IOException
     * @throws java.io.UnsupportedEncodingException
     *                             if the encoding is not supported by the VM
     * @since Commons IO 1.1
     */
    public static List<String> readLines(String fName, String encoding) {
        if (log.isDebugEnabled())
            log.debug("파일 내용을 읽어드립니다. fName=[{}], encoding=[{}]", fName, encoding);

        try {
            return FileUtil.readLines(getResourceFileStream(fName), encoding);
        } catch (Exception e) {
            log.error("파일 내용을 읽는데 실패했습니다. fName=" + fName, e);
            throw new RuntimeException(e);
        }
    }

    public static List<String> readLines(String fName, Charset charset) {
        if (log.isDebugEnabled())
            log.debug("파일 내용을 읽어드립니다. fName=[{}], charset=[{}]", fName, charset);

        try {
            return FileUtil.readLines(getResourceFileStream(fName), charset);
        } catch (Exception e) {
            log.error("파일 내용을 읽는데 실패했습니다. fName=" + fName, e);
            throw new RuntimeException(e);
        }
    }

    public static Future<List<String>> readLinesAsync(final String fName, final Charset charset) {
        if (log.isDebugEnabled())
            log.debug("파일 내용을 읽어드립니다. fName=[{}], charset=[{}]", fName, charset);

        FutureTask<List<String>> futureTask = new FutureTask<List<String>>(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                InputStream in = getResourceFileStream(fName);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset))) {
                    List<String> result = new ArrayList<String>();
                    for (; ; ) {
                        String line = reader.readLine();
                        if (line == null)
                            break;
                        result.add(line);
                    }
                    return result;
                }
            }
        });
        futureTask.run();
        return futureTask;
    }

    //-----------------------------------------------------------------------

    /**
     * Opens a {@link java.io.FileInputStream} for the specified file, providing better
     * error messages than simply calling <code>new FileInputStream(file)</code>.
     * <p/>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * <p/>
     * An exception is thrown if the file does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be read.
     *
     * @param file the file to open for input, must not be <code>null</code>
     * @return a new {@link java.io.FileInputStream} for the specified file
     * @throws java.io.FileNotFoundException if the file does not exist
     * @throws java.io.IOException           if the file object is a directory, if the file cannot be read
     * @since Commons IO 1.3
     */
    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canRead()) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    // readLines
    //-----------------------------------------------------------------------

    /**
     * Get the contents of an <code>InputStream</code> as a list of Strings,
     * one entry per line, using the default character encoding of the platform.
     * <p/>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input the <code>InputStream</code> to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws java.io.IOException  if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static List<String> readLines(InputStream input) throws IOException {
        InputStreamReader reader = new InputStreamReader(input);
        return readLines(reader);
    }

    /**
     * Get the contents of an <code>InputStream</code> as a list of Strings,
     * one entry per line, using the specified character encoding.
     * <p/>
     * Character encoding names can be found at
     * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
     * <p/>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input    the <code>InputStream</code> to read from, not null
     * @param encoding the encoding to use, null means platform default
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws java.io.IOException  if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static List<String> readLines(InputStream input, String encoding) throws IOException {
        if (encoding == null) {
            return readLines(input);
        } else {
            InputStreamReader reader = new InputStreamReader(input, encoding);
            return readLines(reader);
        }
    }

    public static List<String> readLines(InputStream input, Charset charset) throws IOException {
        if (charset == null) {
            charset = KoreanEnv.UTF8;
        }
        InputStreamReader reader = new InputStreamReader(input, charset);
        return readLines(reader);
    }


    /**
     * Get the contents of a <code>Reader</code> as a list of Strings,
     * one entry per line.
     * <p/>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedReader</code>.
     *
     * @param input the <code>Reader</code> to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws java.io.IOException  if an I/O error occurs
     * @since Commons IO 1.1
     */
    public static List<String> readLines(Reader input) throws IOException {
        try (BufferedReader reader = new BufferedReader(input)) {
            List<String> lines = new ArrayList<String>(1000);

            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
    }

    /**
     * Unconditionally close an <code>InputStream</code>.
     * <p/>
     * Equivalent to {@link java.io.InputStream#close()}, except any exceptions will be ignored.
     * This is typically used in finally blocks.
     *
     * @param input the InputStream to close, may be null or already closed
     */
    public static void closeQuietly(InputStream input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ignored) { }
    }


    //-----------------------------------------------------------------------

    /**
     * Convert from a <code>URL</code> to a <code>File</code>.
     * <p/>
     * From version 1.1 this method will decode the URL.
     * Syntax such as <code>file:///my%20docs/file.txt</code> will be
     * correctly decoded to <code>/my docs/file.txt</code>.
     *
     * @param url the file URL to convert, <code>null</code> returns <code>null</code>
     * @return the equivalent <code>File</code> object, or <code>null</code>
     *         if the URL's protocol is not <code>file</code>
     * @throws IllegalArgumentException if the file is incorrectly encoded
     */
    public static File toFile(URL url) {
        if (url == null || !url.getProtocol().equals("file")) {
            return null;
        } else {
            String filename = url.getFile().replace('/', File.separatorChar);
            int pos = 0;
            while ((pos = filename.indexOf('%', pos)) >= 0) {
                if (pos + 2 < filename.length()) {
                    String hexStr = filename.substring(pos + 1, pos + 3);
                    char ch = (char) Integer.parseInt(hexStr, 16);
                    filename = filename.substring(0, pos) + ch + filename.substring(pos + 3);
                }
            }
            return new File(filename);
        }
    }


    //-----------------------------------------------------------------------

    /**
     * Reads the contents of a file into a String.
     * The file is always closed.
     *
     * @param file     the file to read, must not be <code>null</code>
     * @param encoding the encoding to use, <code>null</code> means platform default
     * @return the file contents, never <code>null</code>
     * @throws java.io.IOException in case of an I/O error
     * @throws java.io.UnsupportedEncodingException
     *                             if the encoding is not supported by the VM
     */
    public static String readFileToString(File file, String encoding) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return StringUtil.toString(in, encoding);
        } finally {
            closeQuietly(in);
        }
    }


    public static byte[] readByteFromCurrentJar(String resource) throws MorphException {

        String jarPath = FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        JarResources jar = new JarResources(jarPath);
        try {
            return jar.getResource(resource);
        } catch (Exception e) {
            throw new MorphException(e.getMessage(), e);
        }
    }

}
