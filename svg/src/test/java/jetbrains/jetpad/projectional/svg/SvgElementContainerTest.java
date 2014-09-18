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

public class SvgElementContainerTest {
  private SvgSvgElement root = new SvgSvgElement();
  private SvgElementContainer container = new SvgElementContainer(root);

  @Test
  public void root() {
    assertSame(container.root().get(), root);
    assertTrue(root.isAttached());
    assertSame(container, root.container());
  }

  @Test
  public void changeRoot() {
    SvgSvgElement newRoot = new SvgSvgElement();
    container.root().set(newRoot);
    assertTrue(newRoot.isAttached());
    assertSame(newRoot.container(), container);

    assertFalse(root.isAttached());
    assertNull(root.container());
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
  public void attachWithChildren() {
    SvgNode node1 = newNode();
    SvgNode node2 = newNode();
    node1.children().add(node2);
    assertFalse(node2.isAttached());

    root.children().add(node1);
    assertTrue(node2.isAttached());
  }

  @Test
  public void detachWithChildren() {
    SvgNode node1 = newNode();
    SvgNode node2 = newNode();
    node1.children().add(node2);

    root.children().add(node1);
    root.children().remove(node1);
    assertFalse(node2.isAttached());
    assertNull(node2.container());
  }

  @Test
  public void attachEvent() {
    final Value<Boolean> nodeAttached = new Value<>(false);
    container.addListener(new SvgElementContainerAdapter() {
      @Override
      public void onNodeAttached(SvgNode node) {
        nodeAttached.set(true);
      }
    });
    SvgNode node = newNode();
    root.children().add(node);
    assertTrue(nodeAttached.get());
  }

  @Test
  public void detachEvent() {
    final Value<Boolean> nodeDetached = new Value<>(false);
    container.addListener(new SvgElementContainerAdapter() {
      @Override
      public void onNodeDetached(SvgNode element) {
        nodeDetached.set(true);
      }
    });
    SvgNode node = newNode();
    root.children().add(node);
    assertFalse(nodeDetached.get());
    root.children().remove(node);
    assertTrue(nodeDetached.get());
  }

  @Test(expected = IllegalStateException.class)
  public void alreadyAttachedException() {
    root.attach(container);
  }

  @Test(expected = IllegalStateException.class)
  public void notAttachedDetachException() {
    newNode().detach();
  }

  @Test
  public void attributeSetEvent() {
    SvgEllipseElement element = new SvgEllipseElement();
    root.children().add(element);

    final Value<Boolean> isAttributeSet = new Value<>(false);
    container.addListener(new SvgElementContainerAdapter() {
      @Override
      public void onAttributeSet(SvgElement elt, SvgAttributeEvent event) {
        isAttributeSet.set(true);
      }
    });

    element.setAttribute("attr", "value");
    assertTrue(isAttributeSet.get());

    isAttributeSet.set(false);
    element.cx().set(10.);
    assertTrue(isAttributeSet.get());
  }

  private SvgNode newNode() {
    return new MySvgNode();
  }

  private class MySvgNode extends SvgNode {
  }
}