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
import org.apache.lucene.analysis.kr.morph.AnalysisOutput;
import org.apache.lucene.analysis.kr.morph.MorphAnalyzer;
import org.apache.lucene.analysis.kr.tagging.Tagger;

import java.util.Iterator;

public class TaggerTest extends TestCase {

    public void testTagging() throws Exception {

        Iterator<String[]> iter = Tagger.getGR("할");
        while (iter.hasNext()) {
            String[] strs = iter.next();
            System.out.println(strs[0] + "?" + strs[1] + "?" + strs[2] + "?" + strs[3] + "?" + strs[4] + "?" + strs[5]);
        }

    }

    public void testTag() throws Exception {

        String str0 = "증가함에";
        String str1 = "따라서";
        String str2 = "적다";

        MorphAnalyzer morphAnal = new MorphAnalyzer();

        Tagger tagger = new Tagger();
        tagger.tagging(str0, morphAnal.analyze(str0));
        AnalysisOutput o = tagger.tagging(str1, str2, morphAnal.analyze(str1), morphAnal.analyze(str2));

        System.out.println(">>" + o);
    }
}
