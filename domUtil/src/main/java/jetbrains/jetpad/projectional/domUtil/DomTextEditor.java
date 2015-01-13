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
import jetbrains.jetpad.values.Font;
import jetbrains.jetpad.values.FontFamily;

public class DomTextEditor {
  public static final int DEFAULT_FONT_SIZE = 15;

  private static final int ourCharWidth;
  private static final int ourLineHeight;

  static {
    TextMetrics bounds = TextMetricsCalculator.calculate(new Font(FontFamily.MONOSPACED, DEFAULT_FONT_SIZE), "x");
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
  private boolean myItalic;
  private FontFamily myFontFamily = FontFamily.MONOSPACED;
  private int myFontSize = 15;

  private Element myTextContainer;
  private Element myCaretDiv;
  private Element mySelectionDiv;
  private Element myRoot;

  public DomTextEditor(Element root) {
    myRoot = root;

    Style rootStyle = myRoot.getStyle();
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

    update();
    updateCaretVisibility();
    updateSelectionVisibility();
    updateCaretAndSelection();
  }

  public boolean isBold() {
    return myBold;
  }

  public void setBold(boolean bold) {
    if (myBold == bold) return;
    myBold = bold;
    updateBold();
  }

  public boolean isItalic() {
    return myItalic;
  }

  public void setItalic(boolean italic) {
    if (myItalic == italic) return;
    myItalic = italic;
    updateItalic();
  }

  public FontFamily getFontFamily() {
    return myFontFamily;
  }

  public void setFontFamily(FontFamily family) {
    if (Objects.equal(myFontFamily, family)) return;
    myFontFamily = family;
    updateFont();
  }

  public int getFontSize() {
    return myFontSize;
  }

  public void setFontSize(int size) {
    if (myFontSize == size) return;
    myFontSize = size;
    updateFont();
  }

  public Color getTextColor() {
    return myTextColor;
  }

  public void setTextColor(Color textColor) {
    if (Objects.equal(textColor, myTextColor)) return;
    myTextColor = textColor;
    updateColor();
  }

  public String getText() {
    return myText;
  }

  public void setText(String text) {
    if (Objects.equal(text, myText)) return;
    myText = text;
    update();
  }

  public int getCaretPosition() {
    return myCaretPosition;
  }

  public void setCaretPosition(int caretPosition) {
    if (myCaretPosition == caretPosition) return;
    myCaretPosition = caretPosition;
    updateCaretPosition();

    if (mySelectionVisible) {
      updateSelectionBounds();
    }
  }

  public boolean getSelectionVisible() {
    return mySelectionVisible;
  }

  public void setSelectionVisible(boolean visible) {
    if (mySelectionVisible == visible) return;
    mySelectionVisible = visible;
    updateSelectionVisibility();
  }

  public int getSelectionStart() {
    return mySelectionStart;
  }

  public void setSelectionStart(int selectionStart) {
    if (mySelectionStart == selectionStart) return;
    mySelectionStart = selectionStart;
    updateSelectionBounds();
  }

  public boolean isCaretVisible() {
    return myCaretVisible;
  }

  public void setCaretVisible(boolean caretVisible) {
    if (myCaretVisible == caretVisible) return;
    myCaretVisible = caretVisible;
    updateCaretVisibility();
  }

  private void updateCaretVisibility() {
    Style caretStyle = myCaretDiv.getStyle();
    if (myCaretVisible) {
      caretStyle.setVisibility(Style.Visibility.VISIBLE);
    } else {
      caretStyle.setVisibility(Style.Visibility.HIDDEN);
    }
  }

  private void updateSelectionVisibility() {
    Style selectionStyle = mySelectionDiv.getStyle();
    if (mySelectionVisible) {
      selectionStyle.setVisibility(Style.Visibility.VISIBLE);
    } else {
      selectionStyle.setVisibility(Style.Visibility.HIDDEN);
    }
  }

  private void updateCaretPosition() {
    Style caretStyle = myCaretDiv.getStyle();
    caretStyle.setLeft(getCaretOffset(myCaretPosition), Style.Unit.PX);
  }

  private void updateCaretAndSelection() {
    Style caretStyle = myCaretDiv.getStyle();
    updateCaretPosition();
    caretStyle.setTop(0, Style.Unit.PX);
    caretStyle.setWidth(1, Style.Unit.PX);
    caretStyle.setHeight(getLineHeight(), Style.Unit.PX);
    caretStyle.setBackgroundColor("black");

    Style selectionStyle = mySelectionDiv.getStyle();
    selectionStyle.setTop(0, Style.Unit.PX);
    selectionStyle.setHeight(getLineHeight(), Style.Unit.PX);
    selectionStyle.setBackgroundColor("cyan");

    updateSelectionBounds();
  }

  private void updateSelectionBounds() {
    int left = Math.min(myCaretPosition, mySelectionStart);
    int right = Math.max(myCaretPosition, mySelectionStart);

    Style selectionStyle = mySelectionDiv.getStyle();
    selectionStyle.setLeft(getCaretOffset(left), Style.Unit.PX);
    selectionStyle.setWidth(getCaretOffset(right) - getCaretOffset(left), Style.Unit.PX);
  }

  private void update() {
    updateColor();
    updateBold();
    updateItalic();
    updateText();
    updateFont();
    updateLineHeight();
  }


  private void updateColor() {
    String cssColor = myTextColor != null ? myTextColor.toCssColor() : null;
    myTextContainer.getStyle().setColor(cssColor);
  }

  private void updateBold() {
    if (myBold) {
      myTextContainer.getStyle().setFontWeight(Style.FontWeight.BOLD);
    } else {
      myTextContainer.getStyle().setFontWeight(Style.FontWeight.NORMAL);
    }
  }

  private void updateItalic() {
    if (myItalic) {
      myTextContainer.getStyle().setFontStyle(Style.FontStyle.ITALIC);
    } else {
      myTextContainer.getStyle().setFontStyle(Style.FontStyle.NORMAL);
    }
  }

  private void updateText() {
    String value = myText;
    if (value == null || value.isEmpty()) {
      value = " ";
      myTextContainer.getStyle().setWidth(1, Style.Unit.PX);
    } else {
      myTextContainer.getStyle().clearWidth();
    }
    myTextContainer.setInnerText(normalize(value));
  }

  private void updateFont() {
    myRoot.getStyle().setProperty("font", myFontSize + "px " + TextMetricsCalculator.getFontName(myFontFamily));
  }

  private void updateLineHeight() {
    myRoot.getStyle().setHeight(getLineHeight(), Style.Unit.PX);
  }

  private Font getFont() {
    return new Font(myFontFamily, myFontSize, myBold, myItalic);
  }

  public int getCaretOffset(int caretOffset) {
    if (caretOffset == 0) return 0;
    if (isDefaultFont()) {
      return caretOffset * ourCharWidth;
    } else {
      return TextMetricsCalculator.calculateAprox(getFont(), myText.substring(0, caretOffset)).dimension().x;
    }
  }

  private int getLineHeight() {
    if (isDefaultFont()) {
      return ourLineHeight;
    } else {
      return TextMetricsCalculator.calculateAprox(getFont(), "x").dimension().y;
    }
  }

  public int getCaretPositionAt(int caretOffset) {
    String textValue = myText;
    if (caretOffset <= 0) return 0;
    if (textValue == null) return 0;

    if (isDefaultFont()) {
      int pos = caretOffset / ourCharWidth;
      int tail = caretOffset - pos * ourCharWidth;

      if (tail > ourCharWidth / 2) {
        pos += 1;
      }

      int len = textValue.length();
      if (pos > len) return len;

      return pos;
    } else {
      for (int i = 0; i <= myText.length(); i++) {
        int len = TextMetricsCalculator.calculateAprox(getFont(), myText.substring(0, i)).dimension().x;
        if (len >= caretOffset) return i;
      }
      return myText.length();
    }
  }

  private boolean isDefaultFont() {
    return myFontFamily.equals(FontFamily.MONOSPACED) && myFontSize == DEFAULT_FONT_SIZE;
  }
}