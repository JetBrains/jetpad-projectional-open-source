/*
 * Copyright 2012-2016 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.grammar;


import com.google.common.collect.Range;

import java.util.List;

public interface RuleContext {
  ParserParameters getParams();
  Object get(int index);
  int getValueCount();

  <ValueT> ValueT get(ParserParameter<ValueT> key);

  Range<Integer> getRange();
  List getLexemesValues();
}