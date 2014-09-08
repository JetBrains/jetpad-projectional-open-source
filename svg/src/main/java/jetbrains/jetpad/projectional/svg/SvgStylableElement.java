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

import jetbrains.jetpad.model.property.Property;

import java.util.HashSet;
import java.util.Set;

public abstract class SvgStylableElement extends SvgElement {
  public static class ClassAttribute {
    private Set<String> classes;

    public boolean add(String cl) {
      if (classes == null) {
        classes = new HashSet<>();
      }

      return classes.add(cl);
    }

    public boolean remove(String cl) {
      if (classes == null) {
        return false;
      }

      boolean result = classes.remove(cl);
      if (classes.isEmpty()) {
        classes = null;
      }

      return result;
    }

    public void replace(String oldClass, String newClass) {
      if (!has(oldClass)) {
        throw new IllegalStateException("Class attribute does not contain specified oldClass");
      }

      classes.remove(oldClass);
      classes.add(newClass);
    }

    public void toggle(String cl) {
      if (has(cl)) {
        remove(cl);
      } else {
        add(cl);
      }
    }

    public boolean has(String cl) {
      return classes != null && classes.contains(cl);
    }

    public String getFullClass() {
      if (classes == null || classes.isEmpty()) {
        return "";
      }
      StringBuilder builder = new StringBuilder();
      for (String cl : classes) {
        builder.append(cl).append(' ');
      }
      return builder.toString();
    }

    @Override
    public String toString() {
      return getFullClass();
    }
  }

  private static final SvgAttributeSpec<ClassAttribute> CLASS = SvgAttributeSpec.createSpec("class");

  public Property<ClassAttribute> classAttribute() {
    return getAttribute(CLASS);
  }

  public boolean addClass(String cl) {
    Property<ClassAttribute> attr = classAttribute();
    if (attr.get() == null) {
      attr.set(new ClassAttribute());
    }
    return attr.get().add(cl);
  }

  public boolean removeClass(String cl) {
    ClassAttribute attr = classAttribute().get();
    return attr != null && attr.remove(cl);
  }

  public void replaceClass(String oldClass, String newClass) {
    ClassAttribute attr = classAttribute().get();
    if (attr == null) {
      throw new IllegalStateException("Trying to replace class when class is empty");
    }
    attr.replace(oldClass, newClass);
  }

  public void toggleClass(String cl) {
    ClassAttribute attr = classAttribute().get();
    if (attr == null) {
      addClass(cl);
    } else {
      attr.toggle(cl);
    }
  }

  public boolean hasClass(String cl) {
    ClassAttribute attr = classAttribute().get();
    return attr != null && attr.has(cl);
  }

  public String fullClass() {
    ClassAttribute attr = classAttribute().get();
    if (attr == null) {
      return "";
    }
    return attr.getFullClass();
  }
}