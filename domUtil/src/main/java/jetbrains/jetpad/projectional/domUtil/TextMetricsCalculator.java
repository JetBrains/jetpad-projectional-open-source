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

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.user.client.Window;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.values.Font;
import jetbrains.jetpad.values.FontFamily;


public class TextMetricsCalculator {
  public static String getFontName(FontFamily family) {
    if (family == FontFamily.MONOSPACED) {
      return "monospace";
    } else {
      return family.toString();
    }
  }

  public static TextMetrics calculateAprox(final Font font, String text) {
    Canvas canvas = Canvas.createIfSupported();
    if (canvas == null) {
      throw new IllegalStateException();
    }
    Context2d ctx = canvas.getContext2d();
    ctx.setFont(getFontString(font));
    final int width = (int) ctx.measureText(normalize(text)).getWidth();
    int height = adjustHeight(font.getSize());
    final Vector dimension = new Vector(width, height);
    return new TextMetrics() {
      @Override
      public Vector dimension() {
        return dimension;
      }

      @Override
      public int baseLine() {
        return (2 * font.getSize()) / 3 ;
      }
    };
  }

  public static TextMetrics calculate(final Font font, String text) {
    Canvas canvas = Canvas.createIfSupported();
    if (canvas == null) {
      throw new IllegalStateException();
    }
    Context2d ctx = canvas.getContext2d();
    ctx.setFont(getFontString(font));
    final int width = (int) ctx.measureText(normalize(text)).getWidth();

    int height = adjustHeight(font.getSize());
    final Vector dimension = new Vector(width, height);

    final int baseLine = fontBaseLine(font);
    return new TextMetrics() {
      @Override
      public Vector dimension() {
        return dimension;
      }

      @Override
      public int baseLine() {
        return baseLine;
      }
    };
  }

  private static int adjustHeight(int height) {
    String agent = Window.Navigator.getUserAgent().toLowerCase();
    if (agent.contains("firefox")) {
      height += 1;
    }
    return height;
  }

  private static int fontBaseLine(Font font) {
    int allCharsHeight = measureFontRange(font, allCharsString()).height();
    int capsHeight = measureFontRange(font, allCharsString().toUpperCase()).height();
    int descent = allCharsHeight - capsHeight;
    int ascent = allCharsHeight - descent;
    int lineSpace = font.getSize() - allCharsHeight;
    return lineSpace / 2 + ascent;
  }

  private static String normalize(String text) {
    //replace space with &nbsp;
    return text.replaceAll(" ", "\u00a0");
  }

  private static String allCharsString() {
    StringBuilder allChars = new StringBuilder();
    for (char c = 'a'; c <= 'z'; c++) {
      allChars.append(c);
    }
    for (char c = 'A'; c <= 'Z'; c++) {
      allChars.append(c);
    }

    return allChars.toString();
  }

  private static Metrics measureFontRange(Font font, String text) {
    Canvas canvas = Canvas.createIfSupported();
    if (canvas == null) {
      throw new IllegalStateException();
    }

    Context2d ctx = canvas.getContext2d();

    ctx.setFont(getFontString(font));
    ctx.setFillStyle("rgb(255, 0, 0)");

    int width = (int) ctx.measureText(text).getWidth();
    canvas.setHeight(font.getSize() * 2 + "px");
    canvas.setWidth(width + "px");

    ctx.fillText(text, 0, font.getSize());
    ImageData data = ctx.getImageData(0, 0, width, font.getSize() * 2);
    int[] counts = new int[font.getSize() * 2];

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < font.getSize() * 2; y++) {
        int red = data.getRedAt(x, y);
        if (red > 0) {
          counts[y]++;
        }
      }
    }

    int firstNonZero = firstNonZero(counts);
    int lastNonZero = lastNonZero(counts);
    return new Metrics(firstNonZero, lastNonZero, width);
  }

  private static String getFontString(Font font) {
    StringBuilder result = new StringBuilder();
    if (font.isBold()) {
      result.append(" bold");
    }
    if (font.isItalic()) {
      result.append(" italic");
    }
    result.append(font.getSize()).append("px ").append(getFontName(font.getFamily()));
    return result.toString();
  }

  private static int firstNonZero(int[] counts) {
    for (int i = 0; i < counts.length; i++) {
      if (counts[i] != 0) {
        return i;
      }
    }
    throw new IllegalStateException();
  }

  private static int lastNonZero(int[] counts) {
    for (int i = counts.length - 1; i >= 0; i--) {
      if (counts[i] != 0) {
        return i;
      }
    }
    throw new IllegalStateException();
  }

  private static class Metrics {
    final int firstNonZero;
    final int lastNonZero;
    final int width;

    Metrics(int firstNonZero, int lastNonZero, int width) {
      this.firstNonZero = firstNonZero;
      this.lastNonZero = lastNonZero;
      this.width = width;
    }

    int height() {
      return lastNonZero - firstNonZero;
    }
  }
}