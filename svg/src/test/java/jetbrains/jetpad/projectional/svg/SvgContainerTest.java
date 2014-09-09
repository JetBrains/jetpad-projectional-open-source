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
package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;
import org.junit.Test;

import static org.junit.Assert.*;

public class SvgContainerTest {
  private SvgSvgElement root = new SvgSvgElement();
  private SvgContainer container = new SvgContainer(root);

  @Test
  public void root() {
    assertSame(container.root().get(), root);
    assertTrue(root.isAttached());
    assertSame(container, root.container());
  }

  @Test
  public void attach() {
    SvgNode node = newNode();
    assertNull(node.container());
    assertFalse(node.isAttached());

    root.children().add(node);
    assertTrue(node.isAttached());
    assertSame(node.container(), container);
  }

  @Test
  public void detach() {
    SvgNode node = newNode();
    root.children().add(node);
    root.children().remove(node);
    assertFalse(node.isAttached());
    assertNull(node.container());
  }

  @Test
  public void attachEvent() {
    final Value<Boolean> trigger = new Value<>(false);
    container.addListener(new SvgContainerAdapter() {
      @Override
      public void onNodeAttached(SvgNode node) {
        trigger.set(true);
      }
    });
    SvgNode node = newNode();
    root.children().add(node);
    assertTrue(trigger.get());
  }

  @Test
  public void detachEvent() {
    final Value<Boolean> trigger = new Value<>(false);
    container.addListener(new SvgContainerAdapter() {
      @Override
      public void onNodeDetached(SvgNode element) {
        trigger.set(true);
      }
    });
    SvgNode node = newNode();
    root.children().add(node);
    assertFalse(trigger.get());
    root.children().remove(node);
    assertTrue(trigger.get());
  }

  @Test
  public void attrSetEvent() {
    SvgEllipseElement element = new SvgEllipseElement();
    root.children().add(element);

    final Value<Boolean> trigger = new Value<>(false);
    container.addListener(new SvgContainerAdapter() {
      @Override
      public void onAttrSet(SvgElement elt, SvgAttributeEvent event) {
        trigger.set(true);
      }
    });

    element.setAttribute("attr", "value");
    assertTrue(trigger.get());

    trigger.set(false);
    element.cx().set(10.);
    assertTrue(trigger.get());
  }

  private SvgNode newNode() {
    return new MySvgNode();
  }

  private class MySvgNode extends SvgNode {
  }
}