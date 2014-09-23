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
package jetbrains.jetpad.projectional.diagram.view;

import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.view.*;

public class RootTrait {
  public static final ViewPropertySpec<MoveHandler> MOVE_HANDLER = new ViewPropertySpec<>("moveHandler");
  public static final ViewPropertySpec<DeleteHandler> DELETE_HANDLER = new ViewPropertySpec<>("deleteHandler");

  public static final ViewTrait ROOT_TRAIT;

  static {
    final ViewPropertySpec<Vector> prevPoint = new ViewPropertySpec<>("prevLocation");
    final ViewPropertySpec<MoveHandler> moveHandler = new ViewPropertySpec<>("currentMoveHandler");

    ViewTraitBuilder builder = new ViewTraitBuilder();
    builder.on(ViewEvents.MOUSE_PRESSED, new ViewEventHandler<MouseEvent>() {
      @Override
      public void handle(View view, MouseEvent e) {
        view.getProp(prevPoint).set(e.getLocation());
        View current = view.viewAt(e.getLocation());

        while (current != null) {
          MoveHandler mh = current.getProp(MOVE_HANDLER).get();
          view.getProp(moveHandler).set(mh);
          if (mh != null) break;
          current = current.parent().get();
        }

        ViewContainer container = view.container();
        View target = container.root().viewAt(e.getLocation());
        while (target != null) {
          if (target.focusable().get()) {
            container.focusedView().set(target);
            e.consume();
            return;
          }
          target = target.parent().get();
        }

        container.focusedView().set(null);
        e.consume();
      }
    });

    builder.on(ViewEvents.MOUSE_RELEASED, new ViewEventHandler<MouseEvent>() {
      @Override
      public void handle(View view, MouseEvent e) {
        view.getProp(prevPoint).set(null);
        view.getProp(moveHandler).set(null);
      }
    });

    builder.on(ViewEvents.MOUSE_DRAGGED, new ViewEventHandler<MouseEvent>() {
      @Override
      public void handle(View view, MouseEvent e) {
        MoveHandler mh = view.getProp(moveHandler).get();
        if (mh != null) {
          mh.move(e.getLocation().sub(view.getProp(prevPoint).get()));
        }
        view.getProp(prevPoint).set(e.getLocation());
      }
    });

    builder.on(ViewEvents.KEY_PRESSED, new ViewEventHandler<KeyEvent>() {
      @Override
      public void handle(View view, KeyEvent e) {
        if (e.is(Key.DELETE) || e.is(Key.BACKSPACE)) {
          ViewContainer vc = view.container();
          View current = vc.focusedView().get();

          while (current != null) {
            DeleteHandler dh = current.getProp(DELETE_HANDLER).get();
            if (dh != null && dh.canDelete()) {
              dh.delete();
              e.consume();
              return;
            }

            current = current.parent().get();
          }
        }
      }
    });

    ROOT_TRAIT = builder.build();
  }
}