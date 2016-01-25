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
package jetbrains.jetpad.event.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.event.ClipboardContent;
import jetbrains.jetpad.event.ContentKinds;

import static com.google.gwt.query.client.GQuery.$;

public class ClipboardSupport {
  private Element myTarget;

  public ClipboardSupport(Element target) {
    myTarget = target;
  }

  public void pasteContent(final Handler<String> handler) {
    final TextArea pasteArea = createClipboardTextArea();
    new Timer() {
      @Override
      public void run() {
        RootPanel.get().remove(pasteArea);
        $(myTarget).focus();
        String text = pasteArea.getText();
        handler.handle(text);
      }
    }.schedule(20);
  }

  public void copyContent(ClipboardContent content ) {
    final TextArea copyArea = createClipboardTextArea();
    if (content.isSupported(ContentKinds.TEXT)) {
      copyArea.setText(content.get(ContentKinds.TEXT));
    } else {
      copyArea.setText(content.toString());
    }
    copyArea.selectAll();
    new Timer() {
      @Override
      public void run() {
        RootPanel.get().remove(copyArea);
        $(myTarget).focus();
      }
    }.schedule(20);
  }

  private TextArea createClipboardTextArea() {
    final TextArea pasteArea = new TextArea();
    pasteArea.setPixelSize(0, 0);
    Style style = pasteArea.getElement().getStyle();
    style.setPosition(Style.Position.FIXED);
    RootPanel.get().add(pasteArea);
    pasteArea.setFocus(true);
    return pasteArea;
  }
}