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
package jetbrains.jetpad.hybrid.parser;

import jetbrains.jetpad.values.Color;

public class SimpleToken extends BaseToken {
  private String myName;
  private boolean myNoSpaceLeft;
  private boolean myNoSpaceRight;
  private boolean myRtOnEnd;

  public SimpleToken() {
    this(null);
  }

  public SimpleToken(String name) {
    this(name, false, false);
  }

  public SimpleToken(String name, boolean noSpaceLeft, boolean noSpaceRight) {
    this(name, false, noSpaceLeft, noSpaceRight);
  }

  public SimpleToken(String name, boolean rtOnEnd, boolean noSpaceLeft, boolean noSpaceRight) {
    myName = name;
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
    return myName;
  }

  @Override
  public String toString() {
    return myName;
  }
}