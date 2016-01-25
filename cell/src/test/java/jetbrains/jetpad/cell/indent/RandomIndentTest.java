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
package jetbrains.jetpad.cell.indent;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.toView.CellContainerToViewMapper;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.projectional.view.ViewContainer;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

public class RandomIndentTest extends BaseTestCase {
  private Random random = new Random(2390);

  private ViewContainer viewContainer = new ViewContainer();
  private CellContainer cellContainer = new CellContainer();
  private CellContainerToViewMapper rootMapper;
  private int myLetterCount;

  private IndentCell root = new IndentCell();

  @Before
  public void init() {
    rootMapper = new CellContainerToViewMapper(cellContainer, viewContainer.root(), viewContainer.contentRoot(), viewContainer.decorationRoot());
    rootMapper.attachRoot();

    cellContainer.root.children().add(root);
  }

  @Test
  public void test() {
    doTest();
  }

  public void doTest() {
    for (int i = 0; i < 10000; i++) {
      step();
    }
  }

  private void step() {
    Cell target = findTarget(root);

    int opKind = random.nextInt(3);

    final ObservableList<Cell> children = target.children();

    if (opKind == 0) {
      if (target instanceof IndentCell && !(target instanceof NewLineCell)) {
        int kind = random.nextInt(3);
        Cell newCell;
        if (kind == 0) {
          newCell = new IndentCell(random.nextBoolean());
        } else if (kind == 1) {
          newCell = new TextCell("" + (char) ('a' + myLetterCount++));
        } else {
          newCell = new NewLineCell();
        }
        children.add(random.nextInt(children.size() + 1), newCell);
      }
    } else if (opKind == 1) {
      if (!children.isEmpty()) {
        children.remove(random.nextInt(children.size()));
      }
    } else {
      target.visible().set(!target.visible().get());
    }
  }

  private Cell findTarget(Cell root) {
    if (root.children().isEmpty()) return root;
    if (random.nextBoolean()) return root;
    int index = random.nextInt(root.children().size());
    final Cell child = root.children().get(index);
    if (child instanceof IndentCell) {
      return findTarget(child);
    }
    return child;
  }


}