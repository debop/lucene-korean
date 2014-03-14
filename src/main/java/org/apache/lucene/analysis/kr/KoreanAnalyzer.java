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


import org.apache.lucene.analysis.*;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Filters StandardTokenizer with StandardFilter,
 * {@link org.apache.lucene.analysis.LowerCaseFilter} and {@link org.apache.lucene.analysis.StopFilter},
 * using a list of English stop words.
 *
 * @version $Id: KoreanAnalyzer.java,v 1.2 2013/04/07 13:10:27 smlee0818 Exp $
 */
public class KoreanAnalyzer extends StopwordAnalyzerBase {

    private static final Logger log = LoggerFactory.getLogger(KoreanAnalyzer.class);
    private static final boolean isTraceEnabled = log.isTraceEnabled();
    private static final boolean isDebugEnabled = log.isDebugEnabled();

    /** Default maximum allowed token length */
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

    private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

    /**
     * Specifies whether deprecated acronyms should be replaced with HOST type.
     * See {@linkplain "https://issues.apache.org/jira/browse/LUCENE-1068"}
     */
    private boolean replaceInvalidAcronym = true;

    private Set stopSet;

    private boolean bigrammable = true;

    private boolean hasOrigin = true;

    private boolean exactMatch = false;

    private boolean originCNoun = true;

    public static final String DIC_ENCODING = "UTF-8";

    /**
     * An unmodifiable set containing some common English words that are usually not
     * useful for searching.
     */
    public static final Set<?> STOP_WORDS_SET;


    static {
        List<String> stopWords = Arrays.asList(
                "a", "an", "and", "are", "as", "at", "be", "but", "by",
                "for", "if", "in", "into", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the",
                "their", "then", "there", "these", "they", "this", "to", "was", "will", "with",
                "이", "그", "저", "요", "것", "수", "등", "들", "및", "에", "에서", "그리고", "그래서", "또", "또는", "꼭", "잘",
                "?", "!", ";", ".", "-");

        CharArraySet stopSet = new CharArraySet(Version.LUCENE_36, stopWords.size(), false);
        stopSet.addAll(stopWords);
        STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet);
    }

    public KoreanAnalyzer() {
        this(Version.LUCENE_36, STOP_WORDS_SET);
    }

    /** 검색을 위한 형태소분석 */
    public KoreanAnalyzer(boolean exactMatch) {
        this(Version.LUCENE_36, STOP_WORDS_SET);
        this.exactMatch = exactMatch;
    }

    public KoreanAnalyzer(Version matchVersion, String[] stopWords) throws IOException {
        this(matchVersion, StopFilter.makeStopSet(matchVersion, stopWords));
    }

    /** Builds an analyzer with the stop words from the given file. */
    public KoreanAnalyzer(Version matchVersion) throws IOException {
        this(matchVersion, STOP_WORDS_SET);
    }

//  /** Builds an analyzer with the stop words from the given file.
//   * @see WordlistLoader#getWordSet(File)
//   */
//	public KoreanAnalyzer(Version matchVersion, File stopwords) throws IOException {     
//        this(matchVersion, WordlistLoader.getWordSet(new InputStreamReader(new FileInputStream(stopwords), DIC_ENCODING)));        
//	}
//
//  /** Builds an analyzer with the stop words from the given file.
//   * @see WordlistLoader#getWordSet(File)
//   */
//	public KoreanAnalyzer(Version matchVersion, File stopwords, String encoding) throws IOException {
//        this(matchVersion, WordlistLoader.getWordSet(new InputStreamReader(new FileInputStream(stopwords), encoding)));
//	}
//		
//	/** Builds an analyzer with the stop words from the given reader.
//	 * @see WordlistLoader#getWordSet(Reader)
//	*/
//	public KoreanAnalyzer(Version matchVersion, Reader stopwords) throws IOException {
//	   this(matchVersion, WordlistLoader.getWordSet(stopwords));	    
//	}

    /** Builds an analyzer with the stop words from the given reader. */
    public KoreanAnalyzer(Version matchVersion, Set<?> stopWords) {
        super(matchVersion, stopWords);
        replaceInvalidAcronym = true; // matchVersion.onOrAfter(Version.LUCENE_36);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        if (isDebugEnabled)
            log.debug("TokenStreamComponents를 생성합니다. fieldName=[{}]", fieldName);

        final KoreanTokenizer src = new KoreanTokenizer(matchVersion, reader);
        src.setMaxTokenLength(maxTokenLength);
        //src.setReplaceInvalidAcronym(replaceInvalidAcronym);

        TokenStream tok = new KoreanFilter(src, bigrammable, hasOrigin, exactMatch);
        tok = new LowerCaseFilter(matchVersion, tok);
        tok = new StopFilter(matchVersion, tok, stopwords);

        return new TokenStreamComponents(src, tok) {
            @Override
            protected boolean reset(final Reader reader) throws IOException {
                src.setMaxTokenLength(KoreanAnalyzer.this.maxTokenLength);
                return super.reset(reader);
            }
        };
    }

    /**
     * determine whether the bigram index term is returned or not if a input word is failed to analysis
     * If true is set, the bigram index term is returned. If false is set, the bigram index term is not returned.
     */
    public void setBigrammable(boolean is) {
        bigrammable = is;
    }

    /** determin whether the original term is returned or not if a input word is analyzed morphically. */
    public void setHasOrigin(boolean has) {
        hasOrigin = has;
    }

    /** determin whether the original compound noun is returned or not if a input word is analyzed morphically. */
    public void setOriginCNoun(boolean cnoun) {
        originCNoun = cnoun;
    }

    /** determin whether the original compound noun is returned or not if a input word is analyzed morphically. */
    public void setExactMatch(boolean exact) {
        exactMatch = exact;
    }
}
