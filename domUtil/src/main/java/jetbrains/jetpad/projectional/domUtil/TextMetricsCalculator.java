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
import jetbrains.jetpad.geometry.DoubleVector;
import jetbrains.jetpad.values.Font;
import jetbrains.jetpad.values.FontFamily;

public class TextMetricsCalculator {
  private static final String METRICS_TEST_STRING = "x";

  public static String getFontName(FontFamily family) {
    if (family == FontFamily.MONOSPACED) {
      return "monospace";
    } else if (family == FontFamily.SERIF) {
      return "serif";
    }
    return family.toString();
  }

  public static TextMetrics calculateApprox(Font font) {
    return calculateApprox(font, METRICS_TEST_STRING);
  }

  public static TextMetrics calculate(Font font) {
    return calculate(font, METRICS_TEST_STRING);
  }

  public static TextMetrics calculateApprox(final Font font, String text) {
    final DoubleVector dimension = calculateDimension(font, text);
    return new TextMetrics() {
      @Override
      public DoubleVector dimension() {
        return dimension;
      }

      @Override
      public int baseLine() {
        return (2 * font.getSize()) / 3 ;
      }
    };
  }

  public static TextMetrics calculate(Font font, String text) {
    final DoubleVector dimension = calculateDimension(font, text);
    final int baseLine = fontBaseLine(font);
    return new TextMetrics() {
      @Override
      public DoubleVector dimension() {
        return dimension;
      }

      @Override
      public int baseLine() {
        return baseLine;
      }
    };
  }

  private static DoubleVector calculateDimension(Font font, String text) {
    return new DoubleVector(calculateWidth(font, text), adjustHeight(font.getSize()));
  }

  static double calculateWidth(Font font, String text) {
    Canvas canvas = canvas();
    Context2d ctx = canvas.getContext2d();
    ctx.setFont(getFontString(font));
    return ctx.measureText(normalize(text)).getWidth();
  }

  private static int adjustHeight(int height) {
    String agent = Window.Navigator.getUserAgent().toLowerCase();
    return agent.contains("firefox") ? height + 1 : height;
  }

  private static int fontBaseLine(Font font) {
    int allCharsHeight = measureHeight(font, allCharsString());
    int ascent = measureHeight(font, "A");
    int lineSpace = font.getSize() - allCharsHeight;
    return lineSpace / 2 + ascent;
  }

  static String normalize(String text) {
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

  private static int measureHeight(Font font, String text) {
    Canvas canvas = canvas();
    Context2d ctx = canvas.getContext2d();

    ctx.setFont(getFontString(font));
    ctx.setFillStyle("rgb(255, 0, 0)");

    int width = (int) ctx.measureText(text).getWidth();
    int canvasHeight = font.getSize() * 2;
    canvas.setHeight(canvasHeight + "px");
    canvas.setHeight(font.getSize() * 2 + "px");
    canvas.setWidth(width + "px");

    ctx.fillText(text, 0, font.getSize());
    ImageData data = ctx.getImageData(0, 0, width, canvasHeight);
    int firstY = canvasHeight - 1;
    int lastY = 0;
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < canvasHeight; y++) {
        int red = data.getRedAt(x, y);
        if (red != 0) {
          if (firstY > y) {
            firstY = y;
          }
          if (lastY < y) {
            lastY = y;
          }
        }
      }
    }
    return lastY - firstY;
  }

  private static String getFontString(Font font) {
    StringBuilder result = new StringBuilder();
    if (font.isBold()) {
      result.append(" bold");
    }
    if (font.isItalic()) {
      result.append(" italic");
    }
    result.append(' ').append(font.getSize()).append("px ").append(getFontName(font.getFamily()));
    return result.toString();
  }

  private static Canvas canvas() {
    Canvas canvas = Canvas.createIfSupported();
    if (canvas == null) {
      throw new IllegalStateException();
    }
    return canvas;
  }
}