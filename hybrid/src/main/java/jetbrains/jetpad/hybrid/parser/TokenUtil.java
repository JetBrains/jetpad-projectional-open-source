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
package jetbrains.jetpad.hybrid.parser;

import java.util.List;

public class TokenUtil {
  public static String getText(List<Token> tokens) {
    StringBuilder text = new StringBuilder();
    Token prevToken = null;
    for (Token currToken : tokens) {
      if (prevToken != null && !prevToken.noSpaceToRight() && !currToken.noSpaceToLeft()) {
        text.append(' ');
      }
      // NB: Token.text() may throw UnsupportedOperationException
      text.append(currToken.text());
      prevToken = currToken;
    }
    return text.toString();
  }

  private TokenUtil() {
  }
}
