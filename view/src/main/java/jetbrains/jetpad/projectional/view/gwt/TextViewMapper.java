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
package jetbrains.jetpad.projectional.view.gwt;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.domUtil.DomTextEditor;
import jetbrains.jetpad.projectional.view.TextView;
import com.google.gwt.dom.client.Element;
import jetbrains.jetpad.values.Color;
import jetbrains.jetpad.values.FontFamily;

class TextViewMapper extends BaseViewMapper<TextView, Element> {
  TextViewMapper(View2DomContext ctx, TextView source) {
    super(ctx, source, DOM.createDiv());
  }

  @Override
  protected boolean isDomLayout() {
    return true;
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    final DomTextEditor editor = new DomTextEditor(getTarget());
    Style style = getTarget().getStyle();
    style.setPosition(Style.Position.ABSOLUTE);
    style.setDisplay(Style.Display.BLOCK);

    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperty(getSource().selectionVisible(), new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean value) {
        editor.selectionVisble(value);
      }
    }));
    conf.add(Synchronizers.forProperty(getSource().selectionStart(), new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        editor.selectionStart(value);
      }
    }));
    conf.add(Synchronizers.forProperty(getSource().text(), new WritableProperty<String>() {
      @Override
      public void set(String value) {
        editor.text(value);
      }
    }));
    conf.add(Synchronizers.forProperty(getSource().textColor(), new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        editor.textColor(value);
      }
    }));
    conf.add(Synchronizers.forProperty(getSource().caretVisible(), new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean value) {
        editor.caretVisible(value);
      }
    }));
    conf.add(Synchronizers.forProperty(getSource().caretPosition(), new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        editor.caretPosition(value);
      }
    }));
    conf.add(Synchronizers.forProperty(getSource().bold(), new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean value) {
        editor.bold(value);
      }
    }));
    conf.add(Synchronizers.forProperty(getSource().italic(), new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean value) {
        editor.italic(value);
      }
    }));
    conf.add(Synchronizers.forProperty(getSource().fontFamily(), new WritableProperty<FontFamily>() {
      @Override
      public void set(FontFamily value) {
        editor.fontFamily(value);
      }
    }));
    conf.add(Synchronizers.forProperty(getSource().fontSize(), new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        editor.fontSize(value);
      }
    }));
  }
}