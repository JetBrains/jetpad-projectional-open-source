/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.event.awt;

import jetbrains.jetpad.event.Key;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

class KeyCodeMapper {
  private static final Map<Integer, Key> ourKeysCodeToKeyMap = new HashMap<Integer, Key>();
  private static final Map<Key, Integer> ourKeyToKeyCodeMap = new HashMap<Key, Integer>();

  KeyCodeMapper() {
  }

  private static void register(int code, Key key) {
    ourKeyToKeyCodeMap.put(key, code);
    ourKeysCodeToKeyMap.put(code, key);
  }

  static Key keyByKeyCode(int keyCode) {
    Key result = ourKeysCodeToKeyMap.get(keyCode);
    if (result == null) {
      return Key.UNKNOWN;
    }
    return result;
  }

  static int keyCodeByKey(Key key) {
    Integer result = ourKeyToKeyCodeMap.get(key);
    if (result == null) {
      return -1;
    }
    return result;
  }

  static {
    register(KeyEvent.VK_A, Key.A);
    register(KeyEvent.VK_B, Key.B);
    register(KeyEvent.VK_C, Key.C);
    register(KeyEvent.VK_D, Key.D);
    register(KeyEvent.VK_E, Key.E);
    register(KeyEvent.VK_F, Key.F);
    register(KeyEvent.VK_G, Key.G);
    register(KeyEvent.VK_H, Key.H);
    register(KeyEvent.VK_I, Key.I);
    register(KeyEvent.VK_J, Key.J);
    register(KeyEvent.VK_K, Key.K);
    register(KeyEvent.VK_L, Key.L);
    register(KeyEvent.VK_M, Key.M);
    register(KeyEvent.VK_N, Key.N);
    register(KeyEvent.VK_O, Key.O);
    register(KeyEvent.VK_P, Key.P);
    register(KeyEvent.VK_Q, Key.Q);
    register(KeyEvent.VK_R, Key.R);
    register(KeyEvent.VK_S, Key.S);
    register(KeyEvent.VK_T, Key.T);
    register(KeyEvent.VK_U, Key.U);
    register(KeyEvent.VK_V, Key.V);
    register(KeyEvent.VK_W, Key.W);
    register(KeyEvent.VK_X, Key.X);
    register(KeyEvent.VK_Y, Key.Y);
    register(KeyEvent.VK_Z, Key.Z);

    register(KeyEvent.VK_0, Key.DIGIT_0);
    register(KeyEvent.VK_1, Key.DIGIT_1);
    register(KeyEvent.VK_2, Key.DIGIT_2);
    register(KeyEvent.VK_3, Key.DIGIT_3);
    register(KeyEvent.VK_4, Key.DIGIT_4);
    register(KeyEvent.VK_5, Key.DIGIT_5);
    register(KeyEvent.VK_6, Key.DIGIT_6);
    register(KeyEvent.VK_7, Key.DIGIT_7);
    register(KeyEvent.VK_8, Key.DIGIT_8);
    register(KeyEvent.VK_9, Key.DIGIT_9);

    register(KeyEvent.VK_LEFT, Key.LEFT);
    register(KeyEvent.VK_RIGHT, Key.RIGHT);
    register(KeyEvent.VK_UP, Key.UP);
    register(KeyEvent.VK_DOWN, Key.DOWN);
    register(KeyEvent.VK_PAGE_DOWN, Key.PAGE_DOWN);
    register(KeyEvent.VK_PAGE_UP, Key.PAGE_UP);
    register(KeyEvent.VK_ESCAPE, Key.ESCAPE);
    register(KeyEvent.VK_HOME, Key.HOME);
    register(KeyEvent.VK_END, Key.END);
    register(KeyEvent.VK_ENTER, Key.ENTER);
    register(KeyEvent.VK_TAB, Key.TAB);
    register(KeyEvent.VK_SPACE, Key.SPACE);
    register(KeyEvent.VK_INSERT, Key.INSERT);
    register(KeyEvent.VK_DELETE, Key.DELETE);
    register(KeyEvent.VK_BACK_SPACE, Key.BACKSPACE);
    register(KeyEvent.VK_EQUALS, Key.EQUALS);
    register(KeyEvent.VK_PLUS, Key.PLUS);
    register(KeyEvent.VK_MINUS, Key.MINUS);

    register(KeyEvent.VK_CONTROL, Key.CONTROL);
    register(KeyEvent.VK_ALT, Key.ALT);
    register(KeyEvent.VK_SHIFT, Key.SHIFT);
    register(KeyEvent.VK_META, Key.META);

    register(KeyEvent.VK_F1, Key.F1);
    register(KeyEvent.VK_F2, Key.F2);
    register(KeyEvent.VK_F3, Key.F3);
    register(KeyEvent.VK_F4, Key.F4);
    register(KeyEvent.VK_F5, Key.F5);
    register(KeyEvent.VK_F6, Key.F6);
    register(KeyEvent.VK_F7, Key.F7);
    register(KeyEvent.VK_F8, Key.F8);
    register(KeyEvent.VK_F9, Key.F9);
    register(KeyEvent.VK_F10, Key.F10);
    register(KeyEvent.VK_F11, Key.F11);
  }
}