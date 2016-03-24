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

import jetbrains.jetpad.values.Color;

public class SimpleToken extends BaseToken {
  private final String myText;
  private final boolean myNoSpaceLeft;
  private final boolean myNoSpaceRight;
  private final boolean myRtOnEnd;

  public SimpleToken(String text) {
    this(text, false, false);
  }

  public SimpleToken(String text, boolean noSpaceLeft, boolean noSpaceRight) {
    this(text, false, noSpaceLeft, noSpaceRight);
  }

  public SimpleToken(String text, boolean rtOnEnd, boolean noSpaceLeft, boolean noSpaceRight) {
    if (text == null) {
      throw new NullPointerException();
    }
    myText = text;
    myNoSpaceLeft = noSpaceLeft;
    myNoSpaceRight = noSpaceRight;
    myRtOnEnd = rtOnEnd;
  }

  @Override
  public boolean noSpaceToLeft() {
    return myNoSpaceLeft;
  }

  @Override
  public boolean noSpaceToRight() {
    return myNoSpaceRight;
  }

  @Override
  public boolean isRtOnEnd() {
    return myRtOnEnd;
  }

  public boolean isBold() {
    return false;
  }

  public Color getColor() {
    return Color.BLACK;
  }

  @Override
  public String text() {
    return myText;
  }

  @Override
  public String toString() {
    return myText;
  }
}