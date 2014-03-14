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

import jodd.util.collection.SortedArrayList;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 빈도수가 높은 Terms 를 조회합니다.
 *
 * @author 배성혁 sunghyouk.bae@gmail.com
 * @since 13. 5. 30. 오후 9:10
 */
@Slf4j
public class HighFreqTerms {

    /** 빈도수에 따른 조회 Term의 기본 갯수 */
    public static final int DEFAULT_TERMS_NUM = 100;

    /**
     * 인덱스 파일로부터 빈도수가 높은 단어들을 조회합니다.
     *
     * @param readers  인덱스 리더
     * @param numTerms 조회할 단어의 최대 갯수
     * @param field    엔티티의 필드 (null이면 모든 필드에서 찾는다)
     * @return 빈도수가 높은 단어들의 배열
     * @throws IOException 인덱스 파일 처리 실패 시
     */
    public static TermFreq[] getHighFreqTerms(IndexReader[] readers, int numTerms, String field) throws IOException {
        HighFreqTermQueue queue = new HighFreqTermQueue(numTerms);
        if (field != null) {
            for (IndexReader reader : readers) {
                TermEnum terms = reader.terms(new Term(field));
                if (terms != null && terms.term() != null) {
                    do {
                        if (!terms.term().field().equals(field))
                            break;
                        queue.insertWithOverflow(new TermFreq(terms.term(), terms.docFreq()));

                    } while (terms.next());
                }
            }
        } else {
            for (IndexReader reader : readers) {
                TermEnum terms = reader.terms();
                if (terms != null) {
                    while (terms.next()) {
                        queue.insertWithOverflow(new TermFreq(terms.term(), terms.docFreq()));
                    }
                }
            }
        }

        TermFreq[] result = new TermFreq[queue.size()];
        result = queue.toArray(result);
        return result;
    }

    /**
     * 인덱스 파일로부터 빈도수가 높은 단어들을 조회합니다.
     *
     * @param reader   인덱스 리더
     * @param numTerms 조회할 단어의 최대 갯수
     * @param field    엔티티의 필드 (null이면 모든 필드에서 찾는다)
     * @return 빈도수가 높은 단어들의 배열
     * @throws IOException 인덱스 파일 처리 실패 시
     */
    public static TermFreq[] getHighFreqTerms(IndexReader reader, int numTerms, String field) throws IOException {
        HighFreqTermQueue queue = new HighFreqTermQueue(numTerms);
        if (field != null) {
            TermEnum terms = reader.terms(new Term(field));
            if (terms != null && terms.term() != null) {
                do {
                    if (!terms.term().field().equals(field)) {
                        break;
                    }
                    queue.insertWithOverflow(new TermFreq(terms.term(), terms.docFreq()));
                } while (terms.next());
            } else {
                log.info("No terms for field=[{}]", field);
            }
        } else {
            TermEnum terms = reader.terms();
            if (terms != null) {
                while (terms.next()) {
                    queue.insertWithOverflow(new TermFreq(terms.term(), terms.docFreq()));
                }
            }
        }

        TermFreq[] result = new TermFreq[queue.size()];
        result = queue.toArray(result);
        return result;
    }

    /**
     * 검색한 단어의 빈도 정보를 전체 빈도 수에 따라 역순으로 정렬합니다.
     *
     * @param reader    인덱스 리더
     * @param termFreqs 단어/빈도수 정보
     */
    public static TermFreq[] sortByTotalTermFreq(IndexReader reader, TermFreq[] termFreqs) throws Exception {

        TermFreq[] tfs = new TermFreq[termFreqs.length];
        long totalTermFreq;
        for (int i = 0; i < termFreqs.length; i++) {
            totalTermFreq = getTotalTermFreq(reader, termFreqs[i].term);
            tfs[i] = new TermFreq(termFreqs[i].term, termFreqs[i].docFreq, totalTermFreq);
        }

        Comparator<TermFreq> c = new TotalTermFreqComparatorSortDescending();
        Arrays.sort(tfs, c);
        return tfs;
    }

    /**
     * 해당 단어가 들어간 문서들에서 모든 빈도 수 (문서의 수가 아닌)를 계산합니다.
     *
     * @param reader 인덱스 리더
     * @param term   단어
     * @return 단어의 총 빈도 수 ( 문서 내의 모든 빈도 수의 합 )
     * @throws Exception
     */
    public static long getTotalTermFreq(IndexReader reader, Term term) throws Exception {
        long totalTermFreq = 0;
        TermDocs docs = reader.termDocs(term);
        while (docs.next()) {
            totalTermFreq += docs.freq();
        }
        return totalTermFreq;
    }

    /** 빈도수가 높은 순으로 저장하는 큐입니다 */
    public static class HighFreqTermQueue extends SortedArrayList<TermFreq> {

        private final int capacity;

        public HighFreqTermQueue(int capacity) {
            super(new DocFreqComparatorDescending());
            this.capacity = capacity;
        }

        public void insertWithOverflow(TermFreq termFreq) {
            int index = indexOf(termFreq);
            if (index >= 0) {
                termFreq.docFreq += get(index).docFreq;
                remove(index);
                add(termFreq);
            } else {
                add(termFreq);
                if (size() > capacity) {
                    remove(size() - 1);
                }
            }
        }

        private static final long serialVersionUID = 7262421000814128023L;
    }

    public static class DocFreqComparatorDescending implements Comparator<TermFreq> {
        @Override
        public int compare(TermFreq a, TermFreq b) {
            return b.docFreq - a.docFreq;
        }
    }


    /** 전체 빈도수의 역순 (최다 빈도수가 상위에 오도록) 으로 정렬할 수 있도록 하는 비교자입니다. */
    public static class TotalTermFreqComparatorSortDescending implements Comparator<TermFreq> {
        @Override
        public int compare(TermFreq a, TermFreq b) {
            if (a.totalTermFreq < b.totalTermFreq) {
                return 1;
            } else if (a.totalTermFreq > b.totalTermFreq) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
