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

import jetbrains.jetpad.model.property.Property;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class SvgStylableElement extends SvgElement {
  private static final SvgAttributeSpec<String> CLASS = SvgAttributeSpec.createSpec("class");

  public Property<String> classAttribute() {
    return getAttribute(CLASS);
  }

  public boolean addClass(@Nonnull String cl) {
    validateClassName(cl);

    Property<String> attr = classAttribute();
    if (attr.get() == null) {
      attr.set(cl);
      return true;
    }

    if (Arrays.asList(attr.get().split(" ")).contains(cl)) {
      return false;
    }

    attr.set(attr.get() + " " + cl);
    return true;
  }

  public boolean removeClass(@Nonnull String cl) {
    validateClassName(cl);

    Property<String> attr = classAttribute();
    if (attr.get() == null) {
      return false;
    }

    List<String> classes = new ArrayList<>(Arrays.asList(attr.get().split(" ")));
    boolean result = classes.remove(cl);

    if (result) {
      attr.set(buildClassString(classes));
    }

    return result;
  }

  public void replaceClass(@Nonnull String oldClass, @Nonnull String newClass) {
    validateClassName(oldClass);
    validateClassName(newClass);

    Property<String> attr = classAttribute();
    if (attr.get() == null) {
      throw new IllegalStateException("Trying to replace class when class is empty");
    }

    List<String> classes = Arrays.asList(attr.get().split(" "));
    if (!classes.contains(oldClass)) {
      throw new IllegalStateException("Class attribute does not contain specified oldClass");
    }

    classes.set(classes.indexOf(oldClass), newClass);

    attr.set(buildClassString(classes));
  }

  public boolean toggleClass(@Nonnull String cl) {
    if (hasClass(cl)) {
      removeClass(cl);
      return false;
    } else {
      addClass(cl);
      return true;
    }
  }

  public boolean hasClass(@Nonnull String cl) {
    validateClassName(cl);

    Property<String> attr = classAttribute();
    return attr.get() != null && Arrays.asList(attr.get().split(" ")).contains(cl);
  }

  public String fullClass() {
    Property<String> attr = classAttribute();
    return (attr.get() == null ? "" : attr.get());
  }

  private String buildClassString(List<String> classes) {
    StringBuilder builder = new StringBuilder();
    for (String className : classes) {
      builder.append(className).append(' ');
    }
    return builder.toString();
  }

  private void validateClassName(@Nonnull String cl) {
    if (cl.contains(" ")) {
      throw new IllegalArgumentException("Class name cannot contain spaces");
    }
  }
}