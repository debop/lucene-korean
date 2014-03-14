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
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.kr.KoreanAnalyzer;
import org.apache.lucene.analysis.kr.KoreanFilter;
import org.apache.lucene.analysis.kr.utils.HanjaUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Version;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class KoreanAnalyzerTest extends TestCase {

    /**
     * t.getPositionIncrement() 는 같은 단어에서 추출되었는지, 다른 단어에서 추출되었는지를 알려준다.
     * 즉 1이면 현재의 색인어는 새로운 단어에서 추출된 것이고
     * 0 이면 이전 색인어와 같은 단어에서 추출된 것이다.
     * 이 값은 검색 랭킹에 영향을 미친다.  즉 값이 작으면 검색랭킹이 올라가서 검색시 상위에 올라오게 된다.
     *
     * @throws Exception
     */
    public void testKoreanTokenizer() throws Exception {

        List<String> sources = new ArrayList<>();
        sources.add("우리나라라면에서부터 일본라면이 파생되었잖니?");
        sources.add("呵呵大笑 가교복합체와 가공액을 포함하였다.");
        sources.add("아딸떡볶이");
        sources.add("너는 너는 다시 내게 돌아 올거야. school is a good place 呵呵大笑 呵呵大笑");
        sources.add(" \"ASP.NET 웹 어플리케이션은 어플리케이션 Lifecycle, Page의 Lifecycle 에 상세한 event 를 정의하고 있어, event handler를 정의하면, 여러가지 선처리나 후처리를 수행할 수 있습니다.\\n\" +\n" +
                            "            \"Spring MVC 에서는 어떻게 하나 봤더니 Controller 에 Interceptor 를 등록하면 되더군요.\\n\" +\n" +
                            "            \"단계를 요약하자면...\\n\" +\n" +
                            "            \"org.springframework.web.servlet.HandlerInterceptor 또는 org.springframework.web.servlet.handler.HandlerInterceptorAdapter 를 상속받아 preHandler, postHandler, afterComletion 등에 원하는 작업을 구현합니다.\\n\" +\n" +
                            "            \"servlet.xml 에 위에서 작성한 Interceptor 를 등록합니다.\\n\" +\n" +
                            "            \"아주 쉽죠?\\n\" +\n" +
                            "            \"그럼 실제 예제와 함께 보시죠. 예제는 Spring Framework 3.2.1.RELEASE 와 Hibernate 4.1.9 Final 로 제작했습니다.\\n\" +\n" +
                            "            \"UnitOfWorkInterceptor 는 사용자 요청이 있으면 Start 하고, 요청 작업이 완료되면 Close 하도록 합니다. 이는 Hibernate 를 이용하여 Unit Of Work 패턴을 구현하여, 하나의 요청 중에 모든 작업을 하나의 Transaction으로 묶을 수 있고, 웹 개발자에게는 Unit Of Work 자체를 사용하기만 하면 되고, 실제 Lifecycle 은 Spring MVC 에서 관리하도록 하기 위해서입니다.\"");

        KoreanAnalyzer analyzer = new KoreanAnalyzer(Version.LUCENE_36);
        analyzer.setHasOrigin(false);

        for (String source : sources) {
            System.out.println("--------------------------");
            System.out.println("Analyze source : " + source);
            System.out.println("--------------------------");
            TokenStream stream = analyzer.tokenStream("s", new StringReader(source));

            long start = System.currentTimeMillis();

            while (stream.incrementToken()) {
                CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
                OffsetAttribute offAttr = stream.getAttribute(OffsetAttribute.class);
                PositionIncrementAttribute posAttr = stream.getAttribute(PositionIncrementAttribute.class);
                TypeAttribute typeAttr = stream.getAttribute(TypeAttribute.class);

                System.out.println(new String(termAttr.buffer(), 0, termAttr.length()));
            }

            System.out.println((System.currentTimeMillis() - start) + "ms");
        }
    }

    public void testStandardTokenizer() throws Exception {

        String source = "우리나라라면에서부터 일본라면이 파생되었잖니?";
        source = "너는 너는 다시 내게 돌아 올거야. school is a good place 呵呵大笑 呵呵大笑";

        long start = System.currentTimeMillis();

        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
        TokenStream stream = analyzer.tokenStream("s", new StringReader(source));
        TokenStream tok = new StandardFilter(Version.LUCENE_36, stream);

        while (tok.incrementToken()) {
            CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
            OffsetAttribute offAttr = stream.getAttribute(OffsetAttribute.class);
            PositionIncrementAttribute posAttr = stream.getAttribute(PositionIncrementAttribute.class);
            TypeAttribute typeAttr = stream.getAttribute(TypeAttribute.class);

            System.out.println(new String(termAttr.buffer(), 0, termAttr.length()));
        }

        System.out.println((System.currentTimeMillis() - start) + "ms");
    }


    public void testJavaEscape() throws Exception {

        String str = StringEscapeUtils.unescapeHtml4("&#48085;");
        System.out.println(str);

        //落落長松
        String han = StringEscapeUtils.unescapeJava("0x3400");
        han = StringEscapeUtils.escapeJava("落");

        System.out.println(han);

    }

    public void testConvertHanja() throws Exception {

        String han = "呵呵大笑";

        for (int jj = 0; jj < han.length(); jj++) {
            char[] result = HanjaUtils.convertToHangul(han.charAt(jj));
            for (char c : result)
                System.out.print(c);

            System.out.println();
        }
    }

    public void testHanjaConvert() throws Exception {

        String source = "呵呵大笑  落落長松 ";

        long start = System.currentTimeMillis();

        KoreanAnalyzer analyzer = new KoreanAnalyzer();
        TokenStream stream = analyzer.tokenStream("s", new StringReader(source));
        TokenStream tok = new KoreanFilter(stream);

        while (tok.incrementToken()) {
            CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
            OffsetAttribute offAttr = stream.getAttribute(OffsetAttribute.class);
            PositionIncrementAttribute posAttr = stream.getAttribute(PositionIncrementAttribute.class);
            TypeAttribute typeAttr = stream.getAttribute(TypeAttribute.class);

            System.out.println(new String(termAttr.buffer()));
        }

        System.out.println((System.currentTimeMillis() - start) + "ms");
    }

}
