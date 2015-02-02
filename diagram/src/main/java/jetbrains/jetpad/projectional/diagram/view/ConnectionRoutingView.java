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
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.projectional.diagram.algorithm.geom.AvoidObstacles;
import jetbrains.jetpad.projectional.diagram.layout.ConnectionRouter;
import jetbrains.jetpad.projectional.diagram.view.decoration.DecorationContainer;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.GroupView;

import java.util.*;

public class ConnectionRoutingView extends DiagramView implements DecorationContainer<DiagramView> {
  private static final int MIN_DIST = 30;
  private static final Vector ORIGIN_SHIFT = new Vector(MIN_DIST, MIN_DIST);
  private static final Vector DIMENSION_GROW = new Vector(2 * MIN_DIST + 1, 2 * MIN_DIST + 1);

  private GroupView myDecorationGroup = new GroupView();

  private ConnectionRouter myRouter;

  private View myMovingView = null;
  private Map<View, List<Vector>> myOldPositions = new HashMap<>();

  private RoutingViewConfiguration myConfiguration;

  public ConnectionRoutingView(ConnectionRouter router) {
    this(router, getDefaultConfiguration());
  }

  public ConnectionRoutingView(ConnectionRouter router, RoutingViewConfiguration configuration) {
    setRouter(router);
    children().add(myDecorationGroup);
    myConfiguration = configuration;
  }

  public void setRouter(ConnectionRouter router) {
    myRouter = router;
  }

  private View getInvalid() {
    View invalid = null;
    for (View f: getNodes()) {
      if (!f.valid().get()) {
        if (invalid != null) {
          return null;
        }
        invalid = f;
      }
    }
    return invalid;
  }

  private ObservableList<View> getNodes() {
    return itemsView.children();
  }

  private void preventOverlapping() {
    View moved = getInvalid();
    if (myMovingView != moved) {
      myMovingView = moved;
      myOldPositions.clear();
    }
    if (moved == null) {
      return;
    }
    List<Rectangle> viewRects = new ArrayList<>();
    viewRects.add(getRect(moved));
    List<View> childrenByDist = new ArrayList<>(getNodes());
    childrenByDist.remove(moved);

    final Vector movedCenter = moved.bounds().get().center();
    Collections.sort(childrenByDist, new Comparator<View>() {
      @Override
      public int compare(View f1, View f2) {
        Double d1 = f1.bounds().get().center().sub(movedCenter).length();
        Double d2 = f2.bounds().get().center().sub(movedCenter).length();
        return d1.compareTo(d2);
      }
    });
    for (View f: childrenByDist) {
      List<Vector> oldPos = myOldPositions.get(f);
      Rectangle bounds = f.bounds().get();
      if (oldPos != null) {
        List<Vector> newPos = new ArrayList<>();
        Vector foundPos = null;
        for(Vector pos: oldPos) {
          if (posFree(new Rectangle(pos, bounds.dimension), viewRects)) {
            foundPos = pos;
            break;
          } else {
            newPos.add(pos);
          }
        }
        if (foundPos != null) {
          myOldPositions.put(f, newPos);
          updateView(viewRects, f, foundPos);
          continue;
        }
      }
      Vector newOrigin = new AvoidObstacles().findNewPosition(bounds, viewRects).origin;
      if (!myOldPositions.containsKey(f)) {
        myOldPositions.put(f, new ArrayList<Vector>());
      }
      myOldPositions.get(f).add(newOrigin);
      updateView(viewRects, f, newOrigin);
    }
  }

  private void updateView(List<Rectangle> viewRects, View f, Vector newOrigin) {
    if (!newOrigin.equals(f.bounds().get().origin)) {
      f.moveTo(newOrigin);
      f.invalidate();
    }
    viewRects.add(getRect(f));
  }

  private boolean posFree(Rectangle rect, List<Rectangle> obstacles) {
    for (Rectangle obstacle: obstacles) {
      if (rect.innerIntersects(obstacle)) {
        return false;
      }
    }
    return true;
  }

  private Rectangle getRect(View view) {
    Rectangle bounds = view.bounds().get();
    return bounds.sub(ORIGIN_SHIFT).changeDimension(bounds.dimension.add(DIMENSION_GROW));
  }

  @Override
  protected void doValidate(View.ValidationContext ctx) {
    if (myConfiguration.preventBlocksOverlapping()) {
      preventOverlapping();
    }

    myRouter.findRoutes(this);
    invalidateDecorations();
    super.doValidate(ctx);
  }

  private void invalidateDecorations() {
    for (View decoration: myDecorationGroup.children()) {
      decoration.invalidate();
    }
  }

  @Override
  public void addDecoration(View decoration) {
    myDecorationGroup.children().add(decoration);
  }

  @Override
  public DiagramView getDecoratedView() {
    return this;
  }

  private static RoutingViewConfiguration getDefaultConfiguration() {
    return new RoutingViewConfiguration() {
      @Override
      public boolean preventBlocksOverlapping() {
        return true;
      }
    };
  }
}