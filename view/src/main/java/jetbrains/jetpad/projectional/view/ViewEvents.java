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
package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.event.CopyCutEvent;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.event.PasteEvent;

public class ViewEvents {
  public final static ViewEventSpec<MouseEvent> MOUSE_PRESSED = new ViewEventSpec<MouseEvent>("mousePressed");
  public final static ViewEventSpec<MouseEvent> MOUSE_RELEASED = new ViewEventSpec<MouseEvent>("mouseReleased");
  public final static ViewEventSpec<MouseEvent> MOUSE_DRAGGED = new ViewEventSpec<MouseEvent>("mouseDragged");
  public final static ViewEventSpec<MouseEvent> MOUSE_MOVED = new ViewEventSpec<MouseEvent>("mouseMoved");

  public final static ViewEventSpec<KeyEvent> KEY_PRESSED = new ViewEventSpec<KeyEvent>("keyPressed");
  public final static ViewEventSpec<KeyEvent> KEY_RELEASED = new ViewEventSpec<KeyEvent>("keyReleased");
  public final static ViewEventSpec<KeyEvent> KEY_TYPED = new ViewEventSpec<KeyEvent>("keyTyped");

  public static final ViewEventSpec<CopyCutEvent> COPY = new ViewEventSpec<CopyCutEvent>("copy");
  public static final ViewEventSpec<CopyCutEvent> CUT = new ViewEventSpec<CopyCutEvent>("cut");
  public static final ViewEventSpec<PasteEvent> PASTE = new ViewEventSpec<PasteEvent>("cut");
}