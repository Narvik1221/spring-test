/**
 * Copyright 2009 Alexander Kuznetsov
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

package org.apache.lucene.morphology.dictionary;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GrammarReader {
    private String fileName;
    private String fileEncoding = "windows-1251";
    private List<String> grammarInfo = new ArrayList<>();
    private Map<String, Integer> inverseIndex = new HashMap<>();

    public GrammarReader(String fileName) throws IOException {
        this.fileName = fileName;
        setUp();
    }

    public GrammarReader(String fileName, String fileEncoding) throws IOException {
        this.fileName = fileName;
        this.fileEncoding = fileEncoding;
        setUp();
    }

    private void setUp() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), fileEncoding));
        String line = bufferedReader.readLine();
        while (line != null) {
            line = line.trim();
            if (!line.startsWith("//") && line.length() > 0) {
                String[] strings = line.split(" ", 2);
                int i = grammarInfo.size();
                inverseIndex.put(strings[0], i);
                grammarInfo.add(i, strings[1]);
            }
            line = bufferedReader.readLine();
        }
    }

    public List<String> getGrammarInfo() {
        return grammarInfo;
    }

    public String[] getGrammarInfoAsArray() {
        return grammarInfo.toArray(new String[0]);
    }

    public Map<String, Integer> getGrammarInverseIndex() {
        return inverseIndex;
    }
}
