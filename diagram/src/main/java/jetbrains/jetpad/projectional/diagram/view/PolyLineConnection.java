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
package jetbrains.jetpad.projectional.diagram.view;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Segment;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.view.GroupView;
import jetbrains.jetpad.projectional.view.LineView;
import jetbrains.jetpad.projectional.view.PolyLineView;
import jetbrains.jetpad.projectional.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PolyLineConnection implements Connection {
  private TargetViewProperty myFromView = new TargetViewProperty();
  private TargetViewProperty myToView = new TargetViewProperty();

  private Property<Vector> myFromLocation = new ValueProperty<>();
  private final Property<Vector> myToLocation = new ValueProperty<>();

  private GroupView myGroup = new GroupView();
  private PolyLineView myLinesView = new PolyLineView();

  private View myLabel;

  private List<Segment> myLines = new ArrayList<>();

  public PolyLineConnection() {
    this(new GroupView());
  }

  public PolyLineConnection(View labelView) {
    myLabel = labelView;

    myGroup.children().add(myLinesView);
    myGroup.children().add(myLabel);

    EventHandler<PropertyChangeEvent<View>> viewHandler = new EventHandler<PropertyChangeEvent<View>>() {
      @Override
      public void onEvent(PropertyChangeEvent<View> event) {
        view().invalidate();
      }
    };
    myFromView.addHandler(viewHandler);
    myToView.addHandler(viewHandler);
    EventHandler<PropertyChangeEvent<Vector>> locationHandler = new EventHandler<PropertyChangeEvent<Vector>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Vector> event) {
        view().invalidate();
      }
    };
    myFromLocation.addHandler(locationHandler);
    myToLocation.addHandler(locationHandler);
  }

  public Property<View> fromView() {
    return myFromView;
  }

  public Property<View> toView() {
    return myToView;
  }

  public Property<Vector> fromLocation() {
    return myFromLocation;
  }

  public Property<Vector> toLocation() {
    return myToLocation;
  }

  public View label() {
    return myLabel;
  }

  public Vector getFrom() {
    if (myFromView.get() == null) {
      return myFromLocation.get();
    } else {
      return myFromView.get().bounds().get().center();
    }
  }

  public Vector getTo() {
    if (myToView.get() == null) {
      return myToLocation.get();
    } else {
      return myToView.get().bounds().get().center();
    }
  }

  @Override
  public View view() {
    return myGroup;
  }

  @Override
  public void attach() {
    myFromView.attach();
    myToView.attach();
  }

  @Override
  public void detach() {
    myFromView.detach();
    myToView.detach();
  }

  public void update(Vector... points) {
    updateLines(points);
  }

  public List<Segment> getSegments() {
    return Collections.unmodifiableList(myLines);
  }

  private void updateLines(Vector[] points) {
    List<Vector> vectors = removeParallel(points);
    myLinesView.points.clear();
    myLinesView.points.addAll(vectors);

    myLines.clear();
    Vector prev = null;
    for (Vector p: vectors) {
      if (prev != null) {
        myLines.add(new Segment(prev, p));
      }
      prev = p;
    }
  }

  private List<Vector> removeParallel(Vector... points) {
    List<Vector> res = new ArrayList<>();
    Vector last = null;
    Vector prev = null;
    for (Vector p: points) {
      if (prev == null) {
        res.add(p);
      } else {
        if (last == null) {
          last = p.sub(prev).orthogonal();
        }
        if (last.dotProduct(p.sub(prev)) != 0) {
          last = p.sub(prev).orthogonal();
          res.add(prev);
        }
      }
      prev = p;
    }
    //last point
    res.add(prev);
    return res;
  }

  public List<LineView> getLines() {
    List<LineView> res = new ArrayList<>(myLinesView.children().size());
    for (View v: myLinesView.children()) {
      res.add((LineView) v);
    }
    return res;
  }

  private class TargetViewProperty extends ValueProperty<View> {
    private Registration myBoundsReg;

    @Override
    protected void fireEvents(View oldValue, View newValue) {
      unwatchBounds();
      watchBounds(newValue);
      super.fireEvents(oldValue, newValue);
    }

    private void attach() {
      View view = get();
      watchBounds(view);
    }

    private void detach() {
      unwatchBounds();
    }

    private void watchBounds(View view) {
      if (view != null) {
        myBoundsReg = view.bounds().addHandler(new EventHandler<PropertyChangeEvent<Rectangle>>() {
          @Override
          public void onEvent(PropertyChangeEvent<Rectangle> event) {
            view().invalidate();
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