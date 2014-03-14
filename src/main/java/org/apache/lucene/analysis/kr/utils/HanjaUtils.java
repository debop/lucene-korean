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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HanjaUtils {

    private static final Logger log = LoggerFactory.getLogger(HanjaUtils.class);

    private static final Map<String, char[]> mapHanja = new HashMap<String, char[]>();

    static {
        List<String> strList = FileUtil.readLines(KoreanEnv.getInstance().getValue(KoreanEnv.FILE_MAPHANJA), KoreanEnv.UTF8);

        log.info("한자 사전을 빌드합니다...");

        for (String str : strList) {
            if (str.length() < 1 || !str.contains(","))
                continue;
            String[] hanInfos = StringUtil.split(str, ",");
            if (hanInfos.length != 2)
                continue;

            String hanja = StringEscapeUtil.unescapeJava(hanInfos[0]);
            mapHanja.put(hanja, hanInfos[1].toCharArray());
        }
        log.info("한자 사전을 빌드했습니다. 단어수=[{}], 로드수=[{}]", strList.size(), mapHanja.size());
    }

    /**
     * 한자에 대응하는 한글을 찾아서 반환한다.
     * 하나의 한자는 여러 음으로 읽일 수 있으므로 가능한 모든 음을 한글로 반환한다.
     *
     * @throws org.apache.lucene.analysis.kr.morph.MorphException
     *
     */
    public static char[] convertToHangul(char hanja) throws MorphException {
//		if(hanja>0x9FFF||hanja<0x3400) return new char[]{hanja};

        char[] result = mapHanja.get(new String(new char[] { hanja }));

        if (result == null)
            result = new char[] { hanja };

        if (log.isTraceEnabled())
            log.trace("한자에 대응하는 한글을 찾아서 변환합니다. hanja=[{}], result=[{}]", hanja, result);

        return result;
    }
}
