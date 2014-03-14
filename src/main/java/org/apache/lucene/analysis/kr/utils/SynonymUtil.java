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

import com.google.common.base.Joiner;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.kr.morph.MorphException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 동의어 분석을 수행합니다.
 *
 * @author 배성혁 sunghyouk.bae@gmail.com
 * @since 13. 4. 27. 오전 12:31
 */
public class SynonymUtil {

    private static final Logger log = LoggerFactory.getLogger(SynonymUtil.class);
    private static final boolean isTraceEnabled = log.isTraceEnabled();
    private static final boolean isDebugEnabled = log.isDebugEnabled();

    /** 동의어 사전 */
    private static final SetMultimap<String, String> synonymMap = TreeMultimap.create();
    private static final Set<String> EMPTY_SET = new HashSet<String>();

    static {
        final String filename = KoreanEnv.getInstance().getValue(KoreanEnv.FILE_SYNONYM);
        log.info("동의어 사전에서 동의어 정보를 로드합니다... filename=[{}]", filename);
        List<String> lines = FileUtil.readLines(filename, "UTF-8");
        log.info("동의어 사전을 빌드합니다...");

        for (String line : lines) {
            String[] words = StringUtils.split(line, ",");
            if (words != null && words.length > 1) {
                synonymMap.putAll(words[0], Arrays.asList(words));
                if (isTraceEnabled)
                    log.trace("동의어를 추가합니다. words=[{}]", Joiner.on(",").join(words));
            }
        }
        log.info("동의어 사전을 빌드했습니다. 라인수=[{}], 동의어수=[{}]", lines.size(), synonymMap.values().size());
    }

    /**
     * 지정한 단어의 동의어가 있으면, 모든 동의어를 반환합니다.
     *
     * @throws MorphException
     */
    public static Set<String> getSynonym(String word) throws MorphException {
        if (word == null || word.length() == 0)
            return new HashSet<String>();

        word = word.toLowerCase();

        if (isTraceEnabled)
            log.trace("동의어를 찾습니다... word=[{}]", word);

        if (synonymMap == null || synonymMap.size() == 0)
            return EMPTY_SET;

        for (String key : synonymMap.keySet()) {
            Set<String> synonyms = synonymMap.get(key);
            if (key.equalsIgnoreCase(word) || synonyms.contains(word)) {
                if (isTraceEnabled)
                    log.trace("동의어를 찾았습니다. word=[{}], synonyms=[{}]", word, StringUtil.join(synonyms, ","));
                return synonyms;
            }
        }
        if (isTraceEnabled)
            log.trace("동의어가 없습니다.");

        return EMPTY_SET;
    }
}
