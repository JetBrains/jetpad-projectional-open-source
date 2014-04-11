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
package jetbrains.jetpad.projectional.diagram.view.block;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.diagram.base.GridDirection;
import jetbrains.jetpad.projectional.diagram.view.DiagramNodeView;
import jetbrains.jetpad.projectional.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockView extends DiagramNodeView {
  public BlockView() {
    minimalSize().set(new Vector(75, 75));
  }

  @Override
  public void layoutPorts(List<View> views, GridDirection dir) {
    List<Vector> viewDimensions = new ArrayList<>(views.size());
    for (View v : views) {
      viewDimensions.add(v.bounds().get().dimension);
    }
    Rectangle bounds = new Rectangle(rect.bounds().get().origin, rect.dimension().get());
    List<Vector> viewOrigins = new BlockViewPortLayouter(bounds).layoutPorts(viewDimensions, dir);
    Iterator<Vector> originIter = viewOrigins.iterator();
    for (View v: views) {
      v.moveTo(originIter.next());
    }
  }
}