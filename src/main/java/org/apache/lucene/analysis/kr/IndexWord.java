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

import java.io.Serializable;

/**
 * Index word extracted from a phrase.
 *
 * @author lsm
 */
public class IndexWord implements Serializable {

    private static final long serialVersionUID = 28959989857060670L;

    private String word;
    private int offset = 0;

    public IndexWord() { }

    public IndexWord(String word, int pos) {
        this.word = word;
        this.offset = pos;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String toString() {
        return String.format("IndexWord# word=[%s], offset=[%d]", word, offset);
    }
}
