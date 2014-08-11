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
package jetbrains.jetpad.event;

public enum Key {
  A("A"),
  B("B"),
  C("C"),
  D("D"),
  E("E"),
  F("F"),
  G("G"),
  H("H"),
  I("I"),
  J("J"),
  K("K"),
  L("L"),
  M("M"),
  N("N"),
  O("O"),
  P("P"),
  Q("Q"),
  R("R"),
  S("S"),
  T("T"),
  U("U"),
  V("V"),
  W("W"),
  X("X"),
  Y("Y"),
  Z("Z"),
  DIGIT_0("0"),
  DIGIT_1("1"),
  DIGIT_2("2"),
  DIGIT_3("3"),
  DIGIT_4("4"),
  DIGIT_5("5"),
  DIGIT_6("6"),
  DIGIT_7("7"),
  DIGIT_8("8"),
  DIGIT_9("9"),
  LEFT_BRACE("["),
  RIGHT_BRACE("]"),
  LEFT_PAREN("("),
  RIGHT_PAREN(")"),
  UP("Up"),
  DOWN("Down"),
  LEFT("Left"),
  RIGHT("Right"),
  PAGE_UP("Page Up"),
  PAGE_DOWN("Page Down"),
  ESCAPE("Escape"),
  ENTER("Enter"),
  HOME("Home"),
  END("End"),
  TAB("Tab"),
  SPACE("Space"),
  INSERT("Insert"),
  DELETE("Delete"),
  BACKSPACE("Backspace"),
  EQUALS("Equals"),
  PLUS("Plus"),
  MINUS("Minus"),
  CONTROL("Ctrl"),
  META("Meta"),
  ALT("Alt"),
  SHIFT("Shift"),
  UNKNOWN("?"),
  F1("F1"),
  F2("F2"),
  F3("F3"),
  F4("F4"),
  F5("F5"),
  F6("F6"),
  F7("F7"),
  F8("F8"),
  F9("F9"),
  F10("F10"),
  F11("F11"),
  F12("F12");

  private String myValue;

  Key(String value) {
    myValue = value;
  }

  @Override
  public String toString() {
    return myValue;
  }
}