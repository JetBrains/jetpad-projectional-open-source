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
package jetbrains.jetpad.projectional.view.toGwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.domUtil.DomTextEditor;
import jetbrains.jetpad.projectional.view.TextView;

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

    conf.add(Synchronizers.forPropsOneWay(getSource().selectionVisible(), editor::setSelectionVisible));
    conf.add(Synchronizers.forPropsOneWay(getSource().selectionStart(), editor::setSelectionStart));
    conf.add(Synchronizers.forPropsOneWay(getSource().text(), editor::setText));
    conf.add(Synchronizers.forPropsOneWay(getSource().textColor(), editor::setTextColor));
    conf.add(Synchronizers.forPropsOneWay(getSource().caretVisible(), editor::setCaretVisible));
    conf.add(Synchronizers.forPropsOneWay(getSource().caretPosition(), editor::setCaretPosition));
    conf.add(Synchronizers.forPropsOneWay(getSource().bold(), editor::setBold));
    conf.add(Synchronizers.forPropsOneWay(getSource().italic(), editor::setItalic));
    conf.add(Synchronizers.forPropsOneWay(getSource().fontFamily(), editor::setFontFamily));
    conf.add(Synchronizers.forPropsOneWay(getSource().fontSize(), editor::setFontSize));
  }
}