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

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.kr.KoreanAnalyzer;
import org.apache.lucene.analysis.kr.freq.HighFreqTerms;
import org.apache.lucene.analysis.kr.freq.TermFreq;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

@Slf4j
public class IndexingTest {

    private Directory directory;

    @Before
    public void setUp() throws Exception {
        directory = FSDirectory.open(new File(".lucene/index"));
    }

    private IndexWriter getWriter() throws IOException {
        return new IndexWriter(directory, new KoreanAnalyzer(), IndexWriter.MaxFieldLength.UNLIMITED);
    }

    @Test
    public void testIndexWriter() throws IOException {

        IndexWriter writer = getWriter();

        String description = "Approved for entry into archive by p pant (momo31@gmail.com) on 2011-11-18T05:08:46Z (GMT) No. of bitstreams: 0";
        String publisher = "漢陽大學校";
        String title = "硏究開發費 會計에 關한 硏究";


        Document doc = new Document();
        doc.add(new Field("description", description, Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("publisher", publisher, Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("title", title, Field.Store.YES, Field.Index.ANALYZED));

        writer.addDocument(doc);

        writer.close();
    }

    private IndexReader getReader() throws IOException {
        return IndexReader.open(directory);
    }

    @Test
    public void testIndexReader() throws Exception {
        testIndexWriter();

        IndexReader reader = getReader();
        try {
            int freq = reader.docFreq(new Term("description", "entry"));
            log.debug("freq=[{}]", freq);

        } finally {
            reader.close();
        }
    }

    @Test
    @Ignore( "테스트 전에 인덱스가 만들어 졌는지 확인해야 합니다." )
    public void termHighFreqTerms() throws Exception {
        testIndexWriter();
        try (IndexReader reader = getReader()) {
            TermFreq[] termFreqs = HighFreqTerms.getHighFreqTerms(reader, 100, "description");
            for (TermFreq termFreq : termFreqs) {
                log.debug("term=[{}]", termFreq);
            }
        }
    }

    @Test
    @Ignore( "테스트 전에 인덱스가 만들어 졌는지 확인해야 합니다." )
    public void termHighFreqTermsWithSharding() throws Exception {
        final String prefix = "debop4j-search/.lucene/indexes/kr.debop4j.search.twitter.Twit";
        final int numShard = 4;

        IndexReader[] readers = new IndexReader[numShard];
        for (int i = 0; i < numShard; i++) {
            readers[i] = IndexReader.open(FSDirectory.open(new File(prefix + "." + i)));
        }

        try {
            TermFreq[] termFreqs = HighFreqTerms.getHighFreqTerms(readers, 100, "text");
            for (TermFreq termFreq : termFreqs) {
                log.debug("term=[{}]", termFreq);
            }
        } finally {
            for (IndexReader reader : readers)
                reader.close();
        }
    }
}
