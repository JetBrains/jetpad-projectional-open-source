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
package jetbrains.jetpad.projectional.diagram.view.decoration;

import jetbrains.jetpad.geometry.Segment;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.diagram.algorithm.orthogonal.DivergePointsFinder;
import jetbrains.jetpad.projectional.diagram.view.Connection;
import jetbrains.jetpad.projectional.diagram.view.ConnectionUtil;
import jetbrains.jetpad.projectional.diagram.view.DiagramView;
import jetbrains.jetpad.projectional.diagram.view.PolyLineConnection;
import jetbrains.jetpad.projectional.view.GroupView;
import jetbrains.jetpad.projectional.view.RectView;
import jetbrains.jetpad.values.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConnectionDivergeDecoration extends GroupView {
  private static final int DIVERGE_SIZE = 3;

  private DecorationContainer<DiagramView> myContainer;

  public ConnectionDivergeDecoration(DecorationContainer<DiagramView> container) {
    myContainer = container;
    container.addDecoration(this);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    update();
    super.doValidate(ctx);
  }

  private void update() {
    children().clear();
    for (List<Connection> connections: ConnectionUtil.getInputToConnectionsMap(myContainer.getDecoratedView()).values()) {
      Set<List<Vector>> lines = new HashSet<>();
      for (Connection connection: connections) {
        List<Vector> line = new ArrayList<>();
        for (Segment s: ((PolyLineConnection) connection).getSegments()) {
          if (line.size() == 0) {
            line.add(s.start);
          }
          line.add(s.end);
        }
        lines.add(line);
      }
      for(Vector point: new DivergePointsFinder().find(lines)) {
        addDivergeMark(point);
      }
    }
  }

  private void addDivergeMark(Vector v) {
    RectView r = new RectView();
    r.moveTo(v.sub(new Vector(DIVERGE_SIZE, DIVERGE_SIZE)));
    r.dimension().set(new Vector(DIVERGE_SIZE * 2 + 1, DIVERGE_SIZE * 2 + 1));
    r.background().set(Color.BLACK);
    children().add(r);
  }
}