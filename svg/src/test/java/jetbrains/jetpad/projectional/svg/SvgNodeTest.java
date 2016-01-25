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
package jetbrains.jetpad.projectional.svg;

import org.junit.Test;

import static org.junit.Assert.*;

public class SvgNodeTest {
  @Test
  public void nodeAdd() {
    SvgNode node1 = newNode();
    SvgNode node2 = newNode();

    assertNull(node1.parent().get());

    node1.children().add(node2);

    assertSame(node1, node2.parent().get());
    assertTrue(node1.children().size() == 1);
    assertSame(node1.children().get(0), node2);
  }

  @Test
  public void nodeRemove() {
    SvgNode node1 = newNode();
    SvgNode node2 = newNode();

    node1.children().add(node2);
    node1.children().remove(node2);

    assertNull(node2.parent().get());
    assertTrue(node1.children().isEmpty());
  }

  private SvgNode newNode() {
    return new MySvgNode();
  }

  private class MySvgNode extends SvgNode {
  }
}