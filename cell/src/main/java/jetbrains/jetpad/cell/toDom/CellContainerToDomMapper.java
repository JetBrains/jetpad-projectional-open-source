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
package jetbrains.jetpad.cell.toDom;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.base.animation.Animation;
import jetbrains.jetpad.base.edt.EventDispatchThread;
import jetbrains.jetpad.base.edt.JsEventDispatchThread;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.dom.DomCell;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.indent.NewLineCell;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.event.dom.ClipboardSupport;
import jetbrains.jetpad.event.dom.EventTranslator;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.mapper.gwt.DomAnimations;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.domUtil.DomUtil;
import jetbrains.jetpad.projectional.domUtil.Scrolling;
import jetbrains.jetpad.projectional.domUtil.TextMetricsCalculator;
import jetbrains.jetpad.projectional.view.TextView;

import java.util.Collections;

import static com.google.gwt.query.client.GQuery.$;

public class CellContainerToDomMapper extends Mapper<CellContainer, Element> {
  static final CellPropertySpec<Element> ELEMENT = new CellPropertySpec<>("element");

  public static ReadableProperty<Element> elementFor(Cell cell) {
    return cell.getProp(ELEMENT);
  }

  public static Registration whenElementAvailable(Cell cell, final Runnable r) {
    ReadableProperty<Element> prop = elementFor(cell);
    if (prop.get() != null) {
      r.run();
      return Registration.EMPTY;
    } else {
      final CompositeRegistration reg = new CompositeRegistration();
      final Value<Boolean> removed = new Value<>(false);
      reg.add(prop.addHandler(new EventHandler<PropertyChangeEvent<Element>>() {
        @Override
        public void onEvent(PropertyChangeEvent<Element> event) {
          if (event.getNewValue() != null) {
            r.run();
            reg.remove();
            removed.set(true);
          }
        }
      }));

      return new Registration() {
        @Override
        public void remove() {
          if (removed.get()) return;
          reg.remove();
          removed.set(true);
        }
      };
    }
  }

  private static boolean ourIndentInjected;

  static final CellToDomBundle BUNDLE = GWT.create(CellToDomBundle.class);
  static final CellToDomCss CSS = BUNDLE.style();

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
  private int myScrollLeft;
  private int myScrollTop;
  private HandlerRegistration myWindowReg;
  private Element myLineHighlight;
  private Element myContent;

  public CellContainerToDomMapper(CellContainer source, Element target) {
    super(source, target);

    CSS.ensureInjected();
    ensureIndentInjected();

    myLineHighlight = DOM.createDiv();
    myLineHighlight.addClassName(CSS.lineHighight());

    myContent = DOM.createDiv();
    myContent.addClassName(CSS.content());


    myCellToDomContext = new CellToDomContext(target);
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);
    getSource().setCellContainerPeer(createCellContainerPeer());

    disablePopup(getTarget());
    getTarget().setTabIndex(0);
    getTarget().addClassName(CSS.rootContainer());

    getTarget().appendChild(myLineHighlight);
    getTarget().appendChild(myContent);

    refreshLineHighlight();

    myScrollLeft = Window.getScrollLeft();
    myScrollTop = Window.getScrollTop();

    myWindowReg = Window.addWindowScrollHandler(new Window.ScrollHandler() {
      @Override
      public void onWindowScroll(Window.ScrollEvent event) {
        myScrollLeft = event.getScrollLeft();
        myScrollTop = event.getScrollTop();
      }
    });
  }

  @Override
  protected void onDetach() {
    super.onDetach();

    enablePopup(getTarget());
    getSource().resetContainerPeer();
    getTarget().removeClassName(CSS.rootContainer());

    myContent.removeFromParent();
    myLineHighlight.removeFromParent();

    $(getTarget()).unbind(Event.KEYEVENTS | Event.MOUSEEVENTS);

    myWindowReg.removeHandler();
  }

  private void refreshLineHighlight() {
    Cell current = getSource().focusedCell.get();
    Style style = myLineHighlight.getStyle();
    if (current == null || !Cells.isLeaf(current)) {
      style.setVisibility(Style.Visibility.HIDDEN);
    } else {
      int deltaTop = myContent.getAbsoluteTop() - getTarget().getAbsoluteTop();
      style.setVisibility(Style.Visibility.VISIBLE);
      int rootTop = myContent.getAbsoluteTop();
      final Element currentElement = getElement(current);
      int currentTop = currentElement.getAbsoluteTop();
      style.setTop(currentTop - rootTop + deltaTop, Style.Unit.PX);
      style.setHeight(currentElement.getClientHeight(), Style.Unit.PX);
    }
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.<Cell, Element>forSingleRole(this, Properties.<Cell>constant(getSource().root), new WritableProperty<Element>() {
      @Override
      public void set(Element value) {
        if (value != null) {
          $(myContent).append(value);
        } else {
          $(myContent).html("");
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
          public void onCellPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
            BaseCellMapper<?> mapper = (BaseCellMapper<?>) rootMapper().getDescendantMapper(cell);
            if (mapper != null) {
              if (Cell.isPopupProp(prop)) {
                if (mapper.isAutoPopupManagement()) {
                  mapper.updatePopup((PropertyChangeEvent<Cell>) event);
                }
              } else {
                mapper.refreshProperties();
              }
            }

            refreshLineHighlight();
          }

          @Override
          public void onChildAdded(Cell parent, CollectionItemEvent<Cell> change) {
            BaseCellMapper<?> mapper = (BaseCellMapper<?>) rootMapper().getDescendantMapper(parent);
            if (mapper == null) return;
            mapper.childAdded(change);

            refreshLineHighlight();
          }

          @Override
          public void onChildRemoved(Cell parent, CollectionItemEvent<Cell> change) {
            BaseCellMapper<?> mapper = (BaseCellMapper<?>) rootMapper().getDescendantMapper(parent);
            if (mapper == null) return;
            mapper.childRemoved(change);

            refreshLineHighlight();
          }
        });
      }
    }));

    conf.add(Synchronizers.forRegistration(new Supplier<Registration>() {
      @Override
      public Registration get() {
        return registerListeners();
      }
    }));
  }

  private Element getFocusTarget() {
    return getTarget();
  }

  private Cell findCellFor(Element e) {
    BaseCellMapper<?> result = myCellToDomContext.findMapper(e);
    if (result != null) return result.getSource();
    Element parent = e.getParentElement();
    if (parent == null) return null;
    return findCellFor(parent);
  }

  private Mapper<? extends Cell, ? extends Element> getMapper(Cell cell) {
    return (Mapper<? extends Cell, ? extends Element>) rootMapper().getDescendantMapper(cell);
  }

  private Element getElement(Cell cell) {
    Mapper<? extends Cell, ? extends Element> mapper = getMapper(cell);
    if (mapper == null) {
      return getElement(cell.getParent());
    }
    return mapper.getTarget();
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
          if (cell instanceof NewLineCell) {
            return null;
          }  else if (cell instanceof IndentCell) {
            return Cells.indentBounds((IndentCell) cell);
          } else {
            throw new IllegalStateException();
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


      @Override
      public void scrollTo(Rectangle rect, Cell cell) {
        Scrolling.scrollTo(rect, getElement(cell));
      }

      @Override
      public Cell findCell(Cell root, Vector loc) {
        Element e = elementAt(loc.x - myScrollLeft, loc.y - myScrollTop);
        if (e == null) return null;
        Cell result = findCellFor(e);
        if (result == null) return null;
        if (Composites.isDescendant(root, result)) {
          return result;
        }
        return null;
      }


      private native Element elementAt(int x, int y) /*-{
        return $doc.elementFromPoint(x, y);
      }-*/;

      @Override
      public Rectangle visibleRect() {
        return DomUtil.visiblePart(getTarget());
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

      @Override
      public Animation hideSlide(Cell cell, int duration) {
        return DomAnimations.hideSlide(getMapper(cell).getTarget(), duration);
      }
    };
  }

  private Registration registerListeners() {
    final CompositeRegistration reg = new CompositeRegistration();

    final Element focusTarget = getFocusTarget();
    final Element target = getTarget();
    final ClipboardSupport clipboardSupport = new ClipboardSupport(focusTarget);

    final Value<Boolean> pressed = new Value<>(false);
    final Value<Boolean> pressedOutside = new Value<>(false);

    reg.add(eventRegistration(Event.ONMOUSEDOWN, target, new Function() {
      @Override
      public boolean f(Event e) {
        pressed.set(true);
        MouseEvent event = toMouseEvent(e);
        if (isDomCellEvent(event)) return true;
        getSource().mousePressed(event);
        if (!myCellToDomContext.focused.get()) {
          $(focusTarget).focus();
        }
        return !event.isConsumed();
      }
    }));

    reg.add(eventRegistration(Event.ONMOUSEDOWN, Document.get(), new Function() {
      @Override
      public boolean f(Event e) {
        pressed.set(true);
        MouseEvent evt = toMouseEvent(e);
        if (!isContainerEvent(evt)) {
          pressedOutside.set(true);
          return true;
        }
        return true;
      }
    }));

    reg.add(eventRegistration(Event.ONMOUSEUP, target, new Function() {
      @Override
      public boolean f(Event e) {
        pressed.set(false);
        pressedOutside.set(false);
        return true;
      }
    }));

    reg.add(eventRegistration(Event.ONMOUSEUP, target, new Function() {
      @Override
      public boolean f(Event e) {
        pressed.set(false);
        MouseEvent event = toMouseEvent(e);
        if (isDomCellEvent(event)) return true;
        getSource().mouseReleased(event);
        return true;
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

    reg.add(eventRegistration(Event.ONMOUSEMOVE, target, new Function() {
      public boolean f(Event e) {
        MouseEvent event = toMouseEvent(e);
        if (isDomCellEvent(event)) return true;
        if (pressed.get() && !pressedOutside.get()) {
          getSource().mouseDragged(event);
        } else {
          getSource().mouseMoved(event);
        }
        return !event.isConsumed();
      }
    }));

    reg.add(eventRegistration(Event.ONMOUSEOVER, target, new Function() {
      public boolean f(Event e) {
        MouseEvent event = toMouseEvent(e);
        if (isDomCellEvent(event)) return true;
        getSource().mouseEntered(event);
        return false;
      }
    }));

    reg.add(eventRegistration(Event.ONMOUSEOUT, target, new Function() {
      public boolean f(Event e) {
        MouseEvent event = toMouseEvent(e);
        if (isDomCellEvent(event)) return true;
        getSource().mouseLeft(event);
        return false;
      }
    }));

    reg.add(eventRegistration(Event.ONKEYDOWN, focusTarget, new Function() {
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
    }));
    reg.add(eventRegistration(Event.ONKEYUP, focusTarget, new Function() {
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
    reg.add(eventRegistration(Event.ONKEYPRESS, focusTarget, new Function() {
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

    reg.add(eventRegistration(Event.ONFOCUS, focusTarget, new Function() {
      @Override
      public boolean f(Event e) {
        myCellToDomContext.focused.set(true);
        return false;
      }
    }));
    reg.add(eventRegistration(Event.ONBLUR, focusTarget, new Function() {
      @Override
      public boolean f(Event e) {
        myCellToDomContext.focused.set(false);
        return false;
      }
    }));

    reg.add(getSource().focusedCell.addHandler(new EventHandler<PropertyChangeEvent<Cell>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Cell> event) {
        refreshLineHighlight();
      }
    }));

    return reg;
  }

  private boolean isDomCellEvent(MouseEvent e) {
    Cell target = getSource().findCell(getSource().root, e.getLocation());
    return target instanceof DomCell;
  }

  private Mapper<? extends Cell, Element> rootMapper() {
    return (Mapper<? extends Cell, Element>) getDescendantMapper(getSource().root);
  }

  private Registration eventRegistration(final int event, Object o, Function f) {
    final GQuery q = $(o);
    q.bind(event, f);
    return new Registration() {
      @Override
      public void remove() {
        q.unbind(event);
      }
    };
  }

  private boolean isContainerEvent(MouseEvent evt) {
    return getSource().root.getBounds().contains(evt.getLocation());
  }

  private MouseEvent toMouseEvent(Event e) {
    Vector base = new Vector(e.getClientX() + myScrollLeft, e.getClientY() + myScrollTop);
    return new MouseEvent(base);
  }
}