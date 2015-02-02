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
package jetbrains.jetpad.cell.util;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.TextCell;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static jetbrains.jetpad.cell.util.CellFactory.horizontal;
import static jetbrains.jetpad.cell.util.CellFactory.label;
import static org.junit.Assert.assertEquals;

public class WithPropertyIndexTest {
  private CellContainer container;
  private TextCell c = label("c");
  private TextCell b = label("b");
  private TextCell a = label("a");
  private CellPropertySpec<String> TEST_PROP = new CellPropertySpec<>("testProp");
  private WithPropertyIndex index;

  @Before
  public void init() {
    container = new CellContainer();
    container.root.children().add(horizontal(a, b, c));
  }

  @Test
  public void initialization() {
    setProp(a, b);

    initIndex();

    assertIndex(a, b);
  }

  @Test
  public void setProperty() {
    initIndex();

    setProp(a, c);

    assertIndex(a, c);
  }

  @Test
  public void unsetProperty() {
    initIndex();

    setProp(a);
    unsetProp(a);

    assertIndex();
  }

  @Test
  public void removeWithProp() {
    initIndex();

    setProp(a);

    container.root.children().clear();

    assertIndex();
  }

  @Test
  public void addWithProp() {
    initIndex();

    Cell c1 = label("c1");
    Cell c2 = label("c2");

    setProp(c1, c2);

    a.children().add(horizontal(c1, c2));


    assertIndex(c1, c2);
  }

  @Test
  public void dispose() {
    initIndex();

    setProp(a, b);

    disposeIndex();

    assertIndex();
  }

  private void initIndex() {
    index = WithPropertyIndex.forCellProperty(container, TEST_PROP);
  }

  private void disposeIndex() {
    index.dispose();
  }

  private void setProp(Cell... cells) {
    for (Cell cell : cells) {
      cell.set(TEST_PROP, "+");
    }
  }

  private void unsetProp(Cell... cells) {
    for (Cell cell : cells) {
      cell.set(TEST_PROP, null);
    }
  }

  private void assertIndex(Cell... cells) {
    assertEquals(new HashSet<>(Arrays.asList(cells)), index.withProperty());
  }
}