/*
 * Copyright 2012-2014 JetBrains s.r.o
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
package jetbrains.jetpad.hybrid.testapp.mapper;

import jetbrains.jetpad.hybrid.PairSpec;
import jetbrains.jetpad.hybrid.parser.Token;

class ExprHybridPairSpec implements PairSpec {
  @Override
  public boolean isLeft(Token t) {
    return t == Tokens.LP || t == Tokens.LP_CALL;
  }

  @Override
  public boolean isRight(Token t) {
    return t == Tokens.RP;
  }

  @Override
  public boolean isPair(Token l, Token r) {
    return (l == Tokens.LP || l == Tokens.LP_CALL) && r == Tokens.RP;
  }
}