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
package jetbrains.jetpad.event;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static jetbrains.jetpad.event.ContentKinds.MULTILINE_TEXT;
import static jetbrains.jetpad.event.ContentKinds.SINGLE_LINE_TEXT;

public class TextContentHelper {
  private static final char CARRIAGE_RETURN = '\r';
  private static final char NEWLINE = '\n';

  public static ClipboardContent createClipboardContent(String string) {
    if (string.indexOf(NEWLINE) != -1 || string.indexOf(CARRIAGE_RETURN) != -1) {
      return new MultilineTextClipboardContent(splitByNewline(string));
    }
    return new SingleLineTextClipboardContent(string);
  }

  public static boolean isText(ClipboardContent content) {
    return content.isSupported(SINGLE_LINE_TEXT) || content.isSupported(MULTILINE_TEXT);
  }

  public static String getText(ClipboardContent content) {
    if (content.isSupported(SINGLE_LINE_TEXT)) {
      return content.get(SINGLE_LINE_TEXT);
    }
    if (content.isSupported(MULTILINE_TEXT)) {
      return joinLines(content.get(MULTILINE_TEXT));
    }
    throw new IllegalArgumentException(String.valueOf(content));
  }

  public static Iterable<String> splitByNewline(final String multiline) {
    return new Iterable<String>() {
      @Override
      public Iterator<String> iterator() {
        return new LinesIterator(multiline);
      }
    };
  }

  public static String joinLines(Iterable<String> lines) {
    StringBuilder multiline = new StringBuilder();
    for (String line : lines) {
      multiline.append(line).append(NEWLINE);
    }
    return multiline.toString();
  }

  // Recognized EOL sequences: \r, \n, \r\n, \n\r
  private static class LinesIterator implements Iterator<String> {
    private final String myMultiline;
    private int myCurrentPos;

    private LinesIterator(String multiline) {
      this.myMultiline = multiline;
    }

    @Override
    public boolean hasNext() {
      return !lookingAtEOF();
    }

    @Override
    public String next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      int lineBeginning = myCurrentPos;
      advancePastStringBody();
      int lineRightBound = myCurrentPos;
      if (!lookingAtEOF()) {
        advancePastNewline();
      }
      return myMultiline.substring(lineBeginning, lineRightBound);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    private void advancePastStringBody() {
      while (lookingAtStringBody()) {
        advance();
      }
    }

    private void advancePastNewline() {
      if (!tryAdvancePast(CARRIAGE_RETURN, NEWLINE)) {
        if (!tryAdvancePast(NEWLINE, CARRIAGE_RETURN)) {
          throw new IllegalStateException();
        }
      }
    }

    private boolean tryAdvancePast(char required, char optional) {
      if (current() == required) {
        advance();
        if (!lookingAtEOF() && current() == optional) {
          advance();
        }
        return true;
      }
      return false;
    }

    private char current() {
      return myMultiline.charAt(myCurrentPos);
    }

    private void advance() {
      myCurrentPos++;
    }

    private boolean lookingAtStringBody() {
      return !lookingAtEOF() && current() != CARRIAGE_RETURN && current() != NEWLINE;
    }

    private boolean lookingAtEOF() {
      return myCurrentPos == myMultiline.length();
    }
  }
}
