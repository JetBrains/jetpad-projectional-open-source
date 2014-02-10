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
package jetbrains.jetpad.projectional.diagram.algorithm.orthogonal;

import jetbrains.jetpad.geometry.*;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.diagram.algorithm.graph.WeightedGraph;
import jetbrains.jetpad.projectional.diagram.algorithm.path.ShortestPath;
import jetbrains.jetpad.projectional.diagram.algorithm.util.FreeSegmentsUtil;
import jetbrains.jetpad.projectional.diagram.algorithm.util.IntGeomUtil;

import java.util.*;

public class OrthogonalRouteWithObstacles {
  private final static boolean DEBUG = false;
  private final static int INF = 10000;
  private static final boolean LAZY_GRAPH_BUILD = true;

  private int myMinDistToForbiddenSegment = 0;

  private List<Rectangle> myObstacles;
  private Map<MyLine, List<Segment>> mySegmentsMap = new LinkedHashMap<>();
  private List<Segment> myForbiddenSegments = new ArrayList<>();

  private Vector myStart;
  private Vector myEnd;
  private List<Segment> myEndpointSegments;
  private Set<MyLine> myEndpointLines;

  private long myBuildInitTime = 0;
  private long myBuildLazyTime = 0;
  private long myInitRoutingTime = 0;
  private long myForbiddenSegmentsTime = 0;
  private long myAdditionalLinesTime = 0;
  private long myInitTime = 0;

  public long getBuildInitTime() {
    return myBuildInitTime;
  }

  public long getBuildLazyTime() {
    return myBuildLazyTime;
  }

  public long getInitRoutingTime() {
    return myInitRoutingTime;
  }

  public long getForbiddenSegmentsTime() {
    return myForbiddenSegmentsTime;
  }

  public long getAdditionalLinesTime() {
    return myAdditionalLinesTime;
  }

  public long getInitTime() {
    return myInitTime;
  }

  public OrthogonalRouteWithObstacles() {
    this(new ArrayList<Rectangle>());
  }

  public OrthogonalRouteWithObstacles(List<Rectangle> obstacles) {
    myObstacles = Collections.unmodifiableList(obstacles);
    initObstaclesSegments();
  }

  public List<Vector> findRoute(Vector start, Vector end) {
    //it should be tested here, because later we do not recognize equal points
    if (start.equals(end)) {
      List<Vector> path = new ArrayList<>();
      path.add(start);
      return path;
    }
    initRouting(start, end);
    if (DEBUG) {
      validateLines();
    }
    return findPath();
  }

  public void addAdditionalLines(List<Segment> additionalLines) {
    long time = System.currentTimeMillis();

    for (Segment s: additionalLines) {
      if (s.start.x == s.end.x) {
        addLine(s.start.x, true);
      } else {
        addLine(s.start.y, false);
      }
    }

    myAdditionalLinesTime += System.currentTimeMillis() - time;
  }

  public void addForbiddenSegments(List<Segment> forbiddenSegments) {
    long time = System.currentTimeMillis();

    for (Segment fs: forbiddenSegments) {
      removeForbiddenSegment(fs);
    }
    myForbiddenSegments.addAll(forbiddenSegments);

    myForbiddenSegmentsTime += System.currentTimeMillis() - time;
  }

  public void setMinDistToForbiddenSegment(int minDistToForbiddenSegment) {
    myMinDistToForbiddenSegment = minDistToForbiddenSegment;
  }

  private void validateLines() {
    for (MyLine l: mySegmentsMap.keySet()) {
      for (Segment s: mySegmentsMap.get(l)) {
        if (l.ver) {
          if (s.start.x != s.end.x || s.start.x != l.coord || s.start.y > s.end.y) throw new RuntimeException();
        } else {
          if (s.start.y != s.end.y || s.start.y != l.coord || s.start.x > s.end.x) throw new RuntimeException();
        }
      }
    }
  }

  private void initRouting(Vector start, Vector end) {
    long time = System.currentTimeMillis();

    myStart = start;
    myEnd = end;
    myEndpointSegments = new ArrayList<>();
    myEndpointLines = new HashSet<>();

    addEndpointLine(myStart.x, true);
    addEndpointLine(myStart.y, false);
    addEndpointLine(myEnd.x, true);
    addEndpointLine(myEnd.y, false);

    myInitRoutingTime += System.currentTimeMillis() - time;
  }

  private void addEndpointLine(int coord, boolean ver) {
    MyLine line = new MyLine(ver, coord);
    if (myEndpointLines.contains(line)) return;
    myEndpointLines.add(line);

    List<Vector> freeSegments = getFreeSegments(line);

    ArrayList<Segment> segments = new ArrayList<>(freeSegments.size());
    if (ver) {
      for (Vector seg: freeSegments) {
        segments.add(new Segment(new Vector(coord, seg.x), new Vector(coord, seg.y)));
      }
    } else {
      for (Vector seg: freeSegments) {
        segments.add(new Segment(new Vector(seg.x, coord), new Vector(seg.y, coord)));
      }
    }
    myEndpointSegments.addAll(segments);
  }

  private void removeForbiddenSegment(Segment fs) {
    for (MyLine line: mySegmentsMap.keySet()) {
      if (isVertical(fs) && line.ver && fitsMinDist(fs.start.x, line.coord)) {
        List<Segment> newSegments = new ArrayList<>();
        for (Segment s: mySegmentsMap.get(line)) {
          Vector vs = getYVector(s);
          ArrayList<Vector> list = new ArrayList<>();
          list.add(vs);
          Vector vfs = getYVector(fs);
          for (Vector v: FreeSegmentsUtil.removeSegment(list, vfs)) {
            newSegments.add(new Segment(new Vector(s.start.x, v.x), new Vector(s.start.x, v.y)));
          }
        }
        mySegmentsMap.put(line, newSegments);
      } else if (!isVertical(fs) && !line.ver && fitsMinDist(fs.start.y, line.coord)) {
        List<Segment> newSegments = new ArrayList<>();
        for (Segment s: mySegmentsMap.get(line)) {
          Vector vs = getXVector(s);
          ArrayList<Vector> list = new ArrayList<>();
          list.add(vs);
          Vector vfs = getXVector(fs);
          for (Vector v: FreeSegmentsUtil.removeSegment(list, vfs)) {
            newSegments.add(new Segment(new Vector(v.x, s.start.y), new Vector(v.y, s.start.y)));
          }
        }
        mySegmentsMap.put(line, newSegments);
      }
    }
  }

  private boolean intersects(MyLine line, Segment forbiddenSegment) {
    if (line.ver) {
      return isVertical(forbiddenSegment) && fitsMinDist(forbiddenSegment.start.x, line.coord);
    } else {
      return !isVertical(forbiddenSegment) && fitsMinDist(forbiddenSegment.start.y, line.coord);
    }
  }

  private boolean fitsMinDist(int x1, int x2) {
    return Math.abs(x1 - x2) <= myMinDistToForbiddenSegment;
  }

  private Vector getXVector(Segment s) {
    return new Vector(Math.min(s.start.x, s.end.x), Math.max(s.start.x, s.end.x));
  }

  private Vector getYVector(Segment s) {
    return new Vector(Math.min(s.start.y, s.end.y), Math.max(s.start.y, s.end.y));
  }

  private boolean isVertical(Segment s) {
    return s.start.x == s.end.x;
  }

  private List<Vector> findPath() {
    final GraphBuilder builder = new GraphBuilder();
    final Set<MyLine> addedLines = new HashSet<>();
    builder.buildGraph();
    List<Integer> numPath = new ShortestPath().getPathMinWeight(new WeightedGraph<Integer>() {
      @Override
      public Iterable<Integer> getEdgeWeights(Integer node) {
        addLine(node);
        return builder.segmentsGraphLen.get(node);
      }

      @Override
      public Iterable<Integer> getEdges(Integer node) {
        addLine(node);
        return builder.segmentsGraph.get(node);
      }

      private void addLine(Integer node) {
        if (!LAZY_GRAPH_BUILD) return;

        long time = System.currentTimeMillis();

        MyLine line = builder.pointsToLine.get(builder.points.get(node));
        if (line != null && !addedLines.contains(line)) {
          builder.addLineToGraph(line);
          addedLines.add(line);
        }

        myBuildLazyTime += System.currentTimeMillis() - time;
      }
    }, 0, 1);

    if (numPath == null) return null;
    List<Vector> path = new ArrayList<>(numPath.size());
    for (Integer num: numPath) {
      path.add(builder.points.get(num));
    }
    return path;
  }

  private void initObstaclesSegments() {
    long time = System.currentTimeMillis();

    if (!mySegmentsMap.isEmpty()) {
      throw new IllegalStateException();
    }
    for (Rectangle r: myObstacles) {
      addLine(r.origin.x, true);
      addLine(r.origin.y, false);
      addLine(r.origin.x + r.dimension.x, true);
      addLine(r.origin.y + r.dimension.y, false);
    }

    myInitTime += System.currentTimeMillis() - time;
  }

  private void addLine(int c, boolean vertical) {
    MyLine line = new MyLine(vertical, c);
    if (mySegmentsMap.containsKey(line)) return;

    List<Vector> freeSegments = getFreeSegments(line);

    ArrayList<Segment> segments = new ArrayList<>(freeSegments.size());
    if (vertical) {
      for (Vector seg: freeSegments) {
        segments.add(new Segment(new Vector(c, seg.x), new Vector(c, seg.y)));
      }
    } else {
      for (Vector seg: freeSegments) {
        segments.add(new Segment(new Vector(seg.x, c), new Vector(seg.y, c)));
      }
    }
    mySegmentsMap.put(line, segments);
  }

  private List<Vector> getFreeSegments(MyLine line) {
    List<Vector> freeSegments = new ArrayList<>();
    freeSegments.add(new Vector(-INF, INF));
    for (Rectangle r: myObstacles) {
      if (line.ver) {
        if (r.origin.x < line.coord && line.coord < r.origin.x + r.dimension.x) {
          freeSegments = FreeSegmentsUtil.removeSegment(freeSegments, new Vector(r.origin.y, r.origin.y + r.dimension.y));
        }
      } else {
        if (r.origin.y < line.coord && line.coord < r.origin.y + r.dimension.y) {
          freeSegments = FreeSegmentsUtil.removeSegment(freeSegments, new Vector(r.origin.x, r.origin.x + r.dimension.x));
        }
      }
    }

    for (Segment fs: myForbiddenSegments) {
      if (intersects(line, fs)) {
        if (line.ver) {
          freeSegments = FreeSegmentsUtil.removeSegment(freeSegments, getYVector(fs));
        } else {
          freeSegments = FreeSegmentsUtil.removeSegment(freeSegments, getXVector(fs));
        }
      }
    }
    return freeSegments;
  }

  //test method
  void setObstacles(List<Rectangle> obstacles) {
    myObstacles = Collections.unmodifiableList(obstacles);
    initObstaclesSegments();
  }

  private class MyLine {
    boolean ver;
    int coord;

    private MyLine(boolean ver, int coord) {
      this.ver = ver;
      this.coord = coord;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MyLine myLine = (MyLine) o;

      if (coord != myLine.coord) return false;
      if (ver != myLine.ver) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = (ver ? 1 : 0);
      result = 23917 * result + coord;
      return result;
    }
  }

  private class GraphBuilder {
    private List<List<Integer>> segmentsGraph = new ArrayList<>();
    private List<List<Integer>> segmentsGraphLen = new ArrayList<>();
    Map<Vector, Integer> pointToNum = new HashMap<>();
    List<Vector> points = new ArrayList<>();

    Map<Vector, MyLine> pointsToLine = new HashMap<>();

    private void buildGraph() {
      long time = System.currentTimeMillis();

      put(myStart);
      put(myEnd);
      for (Segment seg: myEndpointSegments) {
        Set<Vector> p = new LinkedHashSet<>();
        if (seg.contains(myStart)) {
          p.add(myStart);
        }
        if (seg.contains(myEnd)) {
          p.add(myEnd);
        }
        addSegmentToGraph(seg, p);
      }
      if (!LAZY_GRAPH_BUILD) {
        for (MyLine line: mySegmentsMap.keySet()) {
          addLineToGraph(line);
        }
      }

      myBuildInitTime += System.currentTimeMillis() - time;
    }

    private void addLineToGraph(MyLine line) {
      Map<Segment, List<Vector>> mySegmentPoints = new HashMap<>();
      for (Segment lineSeg: mySegmentsMap.get(line)) {
        ArrayList<Vector> points = new ArrayList<>();
        mySegmentPoints.put(lineSeg, points);
        for (Segment s: myEndpointSegments) {
          addVertex(lineSeg, points, s, null);
        }
      }
      for (MyLine otherLine: mySegmentsMap.keySet()) {
        if (otherLine.ver == line.ver) continue;
        Segment lineSeg = getIntersectingSegment(line, otherLine.coord);
        if (lineSeg != null && getIntersectingSegment(otherLine, line.coord) != null) {
          Vector intersectingPoint;
          if (line.ver) {
            intersectingPoint = new Vector(line.coord, otherLine.coord);
          } else {
            intersectingPoint = new Vector(otherLine.coord, line.coord);
          }
          mySegmentPoints.get(lineSeg).add(intersectingPoint);
          put(intersectingPoint);
          if (!pointsToLine.containsKey(intersectingPoint)) {
            pointsToLine.put(intersectingPoint, otherLine);
          }
        }
      }
      for (Segment lineSeg: mySegmentPoints.keySet()) {
        connectList(mySegmentPoints.get(lineSeg));
      }
    }

    private Segment getIntersectingSegment(MyLine line, int otherCoord) {
      List<Segment> segments = mySegmentsMap.get(line);
      int left = 0;
      int right = segments.size();

      while (left < right) {
        int mid = (left + right) / 2;
        int compare = compare(line.ver, segments.get(mid), otherCoord);
        if (compare == 0) return segments.get(mid);
        if (compare > 0) {
          left = mid + 1;
        } else {
          right = mid;
        }
      }
      return null;
    }

    private int compare(boolean ver, Segment s, int coord) {
      if (ver) {
        if (s.start.y > coord) return 1;
        if (s.end.y < coord) return -1;
        return 0;
      } else {
        if (s.start.x > coord) return 1;
        if (s.end.x < coord) return -1;
        return 0;
      }
    }

    private void addSegmentToGraph(Segment seg, Set<Vector> points) {
      for (Segment s: myEndpointSegments) {
        addVertex(seg, points, s, null);
      }
      boolean ver = seg.start.x == seg.end.x;
      int coord = ver ? seg.start.x : seg.start.y;
      for (MyLine l: mySegmentsMap.keySet()) {
        if (l.ver == ver) continue;
        boolean segIntersectsLine;
        if (ver) {
          segIntersectsLine = seg.start.y <= l.coord && l.coord <= seg.end.y;
        } else {
          segIntersectsLine = seg.start.x <= l.coord && l.coord <= seg.end.x;
        }
        if (!segIntersectsLine) continue;
        Segment s = getIntersectingSegment(l, coord);
        if (s != null) {
         Vector intersectingPoint;
          if (ver) {
            intersectingPoint = new Vector(coord, l.coord);
          } else {
            intersectingPoint = new Vector(l.coord, coord);
          }
          points.add(intersectingPoint);
          put(intersectingPoint);
          if (!pointsToLine.containsKey(intersectingPoint)) {
            pointsToLine.put(intersectingPoint, l);
          }
        }
      }
      connectList(new ArrayList<>(points));
    }

    private void connectList(List<Vector> points) {
      for (int i = 0; i < points.size(); i++) {
        for (int j = i + 1; j < points.size(); j++) {
          int numI = pointToNum.get(points.get(i));
          int numJ = pointToNum.get(points.get(j));

          if (points.get(i).x != points.get(j).x && points.get(i).y != points.get(j).y) {
            throw new IllegalStateException();
          }

          segmentsGraph.get(numI).add(numJ);
          segmentsGraph.get(numJ).add(numI);
          int len = (int) points.get(i).sub(points.get(j)).length();
          segmentsGraphLen.get(numI).add(len);
          segmentsGraphLen.get(numJ).add(len);
        }
      }
    }

    private void addVertex(Segment seg, Collection<Vector> points, Segment s, MyLine line) {
      if (seg == s || !IntGeomUtil.intersects(seg, s)) return;
      Vector v = IntGeomUtil.findAxisIntersection(seg, s);
      put(v);
      points.add(v);
      if (line != null && !pointsToLine.containsKey(v)) {
        pointsToLine.put(v, line);
      }
    }

    private void put(Vector v) {
      if (pointToNum.containsKey(v)) return;
      pointToNum.put(v, pointToNum.size());
      points.add(v);
      segmentsGraph.add(new ArrayList<Integer>());
      segmentsGraphLen.add(new ArrayList<Integer>());
    }
  }
}