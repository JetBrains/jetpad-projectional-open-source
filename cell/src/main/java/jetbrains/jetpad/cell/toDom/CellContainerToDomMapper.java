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
package jetbrains.jetpad.cell.toDom;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.edt.EventDispatchThread;
import jetbrains.jetpad.base.edt.JsEventDispatchThread;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.event.CompletionEvent;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.event.dom.ClipboardSupport;
import jetbrains.jetpad.event.dom.EventTranslator;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.domUtil.Scrolling;
import jetbrains.jetpad.projectional.domUtil.TextMetricsCalculator;
import jetbrains.jetpad.projectional.view.TextView;
import jetbrains.jetpad.animation.Animation;
import jetbrains.jetpad.animation.DomAnimations;

import java.util.Collections;

import static com.google.gwt.query.client.GQuery.$;

public class CellContainerToDomMapper extends Mapper<CellContainer, Element> {
  private static boolean ourIndentInjected;

  static final CellToDomBundle BUNDLE = GWT.create(CellToDomBundle.class);
  static final CellToDomCss CSS = BUNDLE.style();

  static native boolean isMobile() /*-{
    return $wnd.orientation !== undefined;
  }-*/;

  private static MouseEvent toMouseEvent(Event e) {
    int x = e.getClientX() + Window.getScrollLeft();
    int y = e.getClientY() + Window.getScrollTop();
    return new MouseEvent(x, y);
  }

  private static void ensureIndentInjected() {
    if (ourIndentInjected) return;

    StyleInjector.flush();

    int width = TextMetricsCalculator.calculate(TextView.DEFAULT_FONT, "xx").dimension().x;
    StyleInjector.inject("." + CSS.indented() + "{ padding-left: " + width + "px }", true);
    ourIndentInjected = true;
  }

  private static native void disablePopup(Element el) /*-{
    el.oncontextmenu = function() {
      return false;
    }
  }-*/;

  private static native void enablePopup(Element el) /*-{
    el.oncontextmenu = null;
  }-*/;


  private CellToDomContext myCellToDomContext;

  public CellContainerToDomMapper(CellContainer source, Element target) {
    super(source, target);

    CSS.ensureInjected();
    ensureIndentInjected();

    myCellToDomContext = new CellToDomContext(target);
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);

    getSource().setCellContainerPeer(createCellContainerPeer());

    disablePopup(getTarget());
    getTarget().setTabIndex(0);
    getTarget().addClassName(CSS.rootContainer());

    registerListeners();
  }

  @Override
  protected void onDetach() {
    super.onDetach();

    enablePopup(getTarget());
    getSource().resetContainerPeer();
    getTarget().removeClassName(CSS.rootContainer());

    $(getTarget()).unbind(Event.KEYEVENTS | Event.MOUSEEVENTS);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.<Cell, Element>forSingleRole(this, Properties.<Cell>constant(getSource().root), new WritableProperty<Element>() {
      @Override
      public void set(Element value) {
        if (value != null) {
          $(getTarget()).append(value);
        } else {
          $(getTarget()).html("");
        }
      }
    }, new MapperFactory<Cell, Element>() {
      @Override
      public Mapper<? extends Cell, ? extends Element> createMapper(Cell source) {
        return CellMappers.createMapper(source, myCellToDomContext);
      }
    }));

    conf.add(Synchronizers.forRegistration(new Supplier<Registration>() {
      @Override
      public Registration get() {
        return getSource().addListener(new CellContainerAdapter() {
          @Override
          public void onViewPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
            BaseCellMapper<?> mapper = (BaseCellMapper<?>) rootMapper().getDescendantMapper(cell);
            if (mapper == null) return;
            if (Cell.isPopupProp(prop)) {
              mapper.updatePopup((PropertyChangeEvent<Cell>) event);
            } else {
              mapper.refreshProperties();
            }
          }

          @Override
          public void onChildAdded(Cell parent, CollectionItemEvent<Cell> change) {
            BaseCellMapper<?> mapper = (BaseCellMapper<?>) rootMapper().getDescendantMapper(parent);
            if (mapper == null) return;
            mapper.childAdded(change);
          }

          @Override
          public void onChildRemoved(Cell parent, CollectionItemEvent<Cell> change) {
            BaseCellMapper<?> mapper = (BaseCellMapper<?>) rootMapper().getDescendantMapper(parent);
            if (mapper == null) return;
            mapper.childRemoved(change);
          }
        });
      }
    }));

    if (isMobile()) {
      conf.add(Synchronizers.forRegistration(new Supplier<Registration>() {
        @Override
        public Registration get() {
          return getSource().focusedCell.addHandler(new EventHandler<PropertyChangeEvent<Cell>>() {
            @Override
            public void onEvent(PropertyChangeEvent<Cell> event) {
              getFocusTarget().getStyle().setTop(event.getNewValue() == null ? 0 : event.getNewValue().getBounds().origin.y - getTarget().getAbsoluteTop(), Style.Unit.PX);
            }
          });
        }
      }));

      conf.add(Synchronizers.forRegistration(new Supplier<Registration>() {
        @Override
        public Registration get() {
          return new Registration() {
            private Timer myTimer;
            private Registration myReg;

            {

              myReg = getSource().focusedCell.addHandler(new EventHandler<PropertyChangeEvent<Cell>>() {
                @Override
                public void onEvent(PropertyChangeEvent<Cell> event) {
                  stopTimer();
                  if (event.getNewValue() != null) {
                    startTimer();
                  }
                }
              });
            }

            private void startTimer() {
              myTimer = new Timer() {
                @Override
                public void run() {
                  getSource().complete(new CompletionEvent(true));
                }
              };
              myTimer.schedule(500);
            }

            private void stopTimer() {
              if (myTimer != null) {
                myTimer.cancel();
                myTimer = null;
              }
            }

            @Override
            public void remove() {
              stopTimer();
              myReg.remove();
            }
          };
        }
      }));
    }
  }

  private Element getFocusTarget() {
    return getTarget();
  }

  private CellContainerPeer createCellContainerPeer() {
    return new CellContainerPeer() {
      @Override
      public int getCaretAt(TextCell tv, int x) {
        TextCellMapper textMapper = (TextCellMapper) getMapper(tv);
        return textMapper.getCaretAt(x);
      }

      @Override
      public int getCaretOffset(TextCell tv, int caret) {
        TextCellMapper textMapper = (TextCellMapper) getMapper(tv);
        return textMapper.getCaretOffset(caret);
      }

      @Override
      public Rectangle getBounds(Cell cell) {
        Rectangle result = getBaseBounds(cell);
        if (result == null) {
          result = new Rectangle(Vector.ZERO, Vector.ZERO);
        }

        return result;
      }

      private Rectangle getBaseBounds(Cell cell) {
        Mapper<? extends Cell, ? extends Element> mapper = getMapper(cell);
        if (mapper == null) {
          Rectangle result = null;
          for (Cell child : cell.children()) {
            Rectangle childBounds = getBaseBounds(child);
            if (result == null) {
              result = childBounds;
            } else if (childBounds != null) {
              result = result.union(childBounds);
            }
          }
          if (result != null) {
            return result;
          } else {
            return null;
          }
        } else {
          Element target = getElement(cell);
          int x = target.getAbsoluteLeft();
          int y = target.getAbsoluteTop();
          int width = target.getScrollWidth();
          int height = target.getScrollHeight();
          return new Rectangle(x, y, width, height);
        }
      }

      private Element getElement(Cell cell) {
        Mapper<? extends Cell, ? extends Element> mapper = getMapper(cell);
        if (mapper == null) {
          return getElement(cell.parent().get());
        }
        return mapper.getTarget();
      }

      private Mapper<? extends Cell, ? extends Element> getMapper(Cell cell) {
        return (Mapper<? extends Cell, ? extends Element>) rootMapper().getDescendantMapper(cell);
      }

      @Override
      public void scrollTo(Rectangle rect, Cell cell) {
        Scrolling.scrollTo(rect, getElement(cell));
      }

      @Override
      public void requestFocus() {
        getFocusTarget().focus();
      }

      @Override
      public ReadableProperty<Boolean> focused() {
        return myCellToDomContext.focused;
      }

      @Override
      public EventDispatchThread getEdt() {
        return JsEventDispatchThread.INSTANCE;
      }

      @Override
      public Animation fadeIn(final Cell cell, final int duration) {
        return DomAnimations.fadeIn(getMapper(cell).getTarget(), duration);
      }

      @Override
      public Animation fadeOut(final Cell cell, final int duration) {
        return DomAnimations.fadeOut(getMapper(cell).getTarget(), duration);
      }

      @Override
      public Animation showSlide(Cell cell, int duration) {
        return DomAnimations.showSlide(getMapper(cell).getTarget(), duration);
      }
    };
  }

  private void registerListeners() {
    final Element focusTarget = getFocusTarget();
    final Element target = getTarget();
    final ClipboardSupport clipboardSupport = new ClipboardSupport(focusTarget);

    $(target).mousedown(new Function() {
      @Override
      public boolean f(Event e) {
        MouseEvent event = toMouseEvent(e);
        getSource().mousePressed(event);
        $(focusTarget).focus();
        return false;
      }
    });

    $(target).mouseup(new Function() {
      public boolean f(Event e) {
        getSource().mouseReleased(toMouseEvent(e));
        return false;
      }
    });

    $(target).mousemove(new Function() {
      public boolean f(Event e) {
        getSource().mouseMoved(toMouseEvent(e));
        return false;
      }
    });

    $(target).mouseenter(new Function() {
      public boolean f(Event e) {
        getSource().mouseEntered(toMouseEvent(e));
        return false;
      }
    });

    $(target).mouseleave(new Function() {
      public boolean f(Event e) {
        getSource().mouseLeft(toMouseEvent(e));
        return false;
      }
    });

    $(focusTarget).keydown(new Function() {
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
                    getSource().keyPressed(new KeyEvent(e.key(), e.keyChar(), e.modifiers()));
                  } else {
                    getSource().paste(text);
                  }
                }
              });
              return;
            }

            if (e.is(KeyStrokeSpecs.CUT) || e.is(KeyStrokeSpecs.COPY)) {
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
    });
    $(focusTarget).keyup(new Function() {
      @Override
      public boolean f(Event e) {
        return EventTranslator.dispatchKeyRelease(e, new Handler<KeyEvent>() {
          @Override
          public void handle(KeyEvent e) {
            getSource().keyReleased(e);
          }
        });
      }
    });
    $(focusTarget).keypress(new Function() {
      @Override
      public boolean f(Event e) {
        return EventTranslator.dispatchKeyType(e, new Handler<KeyEvent>() {
          @Override
          public void handle(KeyEvent e) {
            //Space is a special key in Chrome. We emulate its typing in keydown
            if (e.keyChar() == ' ') return;
            getSource().keyTyped(e);
          }
        });
      }
    });

    $(focusTarget).focus(new Function() {
      @Override
      public boolean f(Event e) {
        myCellToDomContext.focused.set(true);
        return false;
      }
    });
    $(focusTarget).blur(new Function() {
      @Override
      public boolean f(Event e) {
        myCellToDomContext.focused.set(false);
        return false;
      }
    });
  }

  private Mapper<? extends Cell, Element> rootMapper() {
    return (Mapper<? extends Cell, Element>) getDescendantMapper(getSource().root);
  }
}