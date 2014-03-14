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

package org.apache.lucene.analysis.kr.morph;

import java.util.ArrayList;
import java.util.List;

/**
 * 공백을 분석한 결과를 저장한다.
 *
 * @author smlee
 */
public class SpaceOutput {

    // 분석된 결과
    private AnalysisOutput output;

    // 분석 결과 앞에 있는 미등록어, 사람 이름은 대부분 이런 경우임.
    private List<AnalysisOutput> nrWords;

    // 분석하기 이전의 어절
    private String source;

    public SpaceOutput() {
        output = null;
        nrWords = new ArrayList<AnalysisOutput>();
        source = null;
    }

    public void initialize() {
        output = null;
        nrWords = new ArrayList<AnalysisOutput>();
        source = null;
    }

    /** @return the output */
    public AnalysisOutput getOutput() {
        return output;
    }

    /** @param output the output to set */
    public void setOutput(AnalysisOutput output) {
        this.output = output;
    }

    public List getNRWords() {
        return nrWords;
    }

    public void setNRWords(List<AnalysisOutput> words) {
        this.nrWords = words;
    }

    public void addNRWord(String word) {
        addNRWord(word, AnalysisOutput.SCORE_CORRECT);
    }

    public void addNRWord(String word, int score) {
        AnalysisOutput output = new AnalysisOutput(word, null, null, PatternConstants.PTN_N, score);
        output.setSource(word);
        output.setPos(PatternConstants.POS_NOUN);
        this.nrWords.add(0, output);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    /** 분석된 전체 단어의 길이를 반환한다. */
    public int getLength() {
        return (this.source == null) ? 0 : this.source.length();
    }
}
