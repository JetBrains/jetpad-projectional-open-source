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
package jetbrains.jetpad.event.dom;

import com.google.gwt.event.dom.client.KeyCodes;
import jetbrains.jetpad.event.Key;

import java.util.HashMap;
import java.util.Map;

class KeyCodeMapper {
  private static final Map<Integer, Key> ourKeyMap = new HashMap<>();

  static Key getKey(int code) {
    Key result = ourKeyMap.get(code);
    if (result != null) {
      return result;
    }
    return Key.UNKNOWN;
  }

  static {
    ourKeyMap.put(KeyCodes.KEY_ALT, Key.ALT);
    ourKeyMap.put(KeyCodes.KEY_BACKSPACE, Key.BACKSPACE);
    ourKeyMap.put(KeyCodes.KEY_CTRL, Key.CONTROL);
    ourKeyMap.put(KeyCodes.KEY_DELETE, Key.DELETE);
    ourKeyMap.put(KeyCodes.KEY_DOWN, Key.DOWN);
    ourKeyMap.put(KeyCodes.KEY_END, Key.END);
    ourKeyMap.put(KeyCodes.KEY_ENTER, Key.ENTER);
    ourKeyMap.put(KeyCodes.KEY_ESCAPE, Key.ESCAPE);
    ourKeyMap.put(KeyCodes.KEY_HOME, Key.HOME);
    ourKeyMap.put(KeyCodes.KEY_LEFT, Key.LEFT);
    ourKeyMap.put(KeyCodes.KEY_PAGEDOWN, Key.PAGE_DOWN);
    ourKeyMap.put(KeyCodes.KEY_PAGEUP, Key.PAGE_UP);
    ourKeyMap.put(KeyCodes.KEY_RIGHT, Key.RIGHT);
    ourKeyMap.put(KeyCodes.KEY_SHIFT, Key.SHIFT);
    ourKeyMap.put(KeyCodes.KEY_TAB, Key.TAB);
    ourKeyMap.put(KeyCodes.KEY_UP, Key.UP);
    ourKeyMap.put(45, Key.INSERT);
    ourKeyMap.put(32, Key.SPACE);

    ourKeyMap.put((int) 'A', Key.A);
    ourKeyMap.put((int) 'B', Key.B);
    ourKeyMap.put((int) 'C', Key.C);
    ourKeyMap.put((int) 'D', Key.D);
    ourKeyMap.put((int) 'E', Key.E);
    ourKeyMap.put((int) 'E', Key.E);
    ourKeyMap.put((int) 'F', Key.F);
    ourKeyMap.put((int) 'G', Key.G);
    ourKeyMap.put((int) 'H', Key.H);
    ourKeyMap.put((int) 'I', Key.I);
    ourKeyMap.put((int) 'J', Key.J);
    ourKeyMap.put((int) 'K', Key.K);
    ourKeyMap.put((int) 'L', Key.L);
    ourKeyMap.put((int) 'M', Key.M);
    ourKeyMap.put((int) 'N', Key.N);
    ourKeyMap.put((int) 'O', Key.O);
    ourKeyMap.put((int) 'P', Key.P);
    ourKeyMap.put((int) 'Q', Key.Q);
    ourKeyMap.put((int) 'R', Key.R);
    ourKeyMap.put((int) 'S', Key.S);
    ourKeyMap.put((int) 'T', Key.T);
    ourKeyMap.put((int) 'U', Key.U);
    ourKeyMap.put((int) 'V', Key.V);
    ourKeyMap.put((int) 'W', Key.W);
    ourKeyMap.put((int) 'X', Key.X);
    ourKeyMap.put((int) 'Y', Key.Y);
    ourKeyMap.put((int) 'Z', Key.Z);

    ourKeyMap.put(219, Key.LEFT_BRACE);
    ourKeyMap.put(221, Key.RIGHT_BRACE);

    ourKeyMap.put((int) '0', Key.DIGIT_0);
    ourKeyMap.put((int) '1', Key.DIGIT_1);
    ourKeyMap.put((int) '2', Key.DIGIT_2);
    ourKeyMap.put((int) '3', Key.DIGIT_3);
    ourKeyMap.put((int) '4', Key.DIGIT_4);
    ourKeyMap.put((int) '5', Key.DIGIT_5);
    ourKeyMap.put((int) '6', Key.DIGIT_6);
    ourKeyMap.put((int) '7', Key.DIGIT_7);
    ourKeyMap.put((int) '8', Key.DIGIT_8);
    ourKeyMap.put((int) '9', Key.DIGIT_9);

    ourKeyMap.put(112, Key.F1);
    ourKeyMap.put(113, Key.F2);
    ourKeyMap.put(114, Key.F3);
    ourKeyMap.put(115, Key.F4);
    ourKeyMap.put(116, Key.F5);
    ourKeyMap.put(117, Key.F6);
    ourKeyMap.put(118, Key.F7);
    ourKeyMap.put(119, Key.F8);
    ourKeyMap.put(120, Key.F9);
    ourKeyMap.put(121, Key.F10);
    ourKeyMap.put(122, Key.F11);
    ourKeyMap.put(123, Key.F12);
  }
}