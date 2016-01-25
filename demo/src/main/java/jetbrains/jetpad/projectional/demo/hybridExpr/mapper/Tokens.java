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
package jetbrains.jetpad.projectional.demo.hybridExpr.mapper;

import jetbrains.jetpad.hybrid.parser.BoolValueToken;
import jetbrains.jetpad.hybrid.parser.SimpleToken;
import jetbrains.jetpad.hybrid.parser.Token;

class Tokens {
  static final Token PLUS = new SimpleToken("+");
  static final Token MINUS = new SimpleToken("-");
  static final Token MUL = new SimpleToken("*");
  static final Token DIV = new SimpleToken("/");
  static final Token COMMA = new SimpleToken(",", true, false);

  static final Token TRUE = new BoolValueToken(true);
  static final Token FALSE = new BoolValueToken(false);

  static final Token INCREMENT = new SimpleToken("++");
  static final Token INCREMENT_LEFT = new SimpleToken("++", false, true);
  static final Token INCREMENT_RIGHT = new SimpleToken("++", true, false);
  static final Token DECREMENT = new SimpleToken("--");
  static final Token DECREMENT_LEFT = new SimpleToken("--", false, true);
  static final Token DECREMENT_RIGHT = new SimpleToken("--", true, false);

  static final Token LEFT_PAREN = new SimpleToken("(", false, true);
  static final Token LEFT_PARENT_METHOD_CALL = new SimpleToken("(", true, true);
  static final Token RIGHT_PAREN = new SimpleToken(")", true, false);

  static final Token DOT = new SimpleToken(".", true, true, true);
}