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

import java.util.Arrays;

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

  private Element myRoot;
  private Element myCaretDiv;

  private Element myPrefix;
  private Element myCenter;
  private Element mySuffix;

  public DomTextEditor(Element root) {
    myRoot = root;

    Style rootStyle = myRoot.getStyle();
    rootStyle.setDisplay(Style.Display.INLINE_BLOCK);
    rootStyle.setPosition(Style.Position.RELATIVE);

    Element caretDiv = DOM.createDiv();
    Style caretStyle = caretDiv.getStyle();
    caretStyle.setPosition(Style.Position.ABSOLUTE);
    caretStyle.setZIndex(2);
    caretStyle.setTop(0, Style.Unit.PX);
    caretStyle.setWidth(1, Style.Unit.PX);
    caretStyle.setHeight(getLineHeight(), Style.Unit.PX);
    caretStyle.setBackgroundColor("black");
    myRoot.appendChild(caretDiv);
    myCaretDiv = caretDiv;

    Element selectionDiv = DOM.createDiv();
    Style selectionStyle = selectionDiv.getStyle();
    selectionStyle.setPosition(Style.Position.ABSOLUTE);
    selectionStyle.setZIndex(1);
    myRoot.appendChild(selectionDiv);

    myPrefix = DOM.createSpan();
    myCenter = DOM.createSpan();
    mySuffix = DOM.createSpan();

    for (Element e : Arrays.asList(myPrefix, myCenter, mySuffix)) {
      e.getStyle().setWhiteSpace(Style.WhiteSpace.NOWRAP);
    }
    myCenter.getStyle().setBackgroundColor("cyan");

    myRoot.appendChild(myPrefix);
    myRoot.appendChild(myCenter);
    myRoot.appendChild(mySuffix);

    update();
    updateCaretVisibility();
    updateCaretPosition();
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
    updateText();
  }

  public int getCaretPosition() {
    return myCaretPosition;
  }

  public void setCaretPosition(int caretPosition) {
    if (myCaretPosition == caretPosition) return;
    myCaretPosition = caretPosition;
    updateCaretPosition();
    updateText();
  }

  public boolean getSelectionVisible() {
    return mySelectionVisible;
  }

  public void setSelectionVisible(boolean visible) {
    if (mySelectionVisible == visible) return;
    mySelectionVisible = visible;
    updateText();
  }

  public int getSelectionStart() {
    return mySelectionStart;
  }

  public void setSelectionStart(int selectionStart) {
    if (mySelectionStart == selectionStart) return;
    mySelectionStart = selectionStart;
    updateText();
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

  private void updateCaretPosition() {
    Style caretStyle = myCaretDiv.getStyle();
    caretStyle.setLeft(getCaretOffset(myCaretPosition), Style.Unit.PX);
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
    myRoot.getStyle().setColor(cssColor);
  }

  private void updateBold() {
    if (myBold) {
      myRoot.getStyle().setFontWeight(Style.FontWeight.BOLD);
    } else {
      myRoot.getStyle().setFontWeight(Style.FontWeight.NORMAL);
    }
  }

  private void updateItalic() {
    if (myItalic) {
      myRoot.getStyle().setFontStyle(Style.FontStyle.ITALIC);
    } else {
      myRoot.getStyle().setFontStyle(Style.FontStyle.NORMAL);
    }
  }

  private void updateText() {
    String value = myText == null ? "" : myText;

    int left = Math.min(myCaretPosition, mySelectionStart);
    int right = Math.max(myCaretPosition, mySelectionStart);

    String prefix;
    String center;
    String suffix;
    if (mySelectionVisible) {
      prefix = value.substring(0, left);
      center = value.substring(left, right);
      suffix = value.substring(right);
    } else {
      prefix = value;
      center = "";
      suffix = "";
    }

    myPrefix.setInnerText(normalize(prefix));
    myCenter.setInnerText(normalize(center));
    mySuffix.setInnerText(normalize(suffix));


    if (value.isEmpty()) {
      myPrefix.getStyle().setWidth(1, Style.Unit.PX);
    } else {
      myPrefix.getStyle().clearWidth();
    }
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