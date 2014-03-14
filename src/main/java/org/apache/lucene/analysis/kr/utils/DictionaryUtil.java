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

import org.apache.lucene.analysis.kr.morph.CompoundEntry;
import org.apache.lucene.analysis.kr.morph.MorphException;
import org.apache.lucene.analysis.kr.morph.WordEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

@SuppressWarnings("unchecked")
public class DictionaryUtil {

    private static final Logger log = LoggerFactory.getLogger(DictionaryUtil.class);

    private static Trie<String, WordEntry> dictionary = new Trie<String, WordEntry>(true);

    private static HashMap<String, String> josas = new HashMap<String, String>();

    private static HashMap<String, String> eomis = new HashMap<String, String>();

    private static HashMap<String, String> prefixs = new HashMap<String, String>();

    private static HashMap<String, String> suffixs = new HashMap<String, String>();

    private static HashMap<String, WordEntry> uncompounds = new HashMap<String, WordEntry>();

    private static HashMap<String, String> cjwords = new HashMap<String, String>();

    static {

        loadDictionary();

        readFile(josas, KoreanEnv.FILE_JOSA);
        readFile(eomis, KoreanEnv.FILE_EOMI);
        readFile(prefixs, KoreanEnv.FILE_PREFIX);
        readFile(suffixs, KoreanEnv.FILE_SUFFIX);

        // uncompunds
        char[] features = "90000X".toCharArray();

        List<String> lines = FileUtil.readLines(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_UNCOMPOUNDS), KoreanEnv.UTF8);
        for (String compound : lines) {
            String[] infos = StringUtil.split(compound, ":");
            if (infos.length != 2) continue;
            WordEntry entry = new WordEntry(infos[0].trim(), features);
            entry.setCompounds(compoundArrayToList(infos[1], StringUtil.split(infos[1], ",")));
            uncompounds.put(entry.getWord(), entry);
        }

        lines = FileUtil.readLines(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_CJ), KoreanEnv.UTF8);
        for (String cj : lines) {
            String[] infos = StringUtil.split(cj, ":");
            if (infos.length != 2) continue;
            cjwords.put(infos[0], infos[1]);
        }
    }


    /** 사전을 로드한다. */
    public synchronized static void loadDictionary() throws MorphException {
        log.info("사전을 로드합니다...");

        log.info("표준 사전을 로드합니다...");
        Future<List<String>> standardDic = FileUtil.readLinesAsync(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_DICTIONARY), KoreanEnv.UTF8);
        log.info("복합명사 사전을 로드합니다...");
        Future<List<String>> compoundDic = FileUtil.readLinesAsync(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_COMPOUNDS), KoreanEnv.UTF8);
        log.info("확장 사전을 로드합니다...");
        Future<List<String>> extensionDic = FileUtil.readLinesAsync(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_EXTENSION), KoreanEnv.UTF8);
        log.info("사용자 정의 사전을 로드합니다...");
        Future<List<String>> customDic = FileUtil.readLinesAsync(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_CUSTOM), KoreanEnv.UTF8);

        try {
            log.info("표준 사전을 파싱합니다...");
            List<String> standards = standardDic.get();
            // List<String> standards = FileUtil.readLines(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_DICTIONARY), "UTF-8");
            int count = 0;
            for (String str : standards) {
                String[] infos = StringUtil.split(str, ",");
                if (infos.length != 2) continue;
                infos[1] = infos[1].trim();
                if (infos[1].length() == 6)
                    infos[1] = infos[1].substring(0, 5) + "000" + infos[1].substring(5);

                if (dictionary.get(infos[0].trim()) == null) {
                    WordEntry entry = new WordEntry(infos[0].trim(), infos[1].trim().toCharArray());
                    dictionary.add(entry.getWord(), entry);
                    count++;
                }
            }
            log.info("표준 사전을 빌드했습니다. 단어수=[{}], 등록수=[{}]", standards.size(), count);
        } catch (Exception e) {
            log.error("표준 사전을 로드하는데 실패했습니다.", e);
            throw new MorphException(e);
        }

        try {
            log.info("복합명사 사전을 파싱합니다...");
            List<String> compounds = compoundDic.get();
            // List<String> compounds = FileUtil.readLines(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_COMPOUNDS), "UTF-8");
            char[] features = "20000000X".toCharArray();
            int count = 0;

            for (String compound : compounds) {
                String[] infos = StringUtil.split(compound, ":");
                if (infos.length != 2) continue;
                if (dictionary.get(infos[0].trim()) == null) {
                    WordEntry entry = new WordEntry(infos[0].trim(), features);
                    entry.setCompounds(compoundArrayToList(infos[1], StringUtil.split(infos[1], ",")));
                    dictionary.add(entry.getWord(), entry);
                    count++;
                }
            }
            log.info("복합명사 사전을 빌드했습니다. 단어수=[{}], 등록수=[{}]", compounds.size(), count);
        } catch (Exception e) {
            log.error("복합명사 사전을 로드하는데 실패했습니다.", e);
            throw new MorphException(e);
        }

        try {
            log.info("확장 사전을 파싱합니다...");
            List<String> extensions = extensionDic.get();
            // List<String> extensions = FileUtil.readLines(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_EXTENSION), "UTF-8");
            int count = 0;

            for (String str : extensions) {
                String[] infos = StringUtil.split(str, ",");
                if (infos.length != 2) continue;
                infos[1] = infos[1].trim();
                if (infos[1].length() == 6)
                    infos[1] = infos[1].substring(0, 5) + "000" + infos[1].substring(5);

                if (dictionary.get(infos[0].trim()) == null) {
                    WordEntry entry = new WordEntry(infos[0].trim(), infos[1].trim().toCharArray());
                    dictionary.add(entry.getWord(), entry);
                    count++;
                }
            }
            log.info("확장 사전을 빌드했습니다. 단어수=[{}], 등록수=[{}]", extensions.size(), count);
        } catch (Exception e) {
            log.error("확장 사전을 로드하는데 실패했습니다.", e);
            throw new MorphException(e);
        }

        try {
            log.info("사용자정의 사전을 파싱합니다...");
            List<String> customs = customDic.get();
            // final List<String> customs = FileUtil.readLines(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_CUSTOM), "UTF-8");
            char[] features = "100000000X".toCharArray();
            int count = 0;

            for (String custom : customs) {
                if (custom != null && custom.trim().length() > 0) {
                    if (dictionary.get(custom.trim()) == null) {
                        WordEntry entry = new WordEntry(custom.trim(), features);
                        dictionary.add(entry.getWord(), entry);
                        count++;
                    }
                }
            }
            log.info("사용자정의 사전을 빌드했습니다. 단어수=[{}], 등록수=[{}]", customs.size(), count);
        } catch (Exception e) {
            log.error("사용자정의 사전을 로드하는데 실패했습니다.", e);
            throw new MorphException(e);
        }
        log.info("사전을 빌드했습니다.");
    }

    public static Iterator findWithPrefix(String prefix) throws MorphException {
        return dictionary.getPrefixedBy(prefix);
    }

    public static WordEntry getWord(String key) throws MorphException {
        if (key.length() == 0) return null;

        return (WordEntry) dictionary.get(key);
    }

    public static WordEntry getWordExceptVerb(String key) throws MorphException {
        WordEntry entry = getWord(key);
        if (entry == null) return null;

        if (entry.getFeature(WordEntry.IDX_NOUN) == '1' ||
                entry.getFeature(WordEntry.IDX_BUSA) == '1') return entry;
        return null;
    }

    public static WordEntry getNoun(String key) throws MorphException {

        WordEntry entry = getWord(key);
        if (entry == null) return null;

        if (entry.getFeature(WordEntry.IDX_NOUN) == '1') return entry;
        return null;
    }

    public static WordEntry getCNoun(String key) throws MorphException {

        WordEntry entry = getWord(key);
        if (entry == null) return null;

        if (entry.getFeature(WordEntry.IDX_NOUN) == '1' || entry.getFeature(WordEntry.IDX_NOUN) == '2') return entry;
        return null;
    }

    public static WordEntry getVerb(String key) throws MorphException {

        WordEntry entry = getWord(key);
        if (entry == null) return null;

        if (entry.getFeature(WordEntry.IDX_VERB) == '1') {
            return entry;
        }
        return null;
    }

    public static WordEntry getAdverb(String key) throws MorphException {
        WordEntry entry = getWord(key);
        if (entry == null) return null;

        if (entry.getFeature(WordEntry.IDX_BUSA) == '1') return entry;
        return null;
    }

    public static WordEntry getBusa(String key) throws MorphException {
        WordEntry entry = getWord(key);
        if (entry == null) return null;

        if (entry.getFeature(WordEntry.IDX_BUSA) == '1' && entry.getFeature(WordEntry.IDX_NOUN) == '0') return entry;
        return null;
    }

    public static WordEntry getIrrVerb(String key, char irrType) throws MorphException {
        WordEntry entry = getWord(key);
        if (entry == null) return null;
        if (entry.getFeature(WordEntry.IDX_VERB) == '1' &&
                entry.getFeature(WordEntry.IDX_REGURA) == irrType) return entry;
        return null;
    }

    public static WordEntry getBeVerb(String key) throws MorphException {
        WordEntry entry = getWord(key);
        if (entry == null) return null;
        if (entry.getFeature(WordEntry.IDX_BEV) == '1') return entry;
        return null;
    }

    public static WordEntry getDoVerb(String key) throws MorphException {
        WordEntry entry = getWord(key);
        if (entry == null) return null;
        if (entry.getFeature(WordEntry.IDX_DOV) == '1') return entry;
        return null;
    }

    public synchronized static WordEntry getUncompound(String key) throws MorphException {
        return uncompounds.get(key);
    }

    public synchronized static String getCJWord(String key) throws MorphException {
        return cjwords.get(key);
    }

    public static boolean existJosa(String str) throws MorphException {
        return josas.get(str) != null;
    }

    public static boolean existEomi(String str) throws MorphException {
        return (eomis.get(str) != null);
    }

    public static boolean existPrefix(String str) throws MorphException {
        return prefixs.get(str) != null;
    }

    public static boolean existSuffix(String str) throws MorphException {
        return suffixs.get(str) != null;
    }

    /** ㄴ,ㄹ,ㅁ,ㅂ과 eomi 가 결합하여 어미가 될 수 있는지 점검한다. */
    public static String combineAndEomiCheck(char s, String eomi) throws MorphException {

        if (eomi == null) eomi = "";

        if (s == 'ㄴ') eomi = "은" + eomi;
        else if (s == 'ㄹ') eomi = "을" + eomi;
        else if (s == 'ㅁ') eomi = "음" + eomi;
        else if (s == 'ㅂ') eomi = "습" + eomi;
        else eomi = s + eomi;

        if (existEomi(eomi)) return eomi;
        return null;
    }

    /**
     * 사전 파일에서 항목을 읽어 사전으로 빌드합니다.
     *
     * @throws org.apache.lucene.analysis.kr.morph.MorphException
     *
     */
    private static void readFile(HashMap<String, String> map, String dic) throws MorphException {
        String filename = KoreanEnv.getInstance().getValue(dic);
        try {
            List<String> lines = FileUtil.readLines(filename, KoreanEnv.UTF8);
            for (final String line : lines) {
                map.put(line.trim(), line);
            }
            log.info("사전 파일에서 [{}]개를 읽어, [{}]개를 등록했습니다. filename=[{}]", lines.size(), map.size(), filename);
        } catch (Exception e) {
            throw new MorphException(e);
        }
    }

    private static List compoundArrayToList(String source, String[] arr) {
        List list = new ArrayList();
        for (String str : arr) {
            CompoundEntry ce = new CompoundEntry(str);
            ce.setOffset(source.indexOf(str));
            list.add(ce);
        }
        return list;
    }
}

