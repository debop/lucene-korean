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

package org.apache.lucene.analysis.kr.freq;

import org.apache.lucene.index.Term;

import java.io.Serializable;

/**
 * org.apache.lucene.analysis.TermFreq
 *
 * @author 배성혁 sunghyouk.bae@gmail.com
 * @since 13. 5. 30. 오후 9:12
 */
public class TermFreq implements Serializable {

    /** 단어 */
    public final Term term;

    /** 단어의 빈도수 */
    public int docFreq;

    /** 전체 단어의 빈도 수 */
    public long totalTermFreq;

    public TermFreq(Term term, int docFreq) {
        this(term, docFreq, 0);
    }

    public TermFreq(Term term, int docFreq, long totalTermFreq) {
        this.term = term;
        this.docFreq = docFreq;
        this.totalTermFreq = totalTermFreq;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof TermFreq) && (hashCode() == obj.hashCode());
    }

    @Override
    public int hashCode() {
        return term.field().hashCode() ^ 31 ^ term.text().hashCode();
    }

    @Override
    public String toString() {
        return String.format("TermFreq# term.field=%s, term.text=%s, docFreq=%d", term.field(), term.text(), docFreq);
    }

    private static final long serialVersionUID = -1015833890092285173L;
}
