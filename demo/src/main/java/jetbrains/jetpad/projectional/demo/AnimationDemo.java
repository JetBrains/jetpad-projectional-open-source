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
package jetbrains.jetpad.projectional.demo;

import jetbrains.jetpad.base.animation.Animation;
import jetbrains.jetpad.base.animation.TimerAnimation;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.view.*;
import jetbrains.jetpad.projectional.view.toAwt.AwtViewDemo;
import jetbrains.jetpad.values.Color;

public class AnimationDemo {
  public static void main(String[] args) {
    ViewContainer container = createContainer();

    AwtViewDemo.show(container);
  }

  private static ViewContainer createContainer() {
    final ViewContainer container = new ViewContainer();

    final RectView rect = new RectView();
    rect.background().set(Color.RED);
    rect.dimension().set(new Vector(50, 50));
    rect.moveTo(new Vector(100, 100));
    container.contentRoot().children().add(rect);

    rect.addTrait(new ViewTraitBuilder()
      .on(ViewEvents.MOUSE_PRESSED, new ViewEventHandler<MouseEvent>() {
        private Animation myAnimation;

        @Override

        public void handle(View view, MouseEvent e) {
          if (myAnimation != null) {
            myAnimation.stop();
          } else {
            myAnimation = new TimerAnimation(container.getEdt(), 100, 100) {
              @Override
              protected void animateFrame(int frame, boolean lastFrame) {
                if (frame <= 50) {
                  rect.moveTo(new Vector(100, 100 + frame * 5));
                } else {
                  rect.moveTo(new Vector(100, 350 - (frame - 50) * 5));
                }
              }
            };
            myAnimation.whenDone(new Runnable() {
              @Override
              public void run() {
                myAnimation = null;
              }
            });
          }
        }
      }).build());



    return container;
  }
}