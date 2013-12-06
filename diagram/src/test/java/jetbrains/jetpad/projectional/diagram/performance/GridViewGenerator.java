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
package jetbrains.jetpad.projectional.diagram.performance;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.diagram.view.DiagramView;
import jetbrains.jetpad.projectional.diagram.view.PolyLineConnection;
import jetbrains.jetpad.projectional.view.RectView;
import jetbrains.jetpad.projectional.view.View;

public class GridViewGenerator {
  private DiagramView myView;
  private int myGridSize;

  private int myChildOffset;

  public GridViewGenerator(DiagramView diagramView, int gridSize) {
    myView = diagramView;
    myGridSize = gridSize;
  }

  public void generateGridView() {
    myChildOffset = myView.children().size();
    Vector hShift = new Vector(100, 0);
    Vector vShift = new Vector(0, 100);
    Vector origin = new Vector(0, 0);
    Vector dim = new Vector(30, 30);
    for (int i = 0; i < myGridSize; i++) {
      Vector curOrigin = origin;
      for (int j = 0; j < myGridSize; j++) {
        Block cur = createBlock(curOrigin, dim);
        Block b = getBlock(i, j - 1);
        if (b != null) {
          addConnection(b, cur, 2);
        }
        b = getBlock(i - 1, j);
        if (b != null) {
          addConnection(b, cur, 0);
        }
        b = getBlock(i - 1, j - 1);
        if (b != null) {
          addConnection(b, cur, 1);
        }
        curOrigin = curOrigin.add(hShift);
      }
      origin = origin.add(vShift);
    }
  }

  private Block createBlock(Vector origin, Vector dimension) {
    Block block = new Block(origin, dimension);
    myView.children().add(block);
    return block;
  }

  private Block getBlock(int vIndex, int hIndex) {
    if (vIndex < 0 || vIndex >= myGridSize || hIndex < 0 || hIndex >= myGridSize) return null;
    return (Block) myView.children().get(vIndex * myGridSize + hIndex + myChildOffset);
  }

  private void addConnection(Block from, Block to, int index) {
    PolyLineConnection connection = new PolyLineConnection();
    connection.fromView().set(from.input[index]);
    connection.toView().set(to.output[index]);
    connection.update(from.input[index].bounds().get().center(), to.output[index].bounds().get().center());
    myView.connections.add(connection);
  }

  private static class Block extends View {
    private RectView block = new RectView();
    private RectView[] input = new RectView[]{new RectView(), new RectView(), new RectView()};
    private RectView[] output = new RectView[]{new RectView(), new RectView(), new RectView()};

    private Block(Vector origin, Vector dimension) {
      children().add(block);
      for (RectView r: input) {
        children().add(r);
      }
      for (RectView r : output) {
        children().add(r);
      }
      block.moveTo(origin);
      block.dimension().set(dimension);
      for (RectView r : input) {
        r.dimension().set(new Vector(3, 3));
      }
      for (RectView r : output) {
        r.dimension().set(new Vector(3, 3));
      }
      validate();
    }

    @Override
    protected void doValidate(ValidationContext ctx) {
      Vector origin = block.bounds().get().origin;
      Vector dim = block.dimension().get();
      input[0].moveTo(origin.add(new Vector(dim.x / 2, dim.y)));
      input[1].moveTo(origin.add(dim));
      input[2].moveTo(origin.add(new Vector(dim.x / 2, dim.y / 2)));
      output[0].moveTo(origin.add(new Vector(dim.x / 2, 0)).sub(output[0].dimension().get()));
      output[1].moveTo(origin.sub(output[1].dimension().get()));
      output[2].moveTo(origin.add(new Vector(0, dim.y / 2)).sub(output[0].dimension().get()));

      super.doValidate(ctx);
    }
  }
}