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

import org.apache.lucene.morphology.LetterDecoderEncoder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class WordCleaner extends WordFilter {

    private LetterDecoderEncoder decoderEncoder;

    public WordCleaner(LetterDecoderEncoder decoderEncoder, WordProcessor wordProcessor) {
        super(wordProcessor);
        this.decoderEncoder = decoderEncoder;
    }

    public List<WordCard> transform(WordCard wordCard) {
        String word = wordCard.getBase() + wordCard.getCanonicalSuffix();

        if (word.contains("-")) return Collections.emptyList();
        if (!decoderEncoder.checkString(word)) return Collections.emptyList();

        List<FlexiaModel> flexiaModelsToRemove = new LinkedList<>();
        for (FlexiaModel fm : wordCard.getWordsForms()) {
            if (!decoderEncoder.checkString(fm.create(wordCard.getBase())) || fm.create(wordCard.getBase()).contains("-")) {
                flexiaModelsToRemove.add(fm);
            }
        }
        for (FlexiaModel fm : flexiaModelsToRemove) {
            wordCard.removeFlexia(fm);
        }

        return new LinkedList<>(Collections.singletonList(wordCard));
    }
}
