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
package jetbrains.jetpad.projectional.view.spi;

import jetbrains.jetpad.base.edt.EventDispatchThread;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewContainer;
import jetbrains.jetpad.base.animation.Animation;
import jetbrains.jetpad.values.Font;

public interface ViewContainerPeer {
  void attach(ViewContainer container);
  void detach();

  void repaint(View view);
  void scrollTo(Rectangle rect, View view);

  Rectangle visibleRect();

  void boundsChanged(View view, PropertyChangeEvent<Rectangle> change);

  int textHeight(Font font);
  int textBaseLine(Font font);
  int textWidth(Font font, String text);

  void requestFocus();

  Object getMappedTo(View view);

  EventDispatchThread getEdt();

  Animation fadeIn(View view, int duration);
  Animation fadeOut(View view, int duration);
  Animation showSlide(View view, int duration);
  Animation hideSlide(View view, int duration);
}