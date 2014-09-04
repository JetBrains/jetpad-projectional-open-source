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
package jetbrains.jetpad.projectional.view.dom;

import com.google.gwt.dom.client.Element;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewPropertyKind;
import jetbrains.jetpad.projectional.view.ViewPropertySpec;

public class DomView extends View {
  public static final ViewPropertySpec<Element> ELEMENT = new ViewPropertySpec<>("element", ViewPropertyKind.RELAYOUT);

  public final Property<Element> element = getProp(ELEMENT);

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    Element e = this.element.get();
    if (e != null) {
      ctx.bounds(new Vector(e.getScrollWidth(), e.getScrollHeight()), 0);
    } else {
      ctx.bounds(Vector.ZERO, 0);
    }
  }
}