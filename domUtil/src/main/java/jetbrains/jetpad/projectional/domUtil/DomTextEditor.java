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
package jetbrains.jetpad.projectional.domUtil;

import com.google.common.base.Objects;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.values.Color;
import jetbrains.jetpad.values.Font;
import jetbrains.jetpad.values.FontFamily;

public class DomTextEditor {
  public static final Font DEFAULT_FONT = new Font(FontFamily.MONOSPACED, 15);

  private static final TextMetrics ourDefaultFontMetrics = TextMetricsCalculator.calculate(DEFAULT_FONT);

  private int myCaretPosition;
  private int mySelectionStart;
  private boolean mySelectionVisible;
  private String myText;
  private boolean myCaretVisible;
  private Color myTextColor;

  private Font myFont = DEFAULT_FONT;
  private TextMetrics myFontMetrics = ourDefaultFontMetrics;

  private Element myTextContainer;
  private Element myCaretDiv;
  private Element mySelectionDiv;
  private Element myRoot;

  public DomTextEditor(Element root) {
    myRoot = root;

    Style rootStyle = myRoot.getStyle();
    rootStyle.setPosition(Style.Position.RELATIVE);

    myTextContainer = DOM.createSpan();
    Style textStyle = myTextContainer.getStyle();
    textStyle.setZIndex(2);
    textStyle.setWhiteSpace(Style.WhiteSpace.NOWRAP);
    myRoot.appendChild(myTextContainer);

    Element caretDiv = DOM.createDiv();
    Style caretStyle = caretDiv.getStyle();
    caretStyle.setPosition(Style.Position.ABSOLUTE);
    caretStyle.setZIndex(2);
    myRoot.appendChild(caretDiv);
    myCaretDiv = caretDiv;

    Element selectionDiv = DOM.createDiv();
    Style selectionStyle = selectionDiv.getStyle();
    selectionStyle.setPosition(Style.Position.ABSOLUTE);
    selectionStyle.setZIndex(1);
    myRoot.appendChild(selectionDiv);
    mySelectionDiv = selectionDiv;

    update();
    updateCaretVisibility();
    updateSelectionVisibility();
    updateCaretAndSelection();
  }

  public Font getFont() {
    return myFont;
  }

  public void setBold(boolean bold) {
    if (myFont.isBold() == bold) return;
    myFont = new Font(myFont.getFamily(), myFont.getSize(), bold, myFont.isItalic());
    fontChanged();
    updateBold();
  }

  public void setItalic(boolean italic) {
    if (myFont.isItalic() == italic) return;
    myFont = new Font(myFont.getFamily(), myFont.getSize(), myFont.isBold(), italic);
    fontChanged();
    updateItalic();
  }

  public void setFontFamily(FontFamily family) {
    if (Objects.equal(myFont.getFamily(), family)) return;
    myFont = new Font(family, myFont.getSize(), myFont.isBold(), myFont.isItalic());
    fontChanged();
    updateFontFamily();
  }

  public void setFontSize(int size) {
    if (myFont.getSize() == size) return;
    myFont = new Font(myFont.getFamily(), size, myFont.isBold(), myFont.isItalic());
    fontChanged();
    updateFontSize();
  }

  private void fontChanged() {
    myFontMetrics = DEFAULT_FONT.equals(myFont) ? ourDefaultFontMetrics : TextMetricsCalculator.calculate(myFont);
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
    updateText();

    if (mySelectionVisible) {
      updateSelectionBoundsAndText();
    }
  }

  public int getCaretPosition() {
    return myCaretPosition;
  }

  public void setCaretPosition(int caretPosition) {
    if (myCaretPosition == caretPosition) return;
    myCaretPosition = caretPosition;
    updateCaretPosition();

    if (mySelectionVisible) {
      updateSelectionBoundsAndText();
    }
  }

  public boolean getSelectionVisible() {
    return mySelectionVisible;
  }

  public void setSelectionVisible(boolean visible) {
    if (mySelectionVisible == visible) return;
    mySelectionVisible = visible;
    updateSelectionVisibility();
    updateSelectionBoundsAndText();
  }

  public int getSelectionStart() {
    return mySelectionStart;
  }

  public void setSelectionStart(int selectionStart) {
    if (mySelectionStart == selectionStart) return;
    mySelectionStart = selectionStart;
    updateSelectionBoundsAndText();
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
    myCaretDiv.getStyle().setVisibility(myCaretVisible ? Style.Visibility.VISIBLE : Style.Visibility.HIDDEN);
  }

  private void updateSelectionVisibility() {
    mySelectionDiv.getStyle().setVisibility(mySelectionVisible ? Style.Visibility.VISIBLE : Style.Visibility.HIDDEN);
  }

  private void updateCaretPosition() {
    myCaretDiv.getStyle().setLeft(getCaretOffset(myCaretPosition), Style.Unit.PX);
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
    selectionStyle.setBackgroundColor("Highlight");
    selectionStyle.setColor("HighlightText");

    updateSelectionBoundsAndText();
  }

  private void updateSelectionBoundsAndText() {
    int left = Math.min(myCaretPosition, mySelectionStart);
    int right = Math.max(myCaretPosition, mySelectionStart);

    String text = myText == null ? "" : myText;
    text = text.substring(left, right);

    Style selectionStyle = mySelectionDiv.getStyle();
    selectionStyle.setLeft(getCaretOffset(left), Style.Unit.PX);
    selectionStyle.setWidth(getCaretOffset(right) - getCaretOffset(left), Style.Unit.PX);

    mySelectionDiv.setInnerText(TextMetricsCalculator.normalize(text));
  }

  private void update() {
    updateColor();
    updateFontFamily();
    updateFontSize();
    updateBold();
    updateItalic();
    updateText();
    updateLineHeight();
  }

  private void updateColor() {
    myRoot.getStyle().setColor(myTextColor == null ? null : myTextColor.toCssColor());
  }

  private void updateBold() {
    myRoot.getStyle().setFontWeight(myFont.isBold() ? Style.FontWeight.BOLD : Style.FontWeight.NORMAL);
  }

  private void updateItalic() {
    myRoot.getStyle().setFontStyle(myFont.isItalic() ? Style.FontStyle.ITALIC : Style.FontStyle.NORMAL);
  }

  private void updateText() {
    if (myText == null || myText.isEmpty()) {
      myTextContainer.setInnerText(" ");
      myTextContainer.getStyle().setWidth(1, Style.Unit.PX);
      myRoot.getStyle().setHeight(getLineHeight(), Style.Unit.PX);
    } else {
      myRoot.getStyle().clearHeight();
      myTextContainer.getStyle().clearWidth();
      myTextContainer.setInnerText(TextMetricsCalculator.normalize(myText));
    }
  }

  private void updateFontFamily() {
    myRoot.getStyle().setProperty("fontFamily", TextMetricsCalculator.getFontName(myFont.getFamily()));
  }

  private void updateFontSize() {
    myRoot.getStyle().setFontSize(myFont.getSize(), Style.Unit.PX);
  }

  private void updateLineHeight() {
    myRoot.getStyle().setLineHeight(getLineHeight(), Style.Unit.PX);
  }

  public int getCaretOffset(int caretOffset) {
    if (caretOffset == 0) return 0;
    if (FontFamily.MONOSPACED == myFont.getFamily()) {
      return caretOffset * getCharWidth();
    } else {
      return TextMetricsCalculator.calculateWidth(myFont, myText.substring(0, caretOffset));
    }
  }

  private int getLineHeight() {
    return myFontMetrics.dimension().y;
  }

  private int getCharWidth() {
    return myFontMetrics.dimension().x;
  }

  public int getCaretPositionAt(int caretOffset) {
    String textValue = myText;
    if (caretOffset <= 0) return 0;
    if (textValue == null) return 0;

    if (FontFamily.MONOSPACED == myFont.getFamily()) {
      int pos = caretOffset / getCharWidth();
      int tail = caretOffset - pos * getCharWidth();
      if (tail > getCharWidth() / 2) {
        pos += 1;
      }
      int len = textValue.length();
      return pos > len ? len : pos;

    } else {
      for (int i = 0; i <= myText.length(); i++) {
        int len = TextMetricsCalculator.calculateWidth(myFont, myText.substring(0, i));
        if (len >= caretOffset) return i;
      }
      return myText.length();
    }
  }
}