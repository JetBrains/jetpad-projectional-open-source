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
package jetbrains.jetpad.projectional.diagram.algorithm.path;

import jetbrains.jetpad.projectional.diagram.algorithm.graph.WeightedGraph;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShortestPathTest {
  List<List<Integer>> graph = new ArrayList<List<Integer>>();
  List<List<Integer>> len = new ArrayList<List<Integer>>();
  WeightedGraph<Integer> wg = new WeightedGraph<Integer>() {
    @Override
    public Iterable<Integer> getEdgeWeights(Integer node) {
      return len.get(node);
    }

    @Override
    public Iterable<Integer> getEdges(Integer node) {
      return graph.get(node);
    }
  };

  @Test
  public void fromEqualsTo() {
    createGraph(2);
    connect(0, 1);
    assertSame(new ShortestPath().getPath(wg, 0, 0), 0);
  }

  @Test
  public void simple() {
    createGraph(3);
    connect(0, 1);
    connect(1, 2);
    connect(0, 2);
    assertSame(new ShortestPath().getPath(wg, 0, 2), 0, 2);
  }

  @Test
  public void noPath() {
    createGraph(4);
    connect(0, 1);
    connect(1, 2);
    connect(0, 2);
    assertNull(new ShortestPath().getPath(wg, 0, 3));
  }

  @Test
  public void simpleLen() {
    createGraph(3);
    connect(0, 1, 1);
    connect(1, 2, 1);
    connect(0, 2, 100);
    List<Integer> path = new ShortestPath().getPathMinWeight(wg, 0, 2);
    assertSame(path, 0, 2);
    Assert.assertTrue(getPathLen(path) == 100);
  }

  @Test
  public void twoWays() {
    createGraph(5);
    connect(0, 1, 1);
    connect(1, 2, 4);
    connect(0, 3, 2);
    connect(3, 2, 2);
    connect(2, 4, 10);
    List<Integer> path = new ShortestPath().getPathMinWeight(wg, 0, 4);
    assertSame(path, 0, 3, 2, 4);
    Assert.assertTrue(getPathLen(path) == 14);
  }

  private void assertSame(List<Integer> path, Integer... points) {
    assertTrue(path.size() == points.length);
    for (int i = 0; i < path.size(); i++) {
      assertEquals(points[i], path.get(i));
    }
  }

  private int getPathLen(List<Integer> path) {
    int prev = -1;
    int pathLen = 0;
    for (Integer p: path) {
      if (prev != -1) {
        pathLen += len.get(prev).get(graph.get(prev).indexOf(p));
      }
      prev = p;
    }
    return pathLen;
  }

  private void connect(int i, int j) {
    graph.get(i).add(j);
    graph.get(j).add(i);
  }

  private void connect(int i, int j, int l) {
    graph.get(i).add(j);
    graph.get(j).add(i);
    len.get(i).add(l);
    len.get(j).add(l);
  }

  private void createGraph(int num) {
    graph = new ArrayList<List<Integer>>();
    len = new ArrayList<List<Integer>>();
    for (int i = 0; i < num; i++) {
      graph.add(new ArrayList<Integer>());
      len.add(new ArrayList<Integer>());
    }
  }
}