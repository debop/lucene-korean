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

package org.apache.lucene.analysis.kr.test;

import junit.framework.TestCase;
import org.apache.lucene.analysis.kr.utils.MorphUtil;
import org.apache.lucene.analysis.kr.utils.VerbUtil;

public class UtilitiesTest extends TestCase {

    public void testEndsWithVerbSuffix() throws Exception {
        String str = "말하";
        int i = VerbUtil.endsWithVerbSuffix(str);
        if (i == -1) return;
        assertEquals("하", str.substring(i));
        System.out.println(i + ":" + str.substring(i));
    }

    public void testEndsWithXVerb() throws Exception {
        String str = "피어오르";
        int i = VerbUtil.endsWithXVerb(str);
        if (i == -1) return;
        assertEquals("오르", str.substring(i));
        System.out.println(i + ":" + str.substring(i));
    }

    public void testDecompse() throws Exception {

        String str = "금융위기";

        for (int i = 0; i < str.length(); i++) {
            char[] c = MorphUtil.decompose(str.charAt(i));

            for (int j = 0; j < c.length; j++) {
                int cn = c[j];
                System.out.print(c[j] + ":" + cn);
            }
            System.out.println();
        }

        System.out.println();

        char c = 4467;
        System.out.println(c);
    }

}
