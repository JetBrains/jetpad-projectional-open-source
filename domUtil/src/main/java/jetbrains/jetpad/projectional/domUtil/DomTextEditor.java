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
package jetbrains.jetpad.projectional.domUtil;

import com.google.common.base.Objects;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.values.Color;

public class DomTextEditor {
  public static final String FONT_FAMILY = "monospace";
  public static final int FONT_SIZE = 15;

  static String font() {
    return DomTextEditor.FONT_SIZE + "px " + DomTextEditor.FONT_FAMILY;
  }

  private static int ourCharWidth;
  private static int ourLineHeight;

  static {
    TextMetrics bounds = TextMetricsCalculator.calculate(FONT_FAMILY, FONT_SIZE, "x");
    ourCharWidth = bounds.dimension().x;
    ourLineHeight = bounds.dimension().y;
  }

  private static String normalize(String text) {
    //replace space with &nbsp;
    return text.replaceAll(" ", "\u00a0");
  }

  private int myCaretPosition;
  private int mySelectionStart;
  private boolean mySelectionVisible;
  private String myText;
  private boolean myCaretVisible;
  private Color myTextColor;
  private boolean myBold;

  private Element myTextContainer;
  private Element myCaretDiv;
  private Element mySelectionDiv;
  private Element myRoot;

  public DomTextEditor(Element root) {
    myRoot = root;

    Style rootStyle = myRoot.getStyle();
    rootStyle.setProperty("font", font());
    rootStyle.setDisplay(Style.Display.INLINE_BLOCK);
    rootStyle.setPosition(Style.Position.RELATIVE);

    myTextContainer = DOM.createDiv();
    Style textStyle = myTextContainer.getStyle();
    textStyle.setZIndex(2);
    textStyle.setPosition(Style.Position.RELATIVE);
    textStyle.setWhiteSpace(Style.WhiteSpace.NOWRAP);
    myRoot.appendChild(myTextContainer);

    Element caretDiv = DOM.createDiv();
    Style caretStyle = caretDiv.getStyle();
    caretStyle.setPosition(Style.Position.ABSOLUTE);
    myRoot.appendChild(caretDiv);
    myCaretDiv = caretDiv;

    Element selectionDiv = DOM.createDiv();
    Style selectionStyle = selectionDiv.getStyle();
    selectionStyle.setPosition(Style.Position.ABSOLUTE);
    myRoot.appendChild(selectionDiv);
    mySelectionDiv = selectionDiv;

    updateText();
    updateCaretAndSelectionVisibility();
  }

  public boolean bold() {
    return myBold;
  }

  public void bold(boolean bold) {
    myBold = bold;
    updateText();
  }

  public Color textColor() {
    return myTextColor;
  }

  public void textColor(Color textColor) {
    myTextColor = textColor;
    updateText();
  }

  public String text() {
    return myText;
  }

  public void text(String text) {
    if (Objects.equal(text, myText)) return;
    myText = text;
    updateText();
  }

  public int caretPosition() {
    return myCaretPosition;
  }

  public void caretPosition(int caretPosition) {
    myCaretPosition = caretPosition;
    updateCaretAndSelection();
  }

  public boolean selectionVisible() {
    return mySelectionVisible;
  }

  public void selectionVisble(boolean visible) {
    mySelectionVisible = visible;
    updateCaretAndSelectionVisibility();
  }

  public int selectionStart() {
    return mySelectionStart;
  }

  public void selectionStart(int selectionStart) {
    mySelectionStart = selectionStart;
    updateCaretAndSelection();
  }

  public boolean caretVisible() {
    return myCaretVisible;
  }

  public void caretVisible(boolean caretVisible) {
    myCaretVisible = caretVisible;
    updateCaretAndSelectionVisibility();
  }

  private void updateCaretAndSelectionVisibility() {
    Style caretStyle = myCaretDiv.getStyle();
    if (myCaretVisible) {
      caretStyle.setVisibility(Style.Visibility.VISIBLE);
    } else {
      caretStyle.setVisibility(Style.Visibility.HIDDEN);
    }

    Style selectionStyle = mySelectionDiv.getStyle();

    if (mySelectionVisible) {
      selectionStyle.setVisibility(Style.Visibility.VISIBLE);
    } else {
      selectionStyle.setVisibility(Style.Visibility.HIDDEN);
    }
  }

  private void updateCaretAndSelection() {
    Style caretStyle = myCaretDiv.getStyle();
    caretStyle.setLeft(caretOffset(myCaretPosition), Style.Unit.PX);
    caretStyle.setTop(0, Style.Unit.PX);
    caretStyle.setWidth(1, Style.Unit.PX);
    caretStyle.setHeight(lineHeight(), Style.Unit.PX);
    caretStyle.setBackgroundColor("black");

    int left = Math.min(myCaretPosition, mySelectionStart);
    int right = Math.max(myCaretPosition, mySelectionStart);

    Style selectionStyle = mySelectionDiv.getStyle();
    selectionStyle.setTop(0, Style.Unit.PX);
    selectionStyle.setHeight(lineHeight(), Style.Unit.PX);
    selectionStyle.setLeft(caretOffset(left), Style.Unit.PX);
    selectionStyle.setWidth(caretOffset(right) - caretOffset(left), Style.Unit.PX);
    selectionStyle.setBackgroundColor("cyan");
  }

  private void updateText() {
    String newValue = myText;
    String cssColor = myTextColor != null ? myTextColor.toCssColor() : null;
    myTextContainer.getStyle().setColor(cssColor);

    if (myBold) {
      myTextContainer.getStyle().setFontWeight(Style.FontWeight.BOLD);
    } else {
      myTextContainer.getStyle().setFontWeight(Style.FontWeight.NORMAL);
    }

    if (newValue == null || newValue.isEmpty()) {
      newValue = " ";
      myTextContainer.getStyle().setWidth(1, Style.Unit.PX);
    } else {
      myTextContainer.getStyle().clearWidth();
    }

    myTextContainer.setInnerText(normalize(newValue));
  }

  public int caretOffset(int caretOffset) {
    if (caretOffset == 0) return 0;
    return caretOffset * ourCharWidth;
  }

  private int lineHeight() {
    return ourLineHeight;
  }

  public int caretPositionAt(int caretOffset) {
    String textValue = myText;
    if (caretOffset <= 0) return 0;
    if (textValue == null) return 0;

    int pos = caretOffset / ourCharWidth;
    int tail = caretOffset - pos * ourCharWidth;

    if (tail > ourCharWidth / 2) {
      pos += 1;
    }

    int len = textValue.length();
    if (pos > len) return len;

    return pos;
  }
}