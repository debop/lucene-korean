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

import lombok.Cleanup;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * JarResources: JarResources maps all resources included in a
 * Zip or Jar file. Additionaly, it provides a method to extract one
 * as a blob.
 */
public final class JarResources {

    // external debug flag
    public boolean debugOn = false;

    // jar resource mapping tables
    private Hashtable htSizes = new Hashtable();

    // a jar file
    private String jarFileName;

    /**
     * creates a JarResources. It extracts all resources from a Jar
     * into an internal hashtable, keyed by resource names.
     *
     * @param jarFileName a jar or zip file
     */
    public JarResources(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    /**
     * Extracts a jar resource as a blob.
     *
     * @param name a resource name.
     */
    public byte[] getResource(String name) {
        return read(name);
    }

    /** initializes internal hash tables with Jar file resources. */
    private byte[] read(String name) {
        try {
            // extracts just sizes only.
            ZipFile zf = new ZipFile(jarFileName);
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                if (debugOn) {
                    System.out.println(dumpZipEntry(ze));
                }
                htSizes.put(ze.getName(), Integer.valueOf((int) ze.getSize()));
            }
            zf.close();

            // extract resources and put them into the hashtable.
            @Cleanup FileInputStream fis = new FileInputStream(jarFileName);
            @Cleanup BufferedInputStream bis = new BufferedInputStream(fis);
            @Cleanup ZipInputStream zis = new ZipInputStream(bis);
            ZipEntry ze = null;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                if (debugOn) {
                    System.out.println(
                            "ze.getName()=" + ze.getName() + "," + "getSize()=" + ze.getSize()
                    );
                }
                int size = (int) ze.getSize();
                // -1 means unknown size.
                if (size == -1) {
                    size = ((Integer) htSizes.get(ze.getName())).intValue();
                }
                byte[] b = new byte[(int) size];
                int rb = 0;
                int chunk = 0;
                while (((int) size - rb) > 0) {
                    chunk = zis.read(b, rb, (int) size - rb);
                    if (chunk == -1) {
                        break;
                    }
                    rb += chunk;
                }

                if (debugOn) {
                    System.out.println(
                            ze.getName() + "  rb=" + rb +
                                    ",size=" + size +
                                    ",csize=" + ze.getCompressedSize()
                    );
                }

                if (ze.getName().equals(name)) {
                    return b;
                }
            }
        } catch (NullPointerException e) {
            System.out.println("done.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Dumps a zip entry into a string.
     *
     * @param ze a ZipEntry
     */
    private String dumpZipEntry(ZipEntry ze) {
        StringBuilder sb = new StringBuilder();
        if (ze.isDirectory()) {
            sb.append("d ");
        } else {
            sb.append("f ");
        }
        if (ze.getMethod() == ZipEntry.STORED) {
            sb.append("stored   ");
        } else {
            sb.append("defalted ");
        }
        sb.append(ze.getName());
        sb.append("\t");
        sb.append("" + ze.getSize());
        if (ze.getMethod() == ZipEntry.DEFLATED) {
            sb.append("/" + ze.getCompressedSize());
        }
        return (sb.toString());
    }

}