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
package jetbrains.jetpad.projectional.diagram.layout;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Segment;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.projectional.diagram.algorithm.orthogonal.OrthogonalRouteWithObstacles;
import jetbrains.jetpad.projectional.diagram.algorithm.util.IntGeomUtil;
import jetbrains.jetpad.projectional.diagram.base.GridDirection;
import jetbrains.jetpad.projectional.diagram.view.*;
import jetbrains.jetpad.projectional.view.View;

import java.util.*;

public class OrthogonalRouter implements ConnectionRouter {
  private static final boolean SHOW_INFO = false;

  private static final int DIST_FROM_OBSTACLE = 20;
  private static final int DIST_BETWEEN_EDGES = 10;

  @Override
  public void findRoutes(DiagramView diagramView) {
    Set<Rectangle> invalidChildrenRects = new HashSet<Rectangle>();
    for (View child : getItems(diagramView)) {
      boolean valid = child.valid().get();
      child.validate();
      if (!valid) {
        invalidChildrenRects.add(getLayoutBounds(child.bounds().get()));
      }
    }

    Set<View> invalidInputs = new HashSet<View>();
    for (Connection c: diagramView.connections) {
      if (!isValid(c)) {
        View input = ((PolyLineConnection) c).fromView().get();
        if (input != null) {
          invalidInputs.add(input);
        }
      }
    }

    for (Connection c: diagramView.connections) {
      if (!isValid(c)) continue;
      if (intersects((PolyLineConnection) c, invalidChildrenRects)) {
        c.view().invalidate();
      }
      View input = ((PolyLineConnection) c).fromView().get();
      if (input != null && invalidInputs.contains(input)) {
        c.view().invalidate();
      }
    }

    reroute(diagramView);
  }

  private ObservableList<View> getItems(DiagramView view) {
    return view.itemsView.children();
  }

  private Boolean isValid(Connection c) {
    return c.view().valid().get();
  }

  private void reroute(DiagramView diagramView) {
    List<Segment> forbiddenSegments = new ArrayList<Segment>();
    List<Segment> additionalLines = new ArrayList<Segment>();
    for (Connection c: diagramView.connections) {
      if (isValid(c)) {
        getAdditionalInfo(forbiddenSegments, additionalLines, ((PolyLineConnection) c).getSegments());
      }
    }

    List<Rectangle> obstacleRects = new ArrayList<Rectangle>();
    for(View f: getItems(diagramView)) {
      obstacleRects.add(getLayoutBounds(f.bounds().get()));
    }
    long totalTime = System.currentTimeMillis();

    OrthogonalRouteWithObstacles router = new OrthogonalRouteWithObstacles(obstacleRects);
    router.setMinDistToForbiddenSegment(DIST_BETWEEN_EDGES - 1);
    router.addForbiddenSegments(forbiddenSegments);
    router.addAdditionalLines(additionalLines);

    Map<View,List<Connection>> connMap = ConnectionUtil.getInputToConnectionsMap(diagramView);
    for (View input: connMap.keySet()) {
      List<Segment> inputForbiddenSegments = new ArrayList<Segment>();
      List<Segment> inputAdditionalLines = new ArrayList<Segment>();
      for (Connection c: connMap.get(input)) {
        if (isValid(c)) continue;
        List<Vector> path = routeConnection((PolyLineConnection) c, router);
        getAdditionalInfo(inputForbiddenSegments, inputAdditionalLines, IntGeomUtil.getSegments(path));
      }
      router.addForbiddenSegments(inputForbiddenSegments);
      router.addAdditionalLines(inputAdditionalLines);
    }

    if (SHOW_INFO) {
      System.out.println("total time =           " + (System.currentTimeMillis() - totalTime));
      System.out.println("init time =            " + router.getInitTime());
      System.out.println("init routing time =    " + router.getInitRoutingTime());
      System.out.println("init build graph time =     " + router.getBuildInitTime());
      System.out.println("lazy build graph time =     " + router.getBuildLazyTime());
      System.out.println("add lines time =       " + router.getAdditionalLinesTime());
      System.out.println("forbid segments time = " + router.getForbiddenSegmentsTime());
    }
  }

  private void getAdditionalInfo(List<Segment> forbiddenSegments, List<Segment> additionalLines, List<Segment> path) {
    for (Segment s: path) {
      forbiddenSegments.add(createForbiddenSegment(s));
      additionalLines.addAll(createAdditionalLines(s));
    }
  }

  private Segment createForbiddenSegment(Segment s) {
    if (s.start.x == s.end.x) {
      Vector start = new Vector(s.start.x, Math.min(s.start.y, s.end.y) - DIST_BETWEEN_EDGES);
      Vector end = new Vector(s.start.x, Math.max(s.start.y, s.end.y) + DIST_BETWEEN_EDGES);
      return new Segment(start, end);
    } else {
      Vector start = new Vector(Math.min(s.start.x, s.end.x) - DIST_BETWEEN_EDGES, s.start.y);
      Vector end = new Vector(Math.max(s.start.x, s.end.x) + DIST_BETWEEN_EDGES, s.start.y);
      return new Segment(start, end);
    }
  }

  private List<Segment> createAdditionalLines(Segment s) {
    List<Segment> res = new ArrayList<Segment>(2);
    Vector dist;
    if (s.start.x == s.end.x) {
      dist = new Vector(DIST_BETWEEN_EDGES, 0);
    } else {
      dist = new Vector(0, DIST_BETWEEN_EDGES);
    }
    res.add(new Segment(s.start.sub(dist), s.end.sub(dist)));
    res.add(new Segment(s.start.add(dist), s.end.add(dist)));
    return res;
  }

  protected List<Vector> routeConnection(PolyLineConnection connection, OrthogonalRouteWithObstacles router) {
    Vector startShift = getEndpointShift(connection.fromView().get(), new Vector(DIST_FROM_OBSTACLE, 0));
    Vector endShift = getEndpointShift(connection.toView().get(), new Vector(-DIST_FROM_OBSTACLE, 0));
    Vector start = connection.getFrom().add(startShift);
    Vector e = connection.getTo().add(endShift);

    List<Vector> path = router.findRoute(start, e);
    if (path == null) {
      return simpleLayout(connection);
    }

    path.add(0, connection.getFrom());
    path.add(connection.getTo());
    connection.update(path.toArray(new Vector[path.size()]));
    return path;
  }

  private Vector getEndpointShift(View endpointView, Vector defaultShift) {
    if (endpointView == null) return defaultShift;
    GridDirection dir = endpointView.prop(DiagramViewSpecs.CONNECTOR_DIR).get();
    if (dir != null) {
      return dir.dir().mul(DIST_FROM_OBSTACLE);
    } else {
      return defaultShift;
    }
  }

  private Rectangle getLayoutBounds(Rectangle rect) {
    return IntGeomUtil.increase(rect, DIST_FROM_OBSTACLE);
  }

  private List<Vector> simpleLayout(PolyLineConnection connection) {
    Vector start = connection.getFrom();
    Vector end = connection.getTo();
    int dx = end.x - start.x;
    int dy = end.y - start.y;
    if (Math.abs(dx) < Math.abs(dy)) {
      if (dx == 0) {
        return updateLines(connection, start, end);
      } else {
        Vector v1 = start.add(new Vector(0, dy / 2));
        Vector v2 = v1.add(new Vector(dx, 0));
        return updateLines(connection, start, v1, v2, end);
      }
    } else {
      if (dy == 0) {
        return updateLines(connection, start, end);
      } else {
        Vector v1 = start.add(new Vector(dx / 2, 0));
        Vector v2 = v1.add(new Vector(0, dy));
        return updateLines(connection, start, v1, v2, end);
      }
    }
  }

  private List<Vector> updateLines(PolyLineConnection connection, Vector... points) {
    connection.update(points);
    return Arrays.asList(points);
  }

  private boolean intersects(PolyLineConnection connection, Set<Rectangle> obstacles) {
    for (Segment s: connection.getSegments()) {
      for (Rectangle rect: obstacles) {
        for (Segment bound: rect.getBoundSegments()) {
          if (IntGeomUtil.intersects(s, bound)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}