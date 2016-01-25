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

public class IntValueToken extends SimpleToken {
  private int myValue;

  public IntValueToken(int value) {
    myValue = value;
  }

  @Override
  public Color getColor() {
    return Color.BLUE;
  }

  public int getValue() {
    return myValue;
  }

  @Override
  public String text() {
    return "" + myValue;
  }

  @Override
  public String toString() {
    return text();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof IntValueToken)) return false;
    return ((IntValueToken) obj).myValue == myValue;
  }

  @Override
  public int hashCode() {
    return myValue;
  }
}