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

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;

public class KoreanTokenizer extends Tokenizer {

    private static final Logger log = LoggerFactory.getLogger(KoreanTokenizer.class);
    private static final boolean isTraceEnabled = log.isTraceEnabled();
    private static final boolean isDebugEnabled = log.isDebugEnabled();

    /** A private instance of the JFlex-constructed scanner */
    private final KoreanTokenizerImpl scanner;

    public static final int ALPHANUM = 0;
    public static final int APOSTROPHE = 1;
    public static final int ACRONYM = 2;
    public static final int COMPANY = 3;
    public static final int EMAIL = 4;
    public static final int HOST = 5;
    public static final int NUM = 6;
    public static final int CJ = 7;
    public static final int ACRONYM_DEP = 8;
    public static final int KOREAN = 9;
    public static final int CHINESE = 10;

    /** String token types that correspond to token type int constants */
    public static final String[] TOKEN_TYPES = new String[] {
            "<ALPHANUM>",
            "<APOSTROPHE>",
            "<ACRONYM>",
            "<COMPANY>",
            "<EMAIL>",
            "<HOST>",
            "<NUM>",
            "<CJ>",
            "<ACRONYM_DEP>",
            "<KOREAN>",
            "<CHINESE>"
    };

    private boolean replaceInvalidAcronym;

    private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

    /**
     * Set the max allowed token length.  Any token longer
     * than this is skipped.
     */
    public void setMaxTokenLength(int length) {
        this.maxTokenLength = length;
    }

    /** @see #setMaxTokenLength */
    public int getMaxTokenLength() {
        return maxTokenLength;
    }

    /**
     * Creates a new instance of the {@link org.apache.lucene.analysis.standard.StandardTokenizer}.  Attaches
     * the <code>input</code> to the newly created JFlex scanner.
     *
     * @param input The input reader
     *              <p/>
     *              See http://issues.apache.org/jira/browse/LUCENE-1068
     */
    public KoreanTokenizer(Version matchVersion, Reader input) {
        super(input);
        this.scanner = new KoreanTokenizerImpl(input);
        init(input, matchVersion);
    }

    /** Creates a new StandardTokenizer with a given {@link org.apache.lucene.util.AttributeSource}. */
    public KoreanTokenizer(Version matchVersion, AttributeSource source, Reader input) {
        super(source, input);
        this.scanner = new KoreanTokenizerImpl(input);
        init(input, matchVersion);
    }

    /** Creates a new StandardTokenizer with a given {@link org.apache.lucene.util.AttributeSource.AttributeFactory} */
    public KoreanTokenizer(Version matchVersion, AttributeFactory factory, Reader input) {
        super(factory, input);
        this.scanner = new KoreanTokenizerImpl(input);
        init(input, matchVersion);
    }

    private void init(Reader input, Version matchVersion) {
        replaceInvalidAcronym = true;
        this.input = input;
    }

    // this tokenizer generates three attributes:
    // term offset, positionIncrement and type
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    /*
     * (non-Javadoc)
     *
     * @see org.apache.lucene.analysis.TokenStream#next()
     */
    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        int posIncr = 1;

        while (true) {
            int tokenType = scanner.getNextToken();

            if (isTraceEnabled)
                log.trace("token 증가. tokenType=[{}]", tokenType);

            if (tokenType == KoreanTokenizerImpl.YYEOF) {
                return false;
            }

            if (scanner.yylength() <= maxTokenLength) {
                posIncrAtt.setPositionIncrement(posIncr);
                scanner.getText(termAtt);
                final int start = scanner.yychar();
                offsetAtt.setOffset(correctOffset(start), correctOffset(start + termAtt.length()));
                typeAtt.setType(KoreanTokenizer.TOKEN_TYPES[tokenType]);

                if (isTraceEnabled)
                    log.trace("토큰 타입=[{}]", KoreanTokenizer.TOKEN_TYPES[tokenType]);

                return true;
            } else
                // When we skip a too-long term, we still increment the
                // position increment
                posIncr++;
        }
    }

    @Override
    public final void end() {
        // set final offset
        int finalOffset = correctOffset(scanner.yychar() + scanner.yylength());
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset(Reader reader) throws IOException {
        super.reset(reader);
        scanner.yyreset(reader);
    }

    /**
     * Prior to https://issues.apache.org/jira/browse/LUCENE-1068, StandardTokenizer mischaracterized as acronyms tokens like www.abc.com
     * when they should have been labeled as hosts instead.
     *
     * @return true if StandardTokenizer now returns these tokens as Hosts, otherwise false
     * @deprecated Remove in 3.X and make true the only valid value
     */
    @Deprecated
    public boolean isReplaceInvalidAcronym() {
        return replaceInvalidAcronym;
    }

    /**
     * @param replaceInvalidAcronym Set to true to replace mischaracterized acronyms as HOST.
     * @deprecated Remove in 3.X and make true the only valid value
     *             <p/>
     *             See https://issues.apache.org/jira/browse/LUCENE-1068
     */
    @Deprecated
    public void setReplaceInvalidAcronym(boolean replaceInvalidAcronym) {
        this.replaceInvalidAcronym = replaceInvalidAcronym;
    }
}
