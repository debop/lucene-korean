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

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MorphAnalyzerManager {

    public void analyze(String strs) {
        if (log.isTraceEnabled())
            log.trace("analyze strs=[{}]", strs);

        MorphAnalyzer analyzer = new MorphAnalyzer();
        String[] tokens = strs.split(" ");

        for (String token : tokens) {
            try {
                List<AnalysisOutput> results = analyzer.analyze(token);
                for (AnalysisOutput o : results) {
                    if (log.isDebugEnabled()) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < o.getCNounList().size(); i++) {
                            sb.append(o.getCNounList().get(i)).append("/");
                        }
                        log.debug("[{}]->[{}]/<[{}]>", o, sb.toString(), o.getScore());
                    }
                }
            } catch (MorphException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
