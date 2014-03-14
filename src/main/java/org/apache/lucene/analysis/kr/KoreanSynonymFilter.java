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

package org.apache.lucene.analysis.kr;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.kr.morph.MorphException;
import org.apache.lucene.analysis.kr.utils.SynonymUtil;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.Set;

/**
 * org.apache.lucene.analysis.KoreanSynonymFilter
 *
 * @author 배성혁 sunghyouk.bae@gmail.com
 * @since 13. 4. 27. 오전 1:41
 */
@Slf4j
public class KoreanSynonymFilter extends TokenFilter {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    /** Construct a token stream filtering the given input. */
    protected KoreanSynonymFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            try {
                Set<String> words = SynonymUtil.getSynonym(termAtt.toString());
                for (String word : words) {
                    termAtt.append(word);
                }
            } catch (MorphException e) {
                return false;
            }
            return true;
        }
        return false;
    }
}
