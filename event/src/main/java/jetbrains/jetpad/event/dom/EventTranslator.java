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
package jetbrains.jetpad.event.dom;

import com.google.gwt.user.client.Event;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.event.ModifierKey;

import java.util.HashSet;
import java.util.Set;

public class EventTranslator {
  private static KeyEvent toKeyEvent(Event e) {
    Key key = KeyCodeMapper.getKey(e.getKeyCode());

    Set<ModifierKey> modifiers = new HashSet<>();
    if (e.getCtrlKey()) {
      modifiers.add(ModifierKey.CONTROL);
    }
    if (e.getAltKey()) {
      modifiers.add(ModifierKey.ALT);
    }
    if (e.getShiftKey()) {
      modifiers.add(ModifierKey.SHIFT);
    }
    if (e.getMetaKey()) {
      modifiers.add(ModifierKey.META);
    }
    return new KeyEvent(key, (char) e.getCharCode(), modifiers);
  }

  public static boolean dispatchKeyPress(Event e, Handler<KeyEvent> handler) {
    final KeyEvent event = EventTranslator.toKeyEvent(e);
    handler.handle(event);

    //disable back button
    if (event.getKey() == Key.BACKSPACE) return false;

    //disable navigation keys to prevent browser scrolling
    if (event.getKey() == Key.UP || event.getKey() == Key.DOWN || event.getKey() == Key.LEFT || event.getKey() == Key.RIGHT) return false;

    //disable space scrolling in case of unhandled space
    if (event.getKey() == Key.SPACE) return false;

    //disable shift+arrow selection
    if (event.is(KeyStrokeSpecs.SELECT_BEFORE) || event.is(KeyStrokeSpecs.SELECT_AFTER)) return false;

    //disable back forward with Ctrl/Cmd + [ / ]
    if (event.is(KeyStrokeSpecs.MATCHING_CONSTRUCTS)) return false;

    //disable tab navigation
    if (event.is(Key.TAB) || event.is(Key.TAB, ModifierKey.SHIFT)) return false;

    return !event.isConsumed();
  }

  public static boolean dispatchKeyRelease(Event e, Handler<KeyEvent> handler) {
    final KeyEvent event = EventTranslator.toKeyEvent(e);
    handler.handle(event);
    return !event.isConsumed();
  }

  public static boolean dispatchKeyType(Event e, Handler<KeyEvent> handler) {
    final KeyEvent event = EventTranslator.toKeyEvent(e);

    if (e.getCharCode() == 0) return true;
    if (e.getCharCode() == '\n') return true;
    if (e.getCharCode() == '\r') return true;
    if (event.has(ModifierKey.META) || event.has(ModifierKey.ALT) || event.has(ModifierKey.CONTROL)) return true;

    handler.handle(event);

    return !event.isConsumed();
  }
}