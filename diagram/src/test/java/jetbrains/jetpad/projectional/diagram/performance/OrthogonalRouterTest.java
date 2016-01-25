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
package jetbrains.jetpad.projectional.diagram.performance;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.diagram.algorithm.orthogonal.OrthogonalRouteWithObstacles;
import jetbrains.jetpad.projectional.diagram.layout.OrthogonalRouter;
import jetbrains.jetpad.projectional.diagram.view.DiagramView;
import jetbrains.jetpad.projectional.diagram.view.PolyLineConnection;
import jetbrains.jetpad.projectional.view.ViewContainer;
import jetbrains.jetpad.projectional.view.toAwt.AwtViewDemo;

import java.util.List;

public class OrthogonalRouterTest {
  public static void main(String[] args) {
    final Value<Integer> connectionsNum = new Value<>(0);
    OrthogonalRouter router = new OrthogonalRouter() {
      @Override
      public void findRoutes(DiagramView view) {
        connectionsNum.set(0);
        super.findRoutes(view);
      }

      @Override
      protected List<Vector> routeConnection(PolyLineConnection connection, OrthogonalRouteWithObstacles router) {
        connectionsNum.set(connectionsNum.get() + 1);
        return super.routeConnection(connection, router);
      }
    };
    final ViewContainer vc = new ViewContainer();
    DiagramView v = new DiagramView();
    vc.root().children().add(v);

    new GridViewGenerator(v, 6).generateGridView();
    long time = System.currentTimeMillis();
    router.findRoutes(v);
    System.out.println("reroute " + connectionsNum + " connections");
    long total = System.currentTimeMillis() - time;
    System.out.println("time: " + total);
    AwtViewDemo.show(vc);
  }
}