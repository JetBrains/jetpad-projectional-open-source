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
package jetbrains.jetpad.projectional.view.gwtcanvas;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.event.dom.EventTranslator;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.PropertyBinding;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.domUtil.TextMetrics;
import jetbrains.jetpad.projectional.domUtil.TextMetricsCalculator;
import jetbrains.jetpad.projectional.view.*;
import jetbrains.jetpad.projectional.view.spi.NullViewContainerPeer;
import jetbrains.jetpad.projectional.view.spi.ViewContainerPeer;
import jetbrains.jetpad.values.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.gwt.query.client.GQuery.$;

public class View2Canvas {
  private static final String FONT_NAME = "monospace";
  private static final int FONT_SIZE = 15;
  
  public static Registration map(ViewContainer container, Element el) {
    final View2Canvas v2c = new View2Canvas(container, el);
    return new Registration() {
      @Override
      public void remove() {
        v2c.dispose();
      }
    };
  }

  private int myMaxWidth = 800;
  private int myMaxHeight = 600;
  private ViewContainer myContainer;
  private Element myElement;
  private Element myCanvasContainer;
  private CanvasElement myCanvas;
  private Vector myCanvasOrigin = Vector.ZERO;
  private CompositeRegistration myOnDispose = new CompositeRegistration();
  private TextMetrics myMetrics;
  private double myScale = devicePixelRatio();

  private boolean myRepaintRequested;
  private Rectangle myRepaintArea;
  private boolean myValidationRequested;
  private Set<View> myDirtyViews = new HashSet<>();
  private Set<View> myMovedViews = new HashSet<>();
  private List<Rectangle> myDirtyAreas = new ArrayList<>();

  private View2Canvas(final ViewContainer container, final Element element) {
    myContainer = container;
    myElement = element;
    Style elementStyle = myElement.getStyle();
    elementStyle.setPadding(0, Style.Unit.PX);

    myCanvasContainer = DOM.createDiv();
    myCanvasContainer.getStyle().setPosition(Style.Position.RELATIVE);

    myCanvas = Document.get().createCanvasElement();
    myCanvas.getStyle().setPosition(Style.Position.ABSOLUTE);
    String tv = "scale(" + (1.0 / myScale) + ", " + (1.0 / myScale) + ")";
    String to = "0% 0%";
    $(myCanvas)
      .css("transform", tv)
      .css("-webkit-transform", tv)
      .css("-ms-transform", tv)
      .css("transform-origin", to)
      .css("-webkit-transform-origin", to)
      .css("-ms-transform-origin", to);

    element.appendChild(myCanvasContainer);
    myCanvasContainer.appendChild(myCanvas);

    element.getStyle().setOverflow(Style.Overflow.AUTO);

    myMetrics = TextMetricsCalculator.calculate(FONT_NAME, FONT_SIZE, "x");

    myContainer.setPeer(new MyViewContainerPeer());
    myOnDispose.add(new Registration() {
      @Override
      public void remove() {
        myContainer.setPeer(new NullViewContainerPeer());
      }
    });

    myOnDispose.add(container.root().valid().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Boolean> event) {
        if (event.getNewValue()) {
          repaint();
        } else {
          requestValidation();
        }
      }
    }));

    myOnDispose.add(PropertyBinding.bind(container.root().bounds(), new WritableProperty<Rectangle>() {
      @Override
      public void set(Rectangle value) {

        Style containerStyle = myCanvasContainer.getStyle();
        containerStyle.setWidth(value.dimension.x, Style.Unit.PX);
        containerStyle.setHeight(value.dimension.y, Style.Unit.PX);

        int width = myMaxWidth;
        int height = Math.min(myMaxHeight, value.dimension.y);
        myCanvas.setWidth((int) (width * myScale));
        myCanvas.setHeight((int) (height * myScale));

        Style elementStyle = element.getStyle();
        elementStyle.setWidth(myMaxWidth, Style.Unit.PX);
        elementStyle.setHeight(Math.min(myMaxHeight, value.dimension.y), Style.Unit.PX);
      }
    }));

    registerListeners();

    disablePopup(myElement);
    myElement.setTabIndex(0);

    if (myContainer.root().valid().get()) {
      repaint();
    } else {
      requestValidation();
    }

  }

  private void registerListeners() {
    final Value<Boolean> pressed = new Value<>(false);
    final Value<Boolean> pressedOutside = new Value<>(false);

    myOnDispose.add(eventRegistration(Event.ONMOUSEDOWN, $(Document.get()).mousedown(new Function() {
      @Override
      public boolean f(Event e) {
        pressed.set(true);
        MouseEvent evt = toMouseEvent(e);
        if (isContainerEvent(evt)) {
          myElement.focus();
          myContainer.mousePressed(evt);
          pressedOutside.set(false);
        } else {
          pressedOutside.set(true);
        }
        return false;
      }
    })));
    myOnDispose.add(eventRegistration(Event.ONMOUSEUP, $(Document.get()).mouseup(new Function() {
      @Override
      public boolean f(Event e) {
        pressed.set(false);
        MouseEvent evt = toMouseEvent(e);
        if (isContainerEvent(evt)) {
          myContainer.mouseReleased(evt);
        }
        return false;
      }
    })));
    myOnDispose.add(eventRegistration(Event.ONMOUSEMOVE, $(Document.get()).mousemove(new Function() {
      @Override
      public boolean f(Event e) {
        MouseEvent evt = toMouseEvent(e);
        if (pressed.get() && !pressedOutside.get()) {
          myContainer.mouseDragged(evt);
        } else if (isContainerEvent(evt)) {
          myContainer.mouseMoved(evt);
        }
        return false;
      }
    })));
    myOnDispose.add(eventRegistration(Event.ONKEYDOWN, $(myElement).keydown(new Function() {
      @Override
      public boolean f(Event e) {
        return EventTranslator.dispatchKeyPress(e, new Handler<KeyEvent>() {
          @Override
          public void handle(KeyEvent e) {
            myContainer.keyPressed(e);
          }
        });
      }
    })));
    myOnDispose.add(eventRegistration(Event.ONKEYUP, $(myElement).keyup(new Function() {
      @Override
      public boolean f(Event e) {
        return EventTranslator.dispatchKeyRelease(e, new Handler<KeyEvent>() {
          @Override
          public void handle(KeyEvent e) {
            myContainer.keyReleased(e);
          }
        });
      }
    })));
    myOnDispose.add(eventRegistration(Event.ONKEYPRESS, $(myElement).keypress(new Function() {
      @Override
      public boolean f(Event e) {
        return EventTranslator.dispatchKeyType(e, new Handler<KeyEvent>() {
          @Override
          public void handle(KeyEvent e) {
            myContainer.keyTyped(e);
          }
        });
      }
    })));

    myOnDispose.add(eventRegistration(Event.ONSCROLL, $(myElement).scroll(new Function() {
      @Override
      public boolean f(Event e) {
        myCanvasOrigin = new Vector(myElement.getScrollLeft(), myElement.getScrollTop());

        int maxLeft = (Math.max(myMaxWidth, myCanvasContainer.getClientWidth()) - (int) (myCanvas.getClientWidth() / myScale));
        int maxTop = myCanvasContainer.getClientHeight() - (int) (myCanvas.getClientHeight() / myScale);

        Style canvasStyle = myCanvas.getStyle();
        canvasStyle.setLeft(Math.min(maxLeft, myElement.getScrollLeft()), Style.Unit.PX);
        canvasStyle.setTop(Math.min(maxTop, myElement.getScrollTop()), Style.Unit.PX);
        repaint();
        return false;
      }
    })));
  }

  private void dispose() {
    myOnDispose.remove();
  }

  private native void disablePopup(Element el) /*-{
    el.oncontextmenu = function() {
      return false;
    }
  }-*/;

  private native double devicePixelRatio() /*-{
    return $wnd.devicePixelRatio ? $wnd.devicePixelRatio : 1.0;
  }-*/;

  private Registration eventRegistration(final int event, final GQuery query) {
    return new Registration() {
      @Override
      public void remove() {
        query.unbind(event);
      }
    };
  }

  private String font() {
    return FONT_SIZE + "px " + FONT_NAME;
  }

  private boolean isContainerEvent(MouseEvent evt) {
    return myContainer.root().bounds().get().contains(evt.location());
  }

  private MouseEvent toMouseEvent(Event e) {
    int cx = e.getClientX();
    int cy = e.getClientY();

    int scrollLeft = Window.getScrollLeft();
    int scrollTop = Window.getScrollTop();

    int absoluteLeft = myElement.getAbsoluteLeft();
    int absoluteTop = myElement.getAbsoluteTop();

    int elScrollTop = myElement.getScrollTop();
    int elScrollLeft = myElement.getScrollLeft();

    int x = cx + scrollLeft - absoluteLeft + elScrollLeft;
    int y = cy + scrollTop - absoluteTop + elScrollTop;

    return new MouseEvent(x, y);
  }

  private void requestValidation() {
    if (myValidationRequested) return;
    Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        myContainer.root().validate();

        for (View v : myDirtyViews) {
          myRepaintArea = union(myRepaintArea, v.bounds().get());
        }
        for (View v : myMovedViews) {
          myRepaintArea = union(myRepaintArea, v.bounds().get());
        }
        for (Rectangle r : myDirtyAreas) {
          myRepaintArea = union(myRepaintArea, r);
        }

        myDirtyViews.clear();
        myMovedViews.clear();
        myDirtyAreas.clear();

        repaint();
        myValidationRequested = false;
      }
    });
    myValidationRequested = true;
  }

  private void repaint() {
    if (myRepaintRequested) return;
    myRepaintRequested = true;
    Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        Context2d ctx = myCanvas.getContext2d();
        ctx.save();
        try {
          Rectangle visibleArea = visibleArea();
          Rectangle area = expand(myRepaintArea != null ? myRepaintArea.intersect(visibleArea) : visibleArea);

          ctx.scale(myScale, myScale);
          ctx.translate(-myCanvasOrigin.x, -myCanvasOrigin.y);
          ctx.rect(area.origin.x, area.origin.y, area.dimension.x, area.dimension.y);
          ctx.clip();

          ctx.clearRect(area.origin.x, area.origin.y, area.dimension.x, area.dimension.y);

          paint(ctx, area, myContainer.root());
        } finally {
          ctx.restore();
        }
        myRepaintRequested = false;
        myRepaintArea = null;
      }
    });
  }

  private Rectangle visibleArea() {
    return new Rectangle(myCanvasOrigin.x, myCanvasOrigin.y, myElement.getClientWidth(), myElement.getClientHeight());
  }

  private Rectangle expand(Rectangle rect) {
    //we expand rects to fix antialiasing clipping problems˚
    Vector d = new Vector(1, 1);
    return new Rectangle(rect.origin.sub(d), rect.dimension.add(d.mul(2)));
  }

  private void paint(Context2d ctx, Rectangle clip, View view) {
    if (!view.visible().get()) return;
    if (clip != null) {
      if (!clip.intersects(expand(view.bounds().get()))) return;
    }

    Rectangle bounds = view.bounds().get();

    Color background = view.background().get();
    if (background != null) {
      ctx.setFillStyle(background.toCssColor());
      ctx.fillRect(bounds.origin.x, bounds.origin.y, bounds.dimension.x, bounds.dimension.y);
    }

    Color border = view.border().get();
    if (border != null) {
      ctx.setStrokeStyle(border.toCssColor());
      ctx.strokeRect(bounds.origin.x, bounds.origin.y, bounds.dimension.x - 1, bounds.dimension.y - 1);
    }

    if (view instanceof RectView) {
      RectView rectView = (RectView) view;
      if (rectView.background().get() != null) {
        ctx.setFillStyle(rectView.background().get().toCssColor());
        ctx.fillRect(bounds.origin.x, bounds.origin.y, bounds.dimension.x, bounds.dimension.y);
      }
    }

    if (view instanceof LineView) {
      LineView lineView = (LineView) view;
      ctx.setStrokeStyle(lineView.color().get().toCssColor());
      Vector start = lineView.start().get();
      Vector end = lineView.end().get();
      ctx.beginPath();
      ctx.moveTo(start.x, start.y);
      ctx.lineTo(end.x, end.y);
      ctx.closePath();
      ctx.stroke();
    }

    if (view instanceof TextView) {
      TextView textView = (TextView) view;
      String text = textView.text().get();
      Vector origin = bounds.origin;

      if (textView.bold().get()) {
        ctx.setFont("bold " + font());
      } else {
        ctx.setFont(font());
      }

      ctx.setFillStyle(textView.textColor().get().toCssColor());
      ctx.fillText(text, origin.x, origin.y + myMetrics.baseLine());

      if (textView.caretVisible().get()) {
        int xOffset = Math.max(0, textView.caretPosition().get() * myMetrics.dimension().x);
        ctx.beginPath();
        ctx.moveTo(origin.x + xOffset, origin.y);
        ctx.lineTo(origin.x + xOffset, origin.y + bounds.dimension.y - 1);
        ctx.setStrokeStyle("black");
        ctx.closePath();
        ctx.stroke();
      }
    }

    for (View child : view.children()) {
      paint(ctx, clip, child);
    }
  }

  private Rectangle union(Rectangle r1, Rectangle r2) {
    if (r1 == null) return r2;
    if (r2 == null) return r1;
    return r1.union(r2);
  }

  private class MyViewContainerPeer implements ViewContainerPeer {
    @Override
    public void attach(ViewContainer container) {
    }

    @Override
    public void detach() {
    }

    @Override
    public void repaint(View view) {
      if (!view.visible().get()) return;

      if (myContainer.root().valid().get()) {
        myRepaintArea = union(myRepaintArea, view.bounds().get());
        View2Canvas.this.repaint();
      } else {
        myDirtyViews.add(view);
      }
    }

    @Override
    public void boundsChanged(View view, PropertyChangeEvent<Rectangle> change) {
      if (!view.visible().get()) return;
      if (myMovedViews.contains(view)) return;

      myMovedViews.add(view);
      myDirtyAreas.add(change.getOldValue());
    }


    @Override
    public Rectangle visibleRect() {
      myContainer.root().validate();
      return myContainer.root().bounds().get();
    }

    @Override
    public void scrollTo(View view) {
      myContainer.root().validate();

      Rectangle bounds = view.bounds().get();
      Rectangle va = visibleRect();
      if (va.contains(bounds)) return;

      Vector o = bounds.origin;
      Vector oo = bounds.origin.add(bounds.dimension);

      int clientWidth = myElement.getClientWidth();
      int clientHeight = myElement.getClientHeight();

      if (oo.x < clientWidth) {
        myElement.setScrollLeft(0);
      } else {
        int maxScrollLeft = myContainer.root().bounds().get().dimension.x - clientWidth;
        myElement.setScrollLeft(Math.min(maxScrollLeft, o.x));
      }

      int maxScrollTop = myContainer.root().bounds().get().dimension.y - clientHeight;
      if (va.origin.y < o.y) {
        myElement.setScrollTop(Math.min(maxScrollTop, (o.y + bounds.dimension.y) - clientHeight));
      } else {
        myElement.setScrollTop(Math.min(maxScrollTop, o.y));
      }
    }

    @Override
    public int textHeight() {
      return myMetrics.dimension().y;
    }

    @Override
    public int textBaseLine() {
      return myMetrics.baseLine();
    }

    @Override
    public int textWidth(String text) {
      return text.length() * myMetrics.dimension().x;
    }

    @Override
    public void requestFocus() {
      myElement.focus();
    }
  }
}