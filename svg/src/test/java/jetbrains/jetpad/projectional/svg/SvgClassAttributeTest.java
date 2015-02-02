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
package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;
import org.junit.Test;

import static org.junit.Assert.*;

public class SvgClassAttributeTest {
  private static String cl = "class";
  private static String altCl = "alt-class";
  private final String illegalCl = cl + " " + altCl;

  SvgStylableElement element = new SvgEllipseElement();

  @Test
  public void empty() {
    assertSame(element.fullClass(), "");
    assertNull(element.classAttribute().get());
  }

  @Test
  public void addClass() {
    assertFalse(element.hasClass(cl));
    assertTrue(element.addClass(cl));
    assertTrue(element.hasClass(cl));
  }

  @Test
  public void addExistingClass() {
    assertTrue(element.addClass(cl));
    assertFalse(element.addClass(cl));
  }

  @Test
  public void removeClass() {
    element.addClass(cl);
    assertTrue(element.removeClass(cl));
    assertFalse(element.hasClass(cl));
  }

  @Test
  public void removeNonexistentClass() {
    assertFalse(element.removeClass(cl));
  }

  @Test
  public void replaceClass() {
    element.addClass(cl);
    element.replaceClass(cl, altCl);
    assertFalse(element.hasClass(cl));
    assertTrue(element.hasClass(altCl));
  }

  @Test(expected = IllegalStateException.class)
  public void replaceEmptyClass() {
    element.replaceClass(cl, altCl);
  }

  @Test(expected = IllegalStateException.class)
  public void replaceNonexistentClassException() {
    element.addClass(cl);
    element.replaceClass(altCl, cl);
  }

  @Test
  public void toggleClass() {
    assertTrue(element.toggleClass(cl));
    assertTrue(element.hasClass(cl));
    assertTrue(element.toggleClass(altCl));
    assertTrue(element.hasClass(altCl));
    assertFalse(element.toggleClass(cl));
    assertFalse(element.hasClass(cl));
  }

  @Test(expected = IllegalArgumentException.class)
  public void addIllegalClassException() {
    element.addClass(illegalCl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void removeIllegalClassException() {
    element.addClass(cl);
    element.addClass(altCl);
    element.removeClass(illegalCl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void replaceIllegalClassException() {
    element.addClass(cl);
    element.addClass(altCl);
    element.replaceClass(illegalCl, cl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void toggleIllegalClassException() {
    element.toggleClass(illegalCl);
  }

  @Test
  public void eventsTriggerOnAdd() {
    final Value<Boolean> classAdded = new Value<>(false);
    element.addClass("init");
    element.addListener(new SvgElementListener<Object>() {
      @Override
      public void onAttrSet(SvgAttributeEvent<Object> event) {
        if (event.getAttrSpec().equals(SvgAttributeSpec.createSpec("class"))) {
          classAdded.set(true);
        }
      }
    });
    element.addClass(cl);
    assertTrue(classAdded.get());
  }

  @Test
  public void eventsTriggerOnRemove() {
    final Value<Boolean> classRemoved = new Value<>(false);
    element.addClass("init");
    element.addClass(cl);
    element.addListener(new SvgElementListener<Object>() {
      @Override
      public void onAttrSet(SvgAttributeEvent<Object> event) {
        if (event.getAttrSpec().equals(SvgAttributeSpec.createSpec("class"))) {
          classRemoved.set(true);
        }
      }
    });
    element.removeClass(cl);
    assertTrue(classRemoved.get());
  }

  @Test
  public void eventsTriggerOnReplace() {
    final Value<Boolean> classReplaced = new Value<>(false);
    element.addClass("init");
    element.addClass(cl);
    element.addListener(new SvgElementListener<Object>() {
      @Override
      public void onAttrSet(SvgAttributeEvent<Object> event) {
        if (event.getAttrSpec().equals(SvgAttributeSpec.createSpec("class"))) {
          classReplaced.set(true);
        }
      }
    });
    element.replaceClass(cl, altCl);
    assertTrue(classReplaced.get());
  }

  @Test
  public void eventsTriggerOnToggle() {
    final Value<Boolean> classToggled = new Value<>(false);
    element.addListener(new SvgElementListener<Object>() {
      @Override
      public void onAttrSet(SvgAttributeEvent<Object> event) {
        if (event.getAttrSpec().equals(SvgAttributeSpec.createSpec("class"))) {
          classToggled.set(true);
        }
      }
    });
    element.toggleClass(cl);
    assertTrue(classToggled.get());

    classToggled.set(false);
    element.toggleClass(cl);
    assertTrue(classToggled.get());
  }
}