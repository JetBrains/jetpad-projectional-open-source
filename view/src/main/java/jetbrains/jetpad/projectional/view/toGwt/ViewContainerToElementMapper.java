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
package jetbrains.jetpad.projectional.view.toGwt;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.base.edt.EventDispatchThread;
import jetbrains.jetpad.base.edt.JsEventDispatchThread;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.event.dom.ClipboardSupport;
import jetbrains.jetpad.event.dom.EventTranslator;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.mapper.gwt.GwtSynchronizers;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.*;
import jetbrains.jetpad.projectional.domUtil.DomUtil;
import jetbrains.jetpad.projectional.domUtil.Scrolling;
import jetbrains.jetpad.projectional.domUtil.TextMetrics;
import jetbrains.jetpad.projectional.domUtil.TextMetricsCalculator;
import jetbrains.jetpad.projectional.view.TextView;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewContainer;
import jetbrains.jetpad.projectional.view.dom.DomView;
import jetbrains.jetpad.projectional.view.spi.NullViewContainerPeer;
import jetbrains.jetpad.projectional.view.spi.ViewContainerPeer;
import jetbrains.jetpad.values.Font;
import jetbrains.jetpad.values.FontFamily;

import java.util.Collections;

import static com.google.gwt.query.client.GQuery.$;

public class ViewContainerToElementMapper extends Mapper<ViewContainer, Element> {
  private Element myRootDiv = DOM.createDiv();
  private ValueProperty<Rectangle> myVisibleArea = new ValueProperty<>();
  private Property<Mapper<? extends View, ? extends Element>> myRootMapper = createChildProperty();
  private final ViewToDomContext myCtx;

  public ViewContainerToElementMapper(ViewContainer source, Element target, final boolean eventsDisabled) {
    super(source, target);

    myCtx = new ViewToDomContext() {
      @Override
      public ReadableProperty<Rectangle> visibleArea() {
        return myVisibleArea;
      }

      @Override
      public MapperFactory<View, Element> getFactory() {
        return ViewMapperFactory.factory(this);
      }

      @Override
      public Boolean areEventsDisabled() {
        return eventsDisabled;
      }
    };

    disablePopup(myRootDiv);
    target.appendChild(myRootDiv);
    myRootDiv.setTabIndex(0);

    final Style rootDivStyle = myRootDiv.getStyle();
    rootDivStyle.setPosition(Style.Position.RELATIVE);
    rootDivStyle.setPadding(0, Style.Unit.PX);
    rootDivStyle.setOverflow(Style.Overflow.VISIBLE);
    rootDivStyle.setOutlineStyle(Style.OutlineStyle.NONE);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(GwtSynchronizers.forRegistration(Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        update();
      }
    })));

    conf.add(GwtSynchronizers.forRegistration(Window.addWindowScrollHandler(new Window.ScrollHandler() {
      @Override
      public void onWindowScroll(Window.ScrollEvent event) {
        update();
      }
    })));

    conf.add(Synchronizers.forPropsOneWay(getSource().root().bounds(), new WritableProperty<Rectangle>() {
      @Override
      public void set(Rectangle value) {
        Vector bottomRight = value.origin.add(value.dimension);
        myRootDiv.getStyle().setWidth(bottomRight.x, Style.Unit.PX);
        myRootDiv.getStyle().setHeight(bottomRight.y, Style.Unit.PX);
      }
    }));

    if (!myCtx.areEventsDisabled()) {
      registerListeners(conf);
    }

  }

  private void registerListeners(SynchronizersConfiguration conf) {
    final Value<Boolean> pressed = new Value<>(false);
    final Value<Boolean> pressedOutside = new Value<>(false);

    conf.add(Synchronizers.forRegistration(new Supplier<Registration>() {
      @Override
      public Registration get() {
        CompositeRegistration reg = new CompositeRegistration();

        reg.add(eventRegistration(Event.ONMOUSEDOWN, myRootDiv, new Function() {
          @Override
          public boolean f(Event e) {
            myRootDiv.focus();
            MouseEvent me = toMouseEvent(e);
            if (isDomViewEvent(me)) return true;
            pressed.set(true);
            getSource().mousePressed(me);
            pressedOutside.set(false);
            return false;
          }
        }));
        reg.add(eventRegistration(Event.ONMOUSEDOWN, Document.get(), new Function() {
          @Override
          public boolean f(Event e) {
            pressed.set(true);
            MouseEvent evt = toMouseEvent(e);
            if (!isContainerEvent(evt)) {
              pressedOutside.set(true);
            }
            return true;
          }
        }));

        reg.add(eventRegistration(Event.ONMOUSEUP, Document.get(), new Function() {
          @Override
          public boolean f(Event e) {
            pressed.set(false);
            pressedOutside.set(false);
            return true;
          }
        }));
        reg.add(eventRegistration(Event.ONMOUSEUP, myRootDiv, new Function() {
          @Override
          public boolean f(Event e) {
            pressed.set(false);
            MouseEvent me = toMouseEvent(e);
            if (isDomViewEvent(me)) return true;
            getSource().mouseReleased(me);
            return false;
          }
        }));

        reg.add(eventRegistration(Event.ONMOUSEMOVE, Document.get(), new Function() {
          @Override
          public boolean f(Event e) {
            MouseEvent evt = toMouseEvent(e);
            if (pressed.get() && !pressedOutside.get()) {
              getSource().mouseDragged(evt);
            }
            return true;
          }
        }));
        reg.add(eventRegistration(Event.ONMOUSEMOVE, myRootDiv, new Function() {
          @Override
          public boolean f(Event e) {
            MouseEvent evt = toMouseEvent(e);
            if (pressed.get() && !pressedOutside.get()) {
              getSource().mouseDragged(evt);
            } else {
              getSource().mouseMoved(evt);
            }
            return true;
          }
        }));

        reg.add(eventRegistration(Event.ONMOUSEOVER, myRootDiv, new Function() {
          @Override
          public boolean f(Event e) {
            MouseEvent me = toMouseEvent(e);
            getSource().mouseEntered(me);
            return false;
          }
        }));
        reg.add(eventRegistration(Event.ONMOUSEOUT, myRootDiv, new Function() {
          @Override
          public boolean f(Event e) {
            getSource().mouseLeft(toMouseEvent(e));
            return false;
          }
        }));

        final ClipboardSupport clipboardSupport = new ClipboardSupport(myRootDiv);
        reg.add(eventRegistration(Event.ONKEYDOWN, myRootDiv, new Function() {
          @Override
          public boolean f(Event e) {
            return EventTranslator.dispatchKeyPress(e, new Handler<KeyEvent>() {
              @Override
              public void handle(final KeyEvent e) {
                if (e.is(Key.SPACE)) {
                  getSource().keyPressed(e);
                  getSource().keyTyped(new KeyEvent(Key.SPACE, ' ', Collections.<ModifierKey>emptySet()));
                  return;
                }

                if (e.is(KeyStrokeSpecs.PASTE)) {
                  clipboardSupport.pasteContent(new Handler<String>() {
                    @Override
                    public void handle(String text) {
                      if (Strings.isNullOrEmpty(text)) {
                        getSource().keyPressed(e.copy());
                      } else {
                        getSource().paste(new PasteEvent(new TextClipboardContent(text)));
                      }
                    }
                  });
                  return;
                }

                if (e.is(KeyStrokeSpecs.COPY) || e.is(KeyStrokeSpecs.CUT)) {
                  CopyCutEvent copyEvent;
                  if (e.is(KeyStrokeSpecs.CUT)) {
                    getSource().cut(copyEvent = new CopyCutEvent(true));
                  } else {
                    getSource().copy(copyEvent = new CopyCutEvent(false));
                  }
                  ClipboardContent content = copyEvent.getResult();
                  if (content != null) {
                    clipboardSupport.copyContent(content);
                  }
                  return;
                }

                getSource().keyPressed(e);
              }
            });
          }
        }));
        reg.add(eventRegistration(Event.ONKEYUP, myRootDiv, new Function() {
          @Override
          public boolean f(Event e) {
            return EventTranslator.dispatchKeyRelease(e, new Handler<KeyEvent>() {
              @Override
              public void handle(KeyEvent e) {
                getSource().keyReleased(e);
              }
            });
          }
        }));
        reg.add(eventRegistration(Event.ONKEYPRESS, myRootDiv, new Function() {
          @Override
          public boolean f(Event e) {
            return EventTranslator.dispatchKeyType(e, new Handler<KeyEvent>() {
              @Override
              public void handle(KeyEvent e) {
                //Space is a special key in Chrome. We emulate its typing in keydown
                if (e.getKeyChar() == ' ') return;
                getSource().keyTyped(e);
              }
            });
          }
        }));

        return reg;
      }
    }));
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);
    update();

    myRootMapper.set(myCtx.getFactory().createMapper(getSource().root()));
    myRootDiv.appendChild(myRootMapper.get().getTarget());

    TextMetrics metrics = TextMetricsCalculator.calculate(TextView.DEFAULT_FONT);
    final int baseLine = metrics.baseLine();
    final int fontWidth = metrics.dimension().x;
    final int fontHeight = metrics.dimension().y;

    getSource().setPeer(new ViewContainerPeer() {
      private Registration myReg;

      @Override
      public void attach(final ViewContainer container) {
        myReg = container.root().valid().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
          @Override
          public void onEvent(PropertyChangeEvent<Boolean> event) {
            Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                container.root().validate();
              }
            });
          }
        });

        container.root().validate();
      }

      @Override
      public void detach() {
        myReg.remove();
      }

      @Override
      public void repaint(View view) {
      }

      @Override
      public Rectangle visibleRect() {
        getSource().root().validate();
        Rectangle visiblePart = DomUtil.visiblePart(myRootDiv);
        return new Rectangle(visiblePart.origin.sub(new Vector(myRootDiv.getAbsoluteLeft(), myRootDiv.getAbsoluteTop())), visiblePart.dimension);
      }

      @Override
      public void scrollTo(Rectangle rect, View view) {
        Scrolling.scrollTo(rect, (Element) myRootMapper.get().getDescendantMapper(view).getTarget());
      }

      @Override
      public void boundsChanged(View view, PropertyChangeEvent<Rectangle> change) {
      }

      @Override
      public int textHeight(Font font) {
        if (font.equals(TextView.DEFAULT_FONT)) {
          return fontHeight;
        } else {
          return TextMetricsCalculator.calculateApprox(font).dimension().y;
        }
      }

      @Override
      public int textBaseLine(Font font) {
        if (font.equals(TextView.DEFAULT_FONT)) {
          return baseLine;
        } else {
          return TextMetricsCalculator.calculateApprox(font).baseLine();
        }
      }

      @Override
      public int textWidth(Font font, String text) {
        if (TextView.DEFAULT_FONT.getFamily() == FontFamily.MONOSPACED && font.equals(TextView.DEFAULT_FONT)) {
          return text.length() * fontWidth;
        } else {
          return TextMetricsCalculator.calculateApprox(font, text).dimension().x;
        }
      }

      @Override
      public void requestFocus() {
        myRootDiv.focus();
      }

      @Override
      public Object getMappedTo(View view) {
        Mapper<? super View, ?> mapper = myRootMapper.get().getDescendantMapper(view);
        if (mapper == null) return null;
        return mapper.getTarget();
      }

      @Override
      public EventDispatchThread getEdt() {
        return JsEventDispatchThread.INSTANCE;
      }
    });
  }

  @Override
  protected void onDetach() {
    super.onDetach();
    getSource().setPeer(new NullViewContainerPeer());
  }

  private void update() {
    Rectangle newRect = new Rectangle(Window.getScrollLeft() - myRootDiv.getAbsoluteLeft(), Window.getScrollTop() - myRootDiv.getAbsoluteTop(), Window.getClientWidth(), Window.getClientHeight());
    if (myVisibleArea.get() != null && myVisibleArea.get().contains(newRect)) return;
    myVisibleArea.set(expand(newRect));
  }

  private Rectangle expand(Rectangle rect) {
    return new Rectangle(
      rect.origin.x, rect.origin.y - rect.dimension.y / 2,
      rect.dimension.x, 2 * rect.dimension.y
    );
  }

  private native void disablePopup(Element el) /*-{
    el.oncontextmenu = function () {
      return false;
    }
  }-*/;

  private boolean isDomViewEvent(MouseEvent e) {
    View targetView = getSource().root().viewAt(e.getLocation());
    return targetView instanceof DomView;
  }

  private MouseEvent toMouseEvent(Event e) {
    int cx = e.getClientX();
    int cy = e.getClientY();

    int scrollLeft = Window.getScrollLeft();
    int scrollTop = Window.getScrollTop();

    int absoluteLeft = myRootDiv.getAbsoluteLeft();
    int absoluteTop = myRootDiv.getAbsoluteTop();

    int elScrollTop = myRootDiv.getScrollTop();
    int elScrollLeft = myRootDiv.getScrollLeft();

    int x = cx + scrollLeft - absoluteLeft + elScrollLeft;
    int y = cy + scrollTop - absoluteTop + elScrollTop;

    return new MouseEvent(x, y);
  }

  private Registration eventRegistration(final int event, Object e, Function f) {
    final GQuery q = $(e);
    q.bind(event, f);
    return new Registration() {
      @Override
      protected void doRemove() {
        q.unbind(event);
      }
    };
  }

  private boolean isContainerEvent(MouseEvent evt) {
    return getSource().contentRoot().bounds().get().contains(evt.getLocation());
  }
}