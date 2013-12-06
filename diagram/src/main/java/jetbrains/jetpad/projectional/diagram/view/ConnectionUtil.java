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
package jetbrains.jetpad.projectional.diagram.view;

import jetbrains.jetpad.projectional.view.View;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConnectionUtil {
  public static Map<View, List<Connection>> getInputToConnectionsMap(DiagramView diagramView) {
    Map<View, List<Connection>> res = new LinkedHashMap<View, List<Connection>>();
    for (Connection c: diagramView.connections) {
      PolyLineConnection bc = (PolyLineConnection) c;
      View input = bc.fromView().get();
      if (input != null) {
        if (!res.containsKey(input)) {
          res.put(input, new ArrayList<Connection>());
        }
        res.get(input).add(c);
      }
    }
    return res;
  }
}