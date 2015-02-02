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
package jetbrains.jetpad.projectional.view.toGwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.domUtil.DomTextEditor;
import jetbrains.jetpad.projectional.view.TextView;
import jetbrains.jetpad.values.Color;
import jetbrains.jetpad.values.FontFamily;

import static com.google.gwt.query.client.GQuery.$;

class TextViewMapper extends BaseViewMapper<TextView, Element> {
  TextViewMapper(ViewToDomContext ctx, TextView source) {
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

    conf.add(Synchronizers.forPropsOneWay(getSource().selectionVisible(), new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean value) {
        editor.setSelectionVisible(value);
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().selectionStart(), new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        editor.setSelectionStart(value);
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().text(), new WritableProperty<String>() {
      @Override
      public void set(String value) {
        editor.setText(value);
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().textColor(), new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        editor.setTextColor(value);
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().caretVisible(), new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean value) {
        editor.setCaretVisible(value);
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().caretPosition(), new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        editor.setCaretPosition(value);
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().bold(), new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean value) {
        editor.setBold(value);
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().italic(), new WritableProperty<Boolean>() {
      @Override
      public void set(Boolean value) {
        editor.setItalic(value);
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().fontFamily(), new WritableProperty<FontFamily>() {
      @Override
      public void set(FontFamily value) {
        editor.setFontFamily(value);
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().fontSize(), new WritableProperty<Integer>() {
      @Override
      public void set(Integer value) {
        editor.setFontSize(value);
      }
    }));
  }
}