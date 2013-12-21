package jetbrains.jetpad.cell.dom;

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
