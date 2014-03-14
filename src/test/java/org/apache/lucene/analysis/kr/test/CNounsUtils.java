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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
public class CNounsUtils extends TestCase {

    public void testAdjust() throws Exception {

        List<String> strs = FileUtils.readLines(new File("cnouns_all.txt"));

        Map<String, String> compoundList = new TreeMap<String, String>();
        Map<String, String> nounList = new TreeMap<String, String>();

        for (String str : strs) {
            System.out.println(str);
            if (str == null || str.length() < 1) continue;

            str = str.trim();
            String[] infoNouns = StringUtils.split(str, ":");
            if (infoNouns.length == 2) {
                infoNouns[0] = infoNouns[0].trim();
                if (compoundList.get(infoNouns[0]) == null) compoundList.put(infoNouns[0], str);
            } else {
                if (compoundList.get(str) == null && str.length() > 1)
                    nounList.put(str, str);
            }
        }

        writeResult(compoundList, "compounds.txt");
        writeResult(nounList, "noun.txt");

    }

    private void writeResult(Map map, String fName) throws IOException {

        Iterator<String> iter = map.keySet().iterator();
        List list = new ArrayList();
        while (iter.hasNext()) {
            String str = iter.next();
            list.add(map.get(str));
        }

        FileUtils.writeLines(new File(fName), list);
    }

}
