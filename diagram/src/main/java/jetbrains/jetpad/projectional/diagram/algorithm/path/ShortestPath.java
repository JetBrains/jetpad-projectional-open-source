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

import jetbrains.jetpad.projectional.diagram.algorithm.graph.Graph;
import jetbrains.jetpad.projectional.diagram.algorithm.graph.WeightedGraph;

import java.util.*;

public class ShortestPath {
  public<NodeT> List<NodeT> getPath(Graph<NodeT> graph, NodeT from, NodeT to) {
    Queue<NodeT> q = new LinkedList<NodeT>();
    Map<NodeT, NodeT> prev = new HashMap<NodeT, NodeT>();
    q.add(from);
    prev.put(from, from);
    while (!q.isEmpty()) {
      NodeT cur = q.poll();
      if (cur == to) break;
      for (NodeT next: graph.getEdges(cur)) {
        if (!prev.containsKey(next)) {
          prev.put(next, cur);
          q.add(next);
        }
      }
    }
    return restorePath(from, to, prev);
  }

  public<NodeT> List<NodeT> getPathMinWeight(WeightedGraph<NodeT> graph, NodeT from, NodeT to) {
    Queue<NodeT> q = new LinkedList<NodeT>();
    Map<NodeT, NodeT> prev = new HashMap<NodeT, NodeT>();
    Map<NodeT, Integer> num = new HashMap<NodeT, Integer>();
    Map<NodeT, Integer> dist = new HashMap<NodeT, Integer>();
    q.add(from);
    prev.put(from, from);
    num.put(from, 0);
    dist.put(from, 0);
    while (!q.isEmpty()) {
      NodeT cur = q.poll();
      if (cur == to) break;
      int curNum = num.get(cur);
      int curDist = dist.get(cur);
      Iterator<NodeT> graphItr = graph.getEdges(cur).iterator();
      Iterator<Integer> lenItr = graph.getEdgeWeights(cur).iterator();
      while (graphItr.hasNext()) {
        NodeT next = graphItr.next();
        int l = lenItr.next();
        if (!prev.containsKey(next)) {
          prev.put(next, cur);
          num.put(next, curNum + 1);
          q.add(next);
          q.add(next);
        }

        if (num.get(next) == curNum + 1 && (!dist.containsKey(next) || curDist + l < dist.get(next))) {
          dist.put(next, curDist + l);
          prev.put(next, cur);
        }
      }
    }
    return restorePath(from, to, prev);
  }


  private<NodeT> List<NodeT> restorePath(NodeT from, NodeT to, Map<NodeT, NodeT> prev) {
    if (!prev.containsKey(to)) {
      return null;
    }
    List<NodeT> path = new ArrayList<NodeT>();
    NodeT cur = to;
    while (cur != from) {
      path.add(cur);
      cur = prev.get(cur);
    }
    path.add(from);
    Collections.reverse(path);
    return path;
  }
}