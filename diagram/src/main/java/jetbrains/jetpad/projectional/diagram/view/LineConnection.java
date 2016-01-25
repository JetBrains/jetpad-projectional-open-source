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
package jetbrains.jetpad.projectional.diagram.view;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.LineView;
import jetbrains.jetpad.values.Color;

public class LineConnection implements Connection {
  private LineView myLine = new LineView();
  private TargetViewProperty myStart = new TargetViewProperty();
  private TargetViewProperty myEnd = new TargetViewProperty();

  public Property<View> start() {
    return myStart;
  }

  public Property<View> end() {
    return myEnd;
  }

  public Property<Color> color() {
    return myLine.color();
  }

  @Override
  public LineView view() {
    return myLine;
  }

  @Override
  public void attach() {
    myStart.attach();
    myEnd.attach();
    update();
  }

  @Override
  public void detach() {
    myStart.detach();
    myEnd.detach();
  }

  private void update() {
    if (myStart.get() != null) myLine.start().set(location(myStart.get()));
    if (myEnd.get() != null) myLine.end().set(location(myEnd.get()));
  }

  private Vector location(View view) {
    return view.bounds().get().center();
  }

  private class TargetViewProperty extends ValueProperty<View> {
    private Registration myBoundsReg;

    @Override
    protected void fireEvents(View oldValue, View newValue) {
      unwatchBounds();
      watchBounds(newValue);
      super.fireEvents(oldValue, newValue);
    }

    void attach() {
      View view = get();
      watchBounds(view);
    }

    void detach() {
      unwatchBounds();
    }

    private void watchBounds(View view) {
      if (view != null) {
        myBoundsReg = view.bounds().addHandler(new EventHandler<PropertyChangeEvent<Rectangle>>() {
          @Override
          public void onEvent(PropertyChangeEvent<Rectangle> event) {
            update();
          }
        });
      }
    }

    private void unwatchBounds() {
      if (myBoundsReg != null) {
        myBoundsReg.remove();
        myBoundsReg = null;
      }
    }
  }
}