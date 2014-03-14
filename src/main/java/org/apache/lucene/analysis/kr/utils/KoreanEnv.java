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

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.kr.morph.MorphException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;

@Slf4j
public class KoreanEnv {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    public static final String FILE_SYLLABLE_FEATURE = "syllable.dic";

    public static final String FILE_DICTIONARY = "dictionary.dic";

    public static final String FILE_JOSA = "josa.dic";

    public static final String FILE_EOMI = "eomi.dic";

    public static final String FILE_EXTENSION = "extension.dic";

    public static final String FILE_MAPHANJA = "mapHanja.dic";

    public static final String FILE_PREFIX = "prefix.dic";

    public static final String FILE_SUFFIX = "suffix.dic";

    public static final String FILE_COMPOUNDS = "compounds.dic";

    public static final String FILE_UNCOMPOUNDS = "uncompounds.dic";

    public static final String FILE_CJ = "cj.dic";

    public static final String FILE_SYNONYM = "synonym.dic";

    public static final String FILE_CUSTOM = "custom.dic";

    public static final String FILE_KOREAN_PROPERTY = "org/apache/lucene/analysis/kr/korean.properties";

    private Properties defaults = null;

    /** The props member gets its values from the configuration in the property file. */
    private Properties props = null;

    private static KoreanEnv instance = new KoreanEnv();

    /** The constructor loads property values from the property file. */
    private KoreanEnv() throws MorphException {
        log.info("형태소분석기의 사전에 대한 환경설정 정보를 로드합니다...");
        try {
            initDefaultProperties();
            props = loadProperties(defaults);
        } catch (MorphException e) {
            throw new MorphException("Failure while initializing property values:\n" + e.getMessage());
        }
        log.info("형태소분석기의 사전에 대한 환경설정 정보를 로드했습니다. 사전 위치=[{}]", defaults.getProperty(FILE_DICTIONARY));
    }

    public static KoreanEnv getInstance() throws MorphException {
        return instance;
    }

    /** Initialize the default property values. */
    private void initDefaultProperties() {
        defaults = new Properties();

        defaults.setProperty(FILE_SYLLABLE_FEATURE, "org/apache/lucene/analysis/kr/dic/syllable.dic");
        defaults.setProperty(FILE_DICTIONARY, "org/apache/lucene/analysis/kr/dic/dictionary.dic");
        defaults.setProperty(FILE_EXTENSION, "org/apache/lucene/analysis/kr/dic/extension.dic");
        defaults.setProperty(FILE_JOSA, "org/apache/lucene/analysis/kr/dic/josa.dic");
        defaults.setProperty(FILE_EOMI, "org/apache/lucene/analysis/kr/dic/eomi.dic");
        defaults.setProperty(FILE_MAPHANJA, "org/apache/lucene/analysis/kr/dic/mapHanja.dic");
        defaults.setProperty(FILE_PREFIX, "org/apache/lucene/analysis/kr/dic/prefix.dic");
        defaults.setProperty(FILE_SUFFIX, "org/apache/lucene/analysis/kr/dic/suffix.dic");
        defaults.setProperty(FILE_COMPOUNDS, "org/apache/lucene/analysis/kr/dic/compounds.dic");
        defaults.setProperty(FILE_UNCOMPOUNDS, "org/apache/lucene/analysis/kr/dic/uncompounds.dic");
        defaults.setProperty(FILE_CJ, "org/apache/lucene/analysis/kr/dic/cj.dic");
        defaults.setProperty(FILE_SYNONYM, "org/apache/lucene/analysis/kr/dic/synonym.dic");
        defaults.setProperty(FILE_CUSTOM, "org/apache/lucene/analysis/kr/dic/custom.dic");
    }


    /**
     * Given a property file name, load the property file and return an object
     * representing the property values.
     *
     * @param def Default property values, or <code>null</code> if there are no defaults.
     * @return The loaded SortedProperties object.
     */
    private Properties loadProperties(Properties def) throws MorphException {

        Properties properties = new Properties();
        if (def != null) properties = new Properties(def);

        try {
            InputStream stream = FileUtil.getResourceFileStream(FILE_KOREAN_PROPERTY);
            if (stream != null) {
                properties.load(stream);
                return properties;
            }

            byte[] in = FileUtil.readByteFromCurrentJar(FILE_KOREAN_PROPERTY);
            properties.load(new ByteArrayInputStream(in));
        } catch (Exception e) {
            throw new MorphException("Failure while trying to load properties file. file=" + FILE_KOREAN_PROPERTY, e);
        }
        return properties;
    }

    /**
     * Returns the value of a property.
     *
     * @param name The name of the property whose value is to be retrieved.
     * @return The value of the property.
     */
    public String getValue(String name) {
        return props.getProperty(name);
    }
}
