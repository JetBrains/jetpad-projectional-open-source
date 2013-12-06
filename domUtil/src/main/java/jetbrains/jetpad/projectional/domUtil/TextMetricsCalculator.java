/*
 * Copyright 2012-2013 JetBrains s.r.o
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


public class TextMetricsCalculator {
  public static TextMetrics calculate(final String fontName, final int fontSize, String text) {
    Canvas canvas = Canvas.createIfSupported();
    if (canvas == null) throw new IllegalStateException();
    Context2d ctx = canvas.getContext2d();
    ctx.setFont(font(fontName, fontSize));
    final int width = (int) ctx.measureText(normalize(text)).getWidth();

    String agent = Window.Navigator.getUserAgent().toLowerCase();
    int height = fontSize;
    if (agent.contains("firefox")) {
      height += 1;
    } else if (agent.contains("chrome")) {
      height += 2;
    }
    final Vector dimension = new Vector(width, height);

    final int baseLine = fontBaseLine(fontName, fontSize);

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

  private static int fontBaseLine(String fontName, int fontSize) {
    int allCharsHeight = measureFontRange(fontName, fontSize, allCharsString()).height();
    int capsHeight = measureFontRange(fontName, fontSize, allCharsString().toUpperCase()).height();
    int descent = allCharsHeight - capsHeight;
    int ascent = allCharsHeight - descent;
    int lineSpace = fontSize - allCharsHeight;
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

  private static Metrics measureFontRange(String fontName, int fontSize, String text) {
    Canvas canvas = Canvas.createIfSupported();
    if (canvas == null) throw new IllegalStateException();

    Context2d ctx = canvas.getContext2d();
    String font = font(fontName, fontSize);

    ctx.setFont(font);
    ctx.setFillStyle("rgb(255, 0, 0)");

    int width = (int) ctx.measureText(text).getWidth();
    canvas.setHeight(fontSize * 2 + "px");
    canvas.setWidth(width + "px");

    ctx.fillText(text, 0, fontSize);
    ImageData data = ctx.getImageData(0, 0, width, fontSize * 2);
    int[] counts = new int[fontSize * 2];

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < fontSize * 2; y++) {
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

  private static String font(String fontName, int fontSize) {
    return fontSize + "px " + fontName;
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