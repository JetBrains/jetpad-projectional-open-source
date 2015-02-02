/*
 * Copyright 2012-2015 JetBrains s.r.o
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

import jetbrains.jetpad.hybrid.parser.SimpleToken;
import jetbrains.jetpad.hybrid.parser.Token;

public class Tokens {
  public static final Token ID = new SimpleToken("id");
  public static final Token PLUS = new SimpleToken("+");
  public static final Token INCREMENT = new SimpleToken("++");
  public static final Token MUL = new SimpleToken("*");
  public static final Token LP = new SimpleToken("(", false, true);
  public static final Token LP_CALL = new SimpleToken("(", true, true);
  public static final Token RP = new SimpleToken(")", true, false);
  public static final Token DOT = new SimpleToken(".", true, true, true);

  static boolean isLp(Token token) {
    return token == LP || token == LP_CALL;
  }
}