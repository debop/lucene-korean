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
import org.apache.lucene.analysis.kr.morph.WordEntry;

/**
 * 동사의 불규칙 변형을 처리하는 Utility Class
 *
 * @author S.M.Lee
 */
public class IrregularUtil {

    // ㅂ 불규칙
    public static final char IRR_TYPE_BIUP = 'B';

    // ㅎ 불규칙
    public static final char IRR_TYPE_HIOOT = 'H';

    // ㄹ 불규칙
    public static final char IRR_TYPE_LIUL = 'U';

    // 르 불규칙
    public static final char IRR_TYPE_LOO = 'L';

    // ㅅ 불규칙
    public static final char IRR_TYPE_SIUT = 'S';

    // ㄷ 불규칙
    public static final char IRR_TYPE_DI = 'D';

    // 러 불규칙
    public static final char IRR_TYPE_RU = 'R';

    // 으 탈락
    public static final char IRR_TYPE_UI = 'X';

    // 규칙형
    public static final char IRR_TYPE_REGULAR = 'X';

    public static String[] restoreIrregularVerb(String start, String end) throws MorphException {

        if (end == null) end = "";
        char[] jasos = new char[0];

        if (end.length() > 0) jasos = MorphUtil.decompose(end.charAt(0));

        if (end.startsWith("ㄴ")) {
            String[] irrs = restoreBIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreHIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreELIrregular(start, end);
            if (irrs != null) return irrs;
        } else if (end.startsWith("ㄹ")) {
            String[] irrs = restoreBIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreHIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreELIrregular(start, end);
            if (irrs != null) return irrs;
        } else if (end.startsWith("ㅁ")) {
            String[] irrs = restoreBIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreHIrregular(start, end);
            if (irrs != null) return irrs;
        } else if (end.startsWith("ㅂ")) {
            String[] irrs = restoreBIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreHIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreELIrregular(start, end);
            if (irrs != null) return irrs;
        } else if (start.endsWith("우") || start.endsWith("오")) {
            String[] irrs = restoreBIrregular(start, end);
            if (irrs != null) return irrs;
        } else if (end.startsWith("오")) {
            String[] irrs = restoreBIrregular(start, end);
            if (irrs != null) return irrs;
        } else if (end.startsWith("시")) {
            String[] irrs = restoreBIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreELIrregular(start, end);
            if (irrs != null) return irrs;
        } else if (end.startsWith("으")) {
            String[] irrs = restoreBIrregular(start, end);
            if (irrs != null) return irrs;
        } else if (jasos.length > 1 && jasos[0] == 'ㅇ' && (jasos[1] == 'ㅓ' || jasos[1] == 'ㅏ')) {
            String[] irrs = restoreDIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreSIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreLIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreHIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreUIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreRUIrregular(start, end);
            if (irrs != null) return irrs;
        } else if (jasos.length > 1 && jasos[0] == 'ㅇ' && jasos[1] == 'ㅡ') {
            String[] irrs = restoreDIrregular(start, end);
            if (irrs != null) return irrs;
            irrs = restoreSIrregular(start, end);
            if (irrs != null) return irrs;
        } else if (("가".equals(start) && "거라".equals(end)) ||
                ("오".equals(start) && "너라".equals(end))) {
            return new String[] { start, end };
        }

        return null;

    }

    /**
     * ㅂ 불규칙 원형을 복원한다. (돕다, 곱다)
     *
     * @throws org.apache.lucene.analysis.kr.morph.MorphException
     *
     */
    private static String[] restoreBIrregular(String start, String end) throws MorphException {

        if (start == null || "".equals(start) || end == null) return null;

        if (start.length() < 2) return null;

        if (!(start.endsWith("오") || start.endsWith("우"))) return null;

        char convEnd = MorphUtil.makeChar(end.charAt(0), 0);
        if ("ㅁ".equals(end) || "ㄴ".equals(end) || "ㄹ".equals(end) ||
                convEnd == '아' || convEnd == '어') { // 도우(돕), 고오(곱), 스러우(스럽) 등으로 변형되므로 반드시 2자 이상임

            char ch = start.charAt(start.length() - 2);
            ch = MorphUtil.makeChar(ch, 17);

            if (start.length() > 2)
                start = Utilities.arrayToString(new String[] { start.substring(0, start.length() - 2), Character.toString(ch) });
            else
                start = Character.toString(ch);

            WordEntry entry = DictionaryUtil.getVerb(start);
            if (entry != null && entry.getFeature(WordEntry.IDX_REGURA) == IRR_TYPE_BIUP)
                return new String[] { start, end };
        }


        return null;
    }

    /**
     * ㄷ 불규칙 원형을 복원한다. (깨닫다, 묻다)
     *
     * @throws org.apache.lucene.analysis.kr.morph.MorphException
     *
     */
    private static String[] restoreDIrregular(String start, String end) throws MorphException {
        if (start == null || "".equals(start)) return null;

        char ch = start.charAt(start.length() - 1);
        char[] jasos = MorphUtil.decompose(ch);
        if (jasos.length != 3 || jasos[2] != 'ㄹ') return null;

        ch = MorphUtil.makeChar(ch, 7);
        if (start.length() > 1)
            start = Utilities.arrayToString(new String[] { start.substring(0, start.length() - 1), Character.toString(ch) });
        else
            start = Character.toString(ch);

        WordEntry entry = DictionaryUtil.getVerb(start);
        if (entry != null && entry.getFeature(WordEntry.IDX_REGURA) == IRR_TYPE_DI)
            return new String[] { start, end };

        return null;
    }

    /**
     * ㅅ 불규칙 원형을 복원한다. (긋다--그어)
     *
     * @throws org.apache.lucene.analysis.kr.morph.MorphException
     *
     */
    private static String[] restoreSIrregular(String start, String end) throws MorphException {
        if (start == null || "".equals(start)) return null;

        char ch = start.charAt(start.length() - 1);
        char[] jasos = MorphUtil.decompose(ch);
        if (jasos.length != 2) return null;

        ch = MorphUtil.makeChar(ch, 19);
        if (start.length() > 1)
            start = start.substring(0, start.length() - 1) + ch;
        else
            start = Character.toString(ch);

        WordEntry entry = DictionaryUtil.getVerb(start);
        if (entry != null && entry.getFeature(WordEntry.IDX_REGURA) == IRR_TYPE_SIUT)
            return new String[] { start, end };

        return null;
    }

    /**
     * 르 불규칙 원형을 복원한다. (흐르다-->흘러)
     * "따르다"는 ㄹ불규칙이 아니지만.. 인 것처럼 처리한다.
     *
     * @throws org.apache.lucene.analysis.kr.morph.MorphException
     *
     */
    private static String[] restoreLIrregular(String start, String end) throws MorphException {

        if (start.length() < 2) return null;

        char ch1 = start.charAt(start.length() - 2);
        char ch2 = start.charAt(start.length() - 1);

        char[] jasos1 = MorphUtil.decompose(ch1);


        if (((jasos1.length == 3 && jasos1[2] == 'ㄹ') || jasos1.length == 2) && (ch2 == '러' || ch2 == '라')) {

            StringBuilder sb = new StringBuilder();

            ch1 = MorphUtil.makeChar(ch1, 0);
            if (start.length() > 2)
                sb.append(start.substring(0, start.length() - 2)).append(ch1).append("르");
            else
                sb.append(Character.toString(ch1)).append("르");

            WordEntry entry = DictionaryUtil.getVerb(sb.toString());
            if (entry != null && entry.getFeature(WordEntry.IDX_REGURA) == IRR_TYPE_LOO)
                return new String[] { sb.toString(), end };

        }

        return null;
    }

    /**
     * ㄹ불규칙 원형을 복원한다. (길다-->긴, 알다-->안)
     * 어간의 끝소리인 ‘ㄹ’이 ‘ㄴ’, ‘ㄹ’, ‘ㅂ’, ‘오’, ‘시’ 앞에서 탈락하는 활용의 형식
     *
     * @throws org.apache.lucene.analysis.kr.morph.MorphException
     *
     */
    private static String[] restoreELIrregular(String start, String end) throws MorphException {

        if (start == null || start.length() == 0 || end == null || end.length() == 0) return null;

        if (!(end.charAt(0) == 'ㄴ' || end.charAt(0) == 'ㄹ' || end.charAt(0) == 'ㅂ' || end.charAt(0) == '오' || end.charAt(0) == '시'))
            return null;

        char convEnd = MorphUtil.makeChar(start.charAt(start.length() - 1), 8);
        start = start.substring(0, start.length() - 1) + convEnd;

        WordEntry entry = DictionaryUtil.getVerb(start);
        if (entry != null)
            return new String[] { start, end };

        return null;
    }

    /**
     * 러 불규칙 원형을 복원한다. (이르다->이르러, 푸르다->푸르러)
     *
     * @throws org.apache.lucene.analysis.kr.morph.MorphException
     *
     */
    private static String[] restoreRUIrregular(String start, String end) throws MorphException {

        if (start.length() < 2) return null;

        char ch1 = start.charAt(start.length() - 1);
        char ch2 = start.charAt(start.length() - 2);

        char[] jasos1 = MorphUtil.decompose(ch1);
        char[] jasos2 = MorphUtil.decompose(ch2);
        if (jasos1[0] != 'ㄹ' || jasos2[0] != 'ㄹ') return null;

        ch2 = MorphUtil.makeChar(ch2, 0);
        if (start.length() > 2)
            start = start.substring(0, start.length() - 1);
        else
            start = Character.toString(ch2);

        WordEntry entry = DictionaryUtil.getVerb(start);
        if (entry != null && entry.getFeature(WordEntry.IDX_REGURA) == IRR_TYPE_RU)
            return new String[] { start, end };

        return null;
    }

    /**
     * ㅎ 탈락 원형을 복원한다. (까맣다-->까만,까매서)
     *
     * @throws org.apache.lucene.analysis.kr.morph.MorphException
     *
     */
    private static String[] restoreHIrregular(String start, String end) throws MorphException {
        if (start == null || "".equals(start) || end == null || "".equals(end)) return null;
        char ch1 = end.charAt(0);
        char ch2 = start.charAt(start.length() - 1);

        char[] jasos1 = MorphUtil.decompose(ch1);
        char[] jasos2 = MorphUtil.decompose(ch2);

        if (jasos1.length == 1) {
            ch2 = MorphUtil.makeChar(ch2, 27);
        } else {
            if (jasos2.length != 2 || jasos2[1] != 'ㅐ') return null;
            ch2 = MorphUtil.makeChar(ch2, 0, 27);
        }

        if (start.length() > 1)
            start = start.substring(0, start.length() - 1) + ch2;
        else
            start = Character.toString(ch2);

        WordEntry entry = DictionaryUtil.getVerb(start);
        if (entry != null && entry.getFeature(WordEntry.IDX_REGURA) == IRR_TYPE_HIOOT)
            return new String[] { start, end };

        return null;
    }

    /**
     * 으 탈락 원형을 복원한다. (뜨다->더, 크다-커)
     *
     * @throws org.apache.lucene.analysis.kr.morph.MorphException
     *
     */
    private static String[] restoreUIrregular(String start, String end) throws MorphException {
        if (start == null || "".equals(start)) return null;
        char ch = start.charAt(start.length() - 1);
        char[] jasos = MorphUtil.decompose(ch);
        if (!(jasos.length == 2 && jasos[1] == 'ㅓ')) return null;

        ch = MorphUtil.makeChar(ch, 18, 0);

        if (start.length() > 1)
            start = start.substring(0, start.length() - 1) + ch;
        else
            start = Character.toString(ch);

        WordEntry entry = DictionaryUtil.getVerb(start);
        if (entry != null) return new String[] { start, end };

        return null;
    }
}
