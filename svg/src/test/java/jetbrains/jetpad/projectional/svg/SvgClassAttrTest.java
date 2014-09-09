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

public class SvgClassAttrTest {
  private static String cl = "class";
  private static String altCl = "alt-class";

  SvgStylableElement element = new SvgEllipseElement();

  @Test
  public void empty() {
    assertSame(element.fullClass(), "");
    assertNull(element.classAttribute().get());
  }

  @Test
  public void addClass() {
    assertFalse(element.hasClass(cl));
    element.addClass(cl);
    assertTrue(element.hasClass(cl));
  }

  @Test
  public void removeClass() {
    element.addClass(cl);
    element.removeClass(cl);
    assertFalse(element.hasClass(cl));
  }

  @Test
  public void replaceClass() {
    element.addClass(cl);
    element.replaceClass(cl, altCl);
    assertFalse(element.hasClass(cl));
    assertTrue(element.hasClass(altCl));
  }

  @Test
  public void toggleClass() {
    element.toggleClass(cl);
    assertTrue(element.hasClass(cl));
    element.toggleClass(altCl);
    assertTrue(element.hasClass(altCl));
    element.toggleClass(cl);
    assertFalse(element.hasClass(cl));
  }

  @Test
  public void eventsTriggerOnAdd() {
    final Value<Boolean> trigger = new Value<>(false);
    element.addClass("init");
    element.addListener(new SvgElementListener<Object>() {
      @Override
      public void onAttrSet(SvgAttributeEvent<Object> event) {
        if (event.getAttrSpec().equals(SvgAttributeSpec.createSpec("class"))) {
          trigger.set(true);
        }
      }
    });
    element.addClass(cl);
    assertTrue(trigger.get());
  }

  @Test
  public void eventsTriggerOnRemove() {
    final Value<Boolean> trigger = new Value<>(false);
    element.addClass("init");
    element.addClass(cl);
    element.addListener(new SvgElementListener<Object>() {
      @Override
      public void onAttrSet(SvgAttributeEvent<Object> event) {
        if (event.getAttrSpec().equals(SvgAttributeSpec.createSpec("class"))) {
          trigger.set(true);
        }
      }
    });
    element.removeClass(cl);
    assertTrue(trigger.get());
  }

  @Test
  public void eventsTriggerOnReplace() {
    final Value<Boolean> trigger = new Value<>(false);
    element.addClass("init");
    element.addClass(cl);
    element.addListener(new SvgElementListener<Object>() {
      @Override
      public void onAttrSet(SvgAttributeEvent<Object> event) {
        if (event.getAttrSpec().equals(SvgAttributeSpec.createSpec("class"))) {
          trigger.set(true);
        }
      }
    });
    element.replaceClass(cl, altCl);
    assertTrue(trigger.get());
  }

  @Test
  public void eventsTriggerOnToggle() {
    final Value<Boolean> trigger = new Value<>(false);
    element.addListener(new SvgElementListener<Object>() {
      @Override
      public void onAttrSet(SvgAttributeEvent<Object> event) {
        if (event.getAttrSpec().equals(SvgAttributeSpec.createSpec("class"))) {
          trigger.set(true);
        }
      }
    });
    element.toggleClass(cl);
    assertTrue(trigger.get());

    trigger.set(false);
    element.toggleClass(cl);
    assertTrue(trigger.get());
  }
}