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
package jetbrains.mps.diagram.dataflow.view;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Segment;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.cell.view.CellView;
import jetbrains.jetpad.projectional.diagram.algorithm.util.FreeSegmentsUtil;
import jetbrains.jetpad.projectional.diagram.view.Connection;
import jetbrains.jetpad.projectional.diagram.view.DiagramView;
import jetbrains.jetpad.projectional.diagram.view.PolyLineConnection;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewPropertyKind;
import jetbrains.jetpad.projectional.view.ViewPropertySpec;

import java.util.ArrayList;
import java.util.List;

public class LabelView extends CellView {
  private static final ViewPropertySpec<Vector> LABEL_DELTA = new ViewPropertySpec<Vector>("label delta", ViewPropertyKind.RELAYOUT, Vector.ZERO);
  private static final int LABEL_TO_CONNECTION_DIST = 2;

  private Property<Vector> myLabelDelta = prop(LABEL_DELTA);
  private LabelPlacement myLabelPlacement = null;
  private PolyLineConnection myConnection;

  private Property<Boolean> myEditing = new ValueProperty<Boolean>(false);

  private DiagramView myDiagramView;

  public LabelView(View popupView, PolyLineConnection connection, DiagramView diagramView) {
    super(popupView);

    RootController.install(container);

    myDiagramView = diagramView;
    myConnection = connection;
    myConnection.view().valid().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Boolean> event) {
        if (!event.getNewValue()) {
          LabelView.this.invalidate();
        }
      }
    });
  }

  public Property<Boolean> editing() {
    return myEditing;
  }

  public void changeLabelDelta(Vector delta) {
    myLabelDelta.set(myLabelDelta.get().add(delta));
  }

  private List<Segment> getLines() {
    return myConnection.getSegments();
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);

    if (myEditing.get()) return;

    LabelPlacement curLabelPlacement;
    Vector labelDimension = bounds().get().dimension;
    Vector labelOrigin = findHorLabelPlacement(labelDimension);
    if (labelOrigin != null) {
      curLabelPlacement = LabelPlacement.HORIZONTAL;
    } else {
      labelOrigin = findVerLabelPlacement(labelDimension);
      if (labelOrigin != null) {
        curLabelPlacement = LabelPlacement.VERTICAL;
      } else {
        labelOrigin = findMiddleLabelPlacement(labelDimension);
        curLabelPlacement = LabelPlacement.MIDDLE;
      }
    }

    if (curLabelPlacement != myLabelPlacement) {
      myLabelDelta.set(Vector.ZERO);
      myLabelPlacement = curLabelPlacement;
    }
    moveTo(labelOrigin.add(myLabelDelta.get()));
  }

  private Vector findHorLabelPlacement(Vector labelDimension) {
    for (Segment s: getLines()) {
      if (s.start.y == s.end.y) {
        Vector labelOrigin = findHorLabel(s, labelDimension);
        if (labelOrigin != null) {
          return labelOrigin;
        }
      }
    }
    return null;
  }

  private Vector findHorLabel(Segment segment, Vector labelDimension) {
    List<Vector> above = new ArrayList<Vector>();
    List<Vector> below = new ArrayList<Vector>();
    Vector initial = new Vector(Math.min(segment.start.x, segment.end.x), Math.max(segment.start.x, segment.end.x));
    above.add(initial);
    below.add(initial);

    int y = segment.start.y;

    Rectangle aboveRect = new Rectangle(initial.x, y - labelDimension.y, initial.y - initial.x, labelDimension.y);
    Rectangle belowRect = aboveRect.add(new Vector(0, labelDimension.y));

    for (View item: myDiagramView.itemsView.children()) {
      Rectangle bounds = item.bounds().get();
      Vector toRemove = new Vector(bounds.origin.x, bounds.origin.x + bounds.dimension.x);
      above = addObstacle(above, aboveRect, bounds, toRemove);
      below = addObstacle(below, belowRect, bounds, toRemove);
    }

    for (Connection c: myDiagramView.connections) {
      if (c == myConnection) continue;
      PolyLineConnection pc = (PolyLineConnection) c;
      for (Segment s: pc.getSegments()) {
        Vector origin = new Vector(Math.min(s.start.x, s.end.x), Math.min(s.start.y, s.end.y));
        Vector dimension = new Vector(Math.abs(s.start.x - s.end.x), Math.abs(s.start.y - s.end.y));
        Rectangle r = new Rectangle(origin, dimension);
        Vector toRemove = new Vector(r.origin.x, r.origin.x + r.dimension.x);
        above = addObstacle(above, aboveRect, r, toRemove);
        below = addObstacle(below, belowRect, r, toRemove);
      }
    }

    for (Vector free: below) {
      Vector labelOrigin = checkFreeHorSegment(free, y, labelDimension);
      if (labelOrigin != null) return labelOrigin;
    }
    for (Vector free: above) {
      Vector labelOrigin = checkFreeHorSegment(free, y, labelDimension);
      if (labelOrigin != null) return labelOrigin;
    }
    return null;
  }

  private List<Vector> addObstacle(List<Vector> freeSegments, Rectangle bounds, Rectangle obstacle, Vector toRemove) {
    if (bounds.intersects(obstacle)) {
      return FreeSegmentsUtil.removeSegment(freeSegments, toRemove);
    }
    return freeSegments;
  }

  private Vector checkFreeHorSegment(Vector free, int y, Vector labelDimension) {
    if (labelDimension.x <= Math.abs(free.x - free.y)) {
      Segment s = new Segment(new Vector(free.x, y), new Vector(free.y, y));
      return getLabelOrigin(s, labelDimension);
    }
    return null;
  }

  private Vector findVerLabelPlacement(Vector labelDimension) {
    for (Segment s: getLines()) {
      if (s.start.x == s.end.x) {
        Vector labelOrigin = findVerLabel(s, labelDimension);
        if (labelOrigin != null) {
          return labelOrigin;
        }
      }
    }
    return null;
  }

  private Vector findVerLabel(Segment segment, Vector labelDimension) {
    List<Vector> left = new ArrayList<Vector>();
    List<Vector> right = new ArrayList<Vector>();
    Vector initial = new Vector(Math.min(segment.start.y, segment.end.y), Math.max(segment.start.y, segment.end.y));
    left.add(initial);
    right.add(initial);

    int x = segment.start.x;

    Rectangle leftRect = new Rectangle(x - labelDimension.x, initial.x, labelDimension.x, initial.y - initial.x);
    Rectangle rightRect = leftRect.add(new Vector(labelDimension.x, 0));

    for (View item: myDiagramView.itemsView.children()) {
      Rectangle bounds = item.bounds().get();
      Vector toRemove = new Vector(bounds.origin.y, bounds.origin.y + bounds.dimension.y);
      left = addObstacle(left, leftRect, bounds, toRemove);
      right = addObstacle(right, rightRect, bounds, toRemove);
    }

    for (Connection c: myDiagramView.connections) {
      if (c == myConnection) continue;
      PolyLineConnection pc = (PolyLineConnection) c;
      for (Segment s: pc.getSegments()) {
        Vector origin = new Vector(Math.min(s.start.x, s.end.x), Math.min(s.start.y, s.end.y));
        Vector dimension = new Vector(Math.abs(s.start.x - s.end.x), Math.abs(s.start.y - s.end.y));
        Rectangle r = new Rectangle(origin, dimension);
        Vector toRemove = new Vector(r.origin.y, r.origin.y + r.dimension.y);
        left = addObstacle(left, leftRect, r, toRemove);
        right = addObstacle(right, rightRect, r, toRemove);
      }
    }

    for (Vector free: right) {
      Vector labelOrigin = checkFreeVerSegment(free, x, labelDimension);
      if (labelOrigin != null) return labelOrigin;
    }
    for (Vector free: left) {
      Vector labelOrigin = checkFreeVerSegment(free, x, labelDimension);
      if (labelOrigin != null) return labelOrigin;
    }
    return null;
  }

  private Vector checkFreeVerSegment(Vector free, int x, Vector labelDimension) {
    if (labelDimension.y <= Math.abs(free.x - free.y)) {
      Segment s = new Segment(new Vector(x, free.x), new Vector(x, free.y));
      return getLabelOrigin(s, labelDimension);
    }
    return null;
  }

  private Vector findMiddleLabelPlacement(Vector labelDimension) {
    Segment middle = getLines().get(getLines().size() - 1);
    return getLabelOrigin(middle, labelDimension);
  }

  private Vector getLabelOrigin(Segment s, Vector labelBounds) {
    if (hor(s)) {
      return new Vector((s.end.x + s.start.x - labelBounds.x) / 2, s.start.y + LABEL_TO_CONNECTION_DIST);
    } else {
      return new Vector(s.start.x + LABEL_TO_CONNECTION_DIST, (s.start.y + s.end.y - labelBounds.y) / 2);
    }
  }

  private boolean hor(Segment s) {
    return s.start.y == s.end.y;
  }

  private enum LabelPlacement {
    HORIZONTAL(),
    VERTICAL(),
    MIDDLE()
  }
}