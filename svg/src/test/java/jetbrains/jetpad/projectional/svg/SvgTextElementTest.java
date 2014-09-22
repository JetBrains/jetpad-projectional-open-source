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

import org.junit.Test;

import static org.junit.Assert.*;

public class SvgTextElementTest {
  private String str = "Some text";
  private String altStr = "Some other text";

  @Test
  public void initEmpty() {
    SvgTextElement element = new SvgTextElement();
    assertTrue(element.children().isEmpty());
  }

  @Test
  public void initString() {
    SvgTextElement element = new SvgTextElement(str);
    assertSame(element.children().size(), 1);
    assertSame(((SvgTextNode) element.children().get(0)).textContent().get(), str);
  }

  @Test
  public void setText() {
    SvgTextElement element = new SvgTextElement(str);
    element.setTextNode(altStr);
    assertSame(element.children().size(), 1);
    assertSame(((SvgTextNode) element.children().get(0)).textContent().get(), altStr);
  }

  @Test
  public void addText() {
    SvgTextElement element = new SvgTextElement(str);
    element.addTextNode(altStr);
    assertSame(element.children().size(), 2);
    assertSame(((SvgTextNode) element.children().get(0)).textContent().get(), str);
    assertSame(((SvgTextNode) element.children().get(1)).textContent().get(), altStr);
  }
}