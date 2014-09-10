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
package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.projectional.svg.SvgSvgElement;
import org.junit.Test;

import static org.junit.Assert.*;

public class SvgViewTest {
  private SvgSvgElement root = new SvgSvgElement();
  private SvgSvgElement altRoot = new SvgSvgElement();
  private SvgView view = new SvgView(root);

  @Test
  public void root() {
    assertTrue(root.isAttached());
    assertSame(root.container(), view.svgContainer());
  }

  @Test
  public void changeRoot() {
    view.root().set(altRoot);
    assertFalse(root.isAttached());
    assertNull(root.container());
    assertTrue(altRoot.isAttached());
    assertSame(altRoot.container(), view.svgContainer());
    assertSame(altRoot, view.svgContainer().root().get());
  }
}