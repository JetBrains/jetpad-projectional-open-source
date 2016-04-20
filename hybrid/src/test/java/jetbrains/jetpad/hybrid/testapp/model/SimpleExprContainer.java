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
package jetbrains.jetpad.hybrid.testapp.model;

import jetbrains.jetpad.hybrid.HybridEditorSpecUtil;
import jetbrains.jetpad.hybrid.HybridProperty;
import jetbrains.jetpad.hybrid.ParsingHybridProperty;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprHybridEditorSpec;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;

public class SimpleExprContainer extends ExprNode {
  private final ExprHybridEditorSpec myEditorSpec = new ExprHybridEditorSpec();

  public final ObservableList<Token> sourceTokens = new ObservableArrayList<>();

  public final HybridProperty<Expr> expr = new ParsingHybridProperty<>(
      myEditorSpec.getParser(), myEditorSpec.getPrettyPrinter(),
      sourceTokens, HybridEditorSpecUtil.getParsingContextFactory(myEditorSpec));
}
