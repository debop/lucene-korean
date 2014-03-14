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
import org.apache.lucene.analysis.kr.morph.MorphException;
import org.apache.lucene.analysis.kr.morph.WordSpaceAnalyzer;
import org.apache.lucene.analysis.kr.utils.DictionaryUtil;
import org.apache.lucene.analysis.kr.utils.SyllableUtil;

import java.util.List;

public class SyllableTest extends TestCase {

    public void testWordSpace() throws Exception {

        String[] strs = new String[] {
                "하지만푸미폰국왕의신격화를지난30년동안제도적으로보호하고언론에재갈을물린시대착오적왕실모독죄는안팎으로도전을받고있다", //0
                "올해크리스마스에는눈이내리지않고비교적포근할전망이다", //1
                "갑근세원천징수에관한질의", //2
                "갑근세원천징수질의", //3
                "본시스템은자동띄워쓰기데모입니다", //4
                "식습관과사회구조변화로", //5
                "지난해사교육시장이여타서비스업에비해", //6
                "자바한글형태소분석기데모", //7
                "과연무엇이문제일까", //8
                "루씬한글분석기오픈소스프로젝트", //9
                "질의편지", //10
                "관광자원을전략적으로개발합니다", //11
                "사람은밥을먹고삽니다", //12
                "어떻게이런일이있을수있는것입니까", //13
                "악취까지남김없이", // 14
                "2기경제팀내수고용대책급물살", // 15
                "재계해외사업체질개선박차", // 16
                "안녕하세요저는황아영입니다",//17
                "나는자랑스러운태극기앞에조국과민족의무궁한영황을위하여몸과마음을받쳐충성을다할것을굳게다짐합니다", //18
                "추출해낼수있으며", // 19
                "터질것같은나의심장이", //20
                "정성스러운게",//21
                "띄워쓰면서" //22
        };

        WordSpaceAnalyzer wsAnal = new WordSpaceAnalyzer();

//		for(String str:strs) {
//			List<AnalysisOutput> list = wsAnal.analyze(str);
//
//			for(AnalysisOutput o: list) {
////				System.out.println(o.getSource()+"<"+o+"("+o.getScore()+")> ");
//				System.out.print(o.getSource()+" ");
//			}
//			System.out.println("");
//		}


        List<AnalysisOutput> list = wsAnal.analyze(strs[22]);

        for (AnalysisOutput o : list) {
            System.out.println(o.getSource() + "<" + o + "(" + o.getScore() + ")> ");
//			System.out.print(o.getSource()+" ");
        }
        System.out.println("");


    }

    public void testSplitWord() throws Exception {

        String str = "올해크리스마스에는눈이내리지않고비교적포근할전망이다";
        char[] chrs = str.toCharArray();


        //StringBuilder word = new StringBuilder();
        for (int ws = 0, es = 1, ee = 0; es < chrs.length; ) {
            char[] f = SyllableUtil.getFeature(chrs[es]);
            if (f[SyllableUtil.IDX_JOSA1] == '1') {

                ee = guessJosa(str, chrs, ws, es);
                if (es != ee) {
                    System.out.println(str.substring(ws, ee));
                    ws = ee;
                    es = ee + 1;
                    continue;
                }
            }

            if (f[SyllableUtil.IDX_EOGAN] == '1') {
                ee = guessEomi(str, chrs, ws, es);
                if (es != ee) {
                    System.out.println(str.substring(ws, ee));
                    ws = ee;
                    es = ee + 1;
                    continue;
                }
            }

            es += 1;
        }
    }

    private int guessJosa(String str, char[] chrs, int ws, int es) throws MorphException {

        int ne = es;

        if (DictionaryUtil.existJosa(str.substring(es, es + 1))) ne++;
        for (int i = ne; i < str.length(); i++) {
            char[] f = SyllableUtil.getFeature(chrs[i]);
            if (f[SyllableUtil.IDX_JOSA2] != '1') break;
            if (DictionaryUtil.existJosa(str.substring(es, i + 1))) ne = i + 1;
        }
        return ne;
    }

    private int guessEomi(String str, char[] chrs, int ws, int es) throws MorphException {
        System.out.println("guessEomi->" + str.substring(ws, es));
        int ne = es + 1;

        for (int i = ne; i < str.length(); i++) {
            char[] f = SyllableUtil.getFeature(chrs[i]);
            if (f[SyllableUtil.IDX_EOGAN] != '1') break;
            ne++;
        }

        for (int i = ne - 1; i > ws; i--) {

        }

        return ne;
    }


}
