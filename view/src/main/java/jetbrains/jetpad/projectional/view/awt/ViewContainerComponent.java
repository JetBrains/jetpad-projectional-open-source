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
package jetbrains.jetpad.projectional.view.awt;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.event.awt.EventTranslator;
import jetbrains.jetpad.geometry.*;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.view.*;
import jetbrains.jetpad.projectional.view.spi.NullViewContainerPeer;
import jetbrains.jetpad.projectional.view.spi.ViewContainerPeer;
import jetbrains.jetpad.values.Color;

import javax.swing.*;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static jetbrains.jetpad.projectional.view.awt.AwtConverters.*;

public class ViewContainerComponent extends JComponent implements Scrollable {
  static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 15);

  static final Color SELECTION_COLOR = Color.DARK_BLUE;

  private ViewContainer myContainer;
  private Registration myContainerReg = Registration.EMPTY;

  private Set<View> myMovedViews = new LinkedHashSet<View>();
  private Set<jetbrains.jetpad.geometry.Rectangle> myDirtyAreas = new LinkedHashSet<jetbrains.jetpad.geometry.Rectangle>();
  private Set<View> myDirtyViews = new LinkedHashSet<View>();
  private boolean myCaretVisible;
  private boolean myFocused;
  private Timer myTimer;
  private long myLastActionTime;
  private Set<TextView> myWithCaretVisible = new HashSet<TextView>();
  private MyViewContainerPeer myPeer = new MyViewContainerPeer();

  public ViewContainerComponent() {
    setFocusable(true);
    setFocusTraversalKeysEnabled(false);
    setDoubleBuffered(true);

    MouseAdapter mouseListener = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        handleMouseEvent(e, new Handler<jetbrains.jetpad.event.MouseEvent>() {
          @Override
          public void handle(jetbrains.jetpad.event.MouseEvent e) {
            requestFocus();
            myContainer.mousePressed(e);
          }
        });
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        handleMouseEvent(e, new Handler<jetbrains.jetpad.event.MouseEvent>() {
          @Override
          public void handle(jetbrains.jetpad.event.MouseEvent e) {
            myContainer.mouseReleased(e);
          }
        });
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        handleMouseEvent(e, new Handler<jetbrains.jetpad.event.MouseEvent>() {
          @Override
          public void handle(jetbrains.jetpad.event.MouseEvent e) {
            myContainer.mouseMoved(e);
          }
        }, false);
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        handleMouseEvent(e, new Handler<jetbrains.jetpad.event.MouseEvent>() {
          @Override
          public void handle(jetbrains.jetpad.event.MouseEvent e) {
            myContainer.mouseDragged(e);
          }
        });
      }

      private void handleMouseEvent(MouseEvent e, Handler<jetbrains.jetpad.event.MouseEvent> handler) {
        handleMouseEvent(e, handler, true);
      }

      private void handleMouseEvent(MouseEvent e, Handler<jetbrains.jetpad.event.MouseEvent> handler, boolean action) {
        if (myContainer == null) return;
        if (action) {
          actionHappened();
        }
        jetbrains.jetpad.event.MouseEvent evt = EventTranslator.translate(e);
        handler.handle(evt);
        if (evt.isConsumed()) {
          e.consume();
        }
      }
    };
    addMouseListener(mouseListener);
    addMouseMotionListener(mouseListener);
    addMouseWheelListener(new MouseWheelListener() {
      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        View current = myContainer.root().viewAt(new Vector(e.getX(), e.getY()));
        while (current != null) {
          if (current instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) current;
            if (scrollView.scroll().get() && scrollView.isVerticalScroller()) {
              Vector offset = scrollView.offset().get().sub(new Vector(0, getFontMetrics().getHeight() * e.getWheelRotation()));
              offset = offset.min(Vector.ZERO);
              offset = offset.max(scrollView.internalsBounds().sub(scrollView.maxDimension().get()).max(Vector.ZERO).negate());
              scrollView.offset().set(offset);
              e.consume();
              return;
            }
          }
          current = current.parent().get();
        }
      }
    });

    KeyListener keyListener = new KeyAdapter() {
      @Override
      public void keyTyped(KeyEvent e) {
        char ch = e.getKeyChar();
        if (e.isControlDown() || e.isMetaDown())  return;
        if (ch < 0x20 || ch == 0x7F) return;

        handleKeyEvent(e, new Handler<jetbrains.jetpad.event.KeyEvent>() {
          @Override
          public void handle(jetbrains.jetpad.event.KeyEvent item) {
            myContainer.keyTyped(item);
          }
        });
      }

      @Override
      public void keyPressed(KeyEvent e) {
        handleKeyEvent(e, new Handler<jetbrains.jetpad.event.KeyEvent>() {
          @Override
          public void handle(jetbrains.jetpad.event.KeyEvent e) {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (e.is(Key.V, ModifierKey.CONTROL) || e.is(Key.V, ModifierKey.META)) {
              if (cb.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                try {
                  String text = (String) cb.getData(DataFlavor.stringFlavor);
                  myContainer.paste(new PasteEvent(new TextClipboardContent(text)));
                  e.consume();
                  return;
                } catch (UnsupportedFlavorException ex) {
                  throw new RuntimeException(ex);
                } catch (IOException ex) {
                  throw new RuntimeException(ex);
                }
              }
            }

            if (e.is(Key.C, ModifierKey.CONTROL) || e.is(Key.C, ModifierKey.META) || e.is(Key.X, ModifierKey.CONTROL) || e.is(Key.X, ModifierKey.META)) {
              CopyCutEvent event;
              if (e.key() == Key.X) {
                myContainer.cut(event = new CopyCutEvent(true));
              } else {
                myContainer.copy(event = new CopyCutEvent(true));
              }
              ClipboardContent content = event.getResult();
              if (content != null) {
                String text;
                if (content.isSupported(ContentKinds.TEXT)) {
                  text = content.get(ContentKinds.TEXT);
                } else {
                  text = content.toString();
                }
                cb.setContents(new StringSelection(text), new ClipboardOwner() {
                  @Override
                  public void lostOwnership(Clipboard clipboard, Transferable contents) {
                  }
                });
              }
              e.consume();
              return;
            }

            myContainer.keyPressed(e);
          }
        });
      }

      @Override
      public void keyReleased(KeyEvent e) {
        handleKeyEvent(e, new Handler<jetbrains.jetpad.event.KeyEvent>() {
          @Override
          public void handle(jetbrains.jetpad.event.KeyEvent item) {
            myContainer.keyReleased(item);
          }
        });
      }

      private void handleKeyEvent(KeyEvent e, Handler<jetbrains.jetpad.event.KeyEvent> handler) {
        if (myContainer == null) return;
        actionHappened();
        jetbrains.jetpad.event.KeyEvent evt = EventTranslator.translate(e);
        handler.handle(evt);
        if (evt.isConsumed()) {
          e.consume();
        }
      }
    };
    addKeyListener(keyListener);

    myTimer = new Timer(500, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (System.currentTimeMillis() - myLastActionTime < 3000) return;
        setCaretVisible(!myCaretVisible);
      }
    });

    addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        setFocused(true);
      }

      @Override
      public void focusLost(FocusEvent e) {
        setFocused(false);
      }

      private void setFocused(boolean focused) {
        if (myFocused == focused) return;
        myFocused = focused;
        repaintCarets();
      }
    });
  }


  private void setCaretVisible(boolean visible) {
    if (myCaretVisible == visible) return;

    myCaretVisible = visible;
    repaintCarets();
  }

  private void actionHappened() {
    myCaretVisible = true;
    myLastActionTime = System.currentTimeMillis();
  }

  private void repaintCarets() {
    for (TextView tv : myWithCaretVisible) {
      myPeer.repaint(tv);
    }
  }

  public ViewContainer container() {
    return myContainer;
  }

  public void container(ViewContainer container) {
    if (myContainer != null) {
      myContainer.setPeer(new NullViewContainerPeer());
      myContainerReg.remove();
      myTimer.stop();
      myContainerReg = null;
    }

    myContainer = container;

    if (myContainer != null) {
      myContainer.setPeer(myPeer);
      validateRoot();
      myContainerReg = new CompositeRegistration(
        myContainer.root().bounds().addHandler(new EventHandler<PropertyChangeEvent<jetbrains.jetpad.geometry.Rectangle>>() {
          @Override
          public void onEvent(PropertyChangeEvent<jetbrains.jetpad.geometry.Rectangle> event) {
            revalidate();
          }
        }),
        myContainer.root().valid().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
          @Override
          public void onEvent(PropertyChangeEvent<Boolean> event) {
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                validateRoot();
              }
            });
          }
        }),
        myContainer.addListener(new ViewContainerAdapter() {
          @Override
          public void onPropertySet(View view, ViewPropertySpec<?> prop, PropertyChangeEvent<?> event) {
            if (view instanceof TextView && prop == TextView.CARET_VISIBLE) {
              TextView textView = (TextView) view;
              if (textView.caretVisible().get()) {
                myWithCaretVisible.add(textView);
              } else {
                myWithCaretVisible.remove(textView);
              }
            }
          }

          @Override
          public void onViewAttached(View view) {
            if (view instanceof TextView) {
              TextView textView = (TextView) view;
              if (textView.caretVisible().get()) {
                myWithCaretVisible.add(textView);
              }
            }
          }

          @Override
          public void onViewDetached(View view) {
            myDirtyAreas.add(view.bounds().get());
            if (view instanceof TextView) {
              TextView textView = (TextView) view;
              myWithCaretVisible.remove(textView);
            }
          }
        })
      );
      myTimer.start();
    }
  }

  private void validateRoot() {
    try {
      myContainer.root().validate();
    } finally {
      for (View v : myDirtyViews) {
        repaint(v.bounds().get());
      }
      myDirtyViews.clear();

      for (View v : myMovedViews) {
        repaint(v.bounds().get());
      }

      for (jetbrains.jetpad.geometry.Rectangle r : myDirtyAreas) {
        repaint(r);
      }
      myMovedViews.clear();
    }
  }

  @Override
  public void doLayout() {
    View root = myContainer.root();
    root.validate();
    Vector dim = root.bounds().get().dimension;
    setPreferredSize(new Dimension(dim.x, dim.y));
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    g.setColor(toAwtColor(jetbrains.jetpad.values.Color.WHITE));
    g.fillRect(0, 0, getWidth(), getHeight());

    myContainer.root().validate();
    paint(g, myContainer.root());
  }

  private void paint(Graphics g, View view) {
    if (!view.visible().get()) return;

    java.awt.Rectangle clip = g.getClipBounds();

    jetbrains.jetpad.geometry.Rectangle bounds = view.bounds().get();

    if (clip != null) {
      jetbrains.jetpad.geometry.Rectangle rect = new jetbrains.jetpad.geometry.Rectangle(clip.x, clip.y, clip.width, clip.height);

      if (!rect.intersects(bounds)) return;
    }
    g.clipRect(bounds.origin.x, bounds.origin.y, bounds.dimension.x, bounds.dimension.y);

    Color background = view.background().get();
    if (background != null) {
      g.setColor(toAwtColor(background));
      g.fillRect(bounds.origin.x, bounds.origin.y, bounds.dimension.x, bounds.dimension.y);
    }

    Color border = view.border().get();
    if (border != null) {
      g.setColor(toAwtColor(border));
      g.drawRect(bounds.origin.x, bounds.origin.y, bounds.dimension.x - 1, bounds.dimension.y - 1);
    }

    if (view instanceof RectView) {
      RectView rectView = (RectView) view;
      g.setColor(toAwtColor(rectView.background().get()));

      g.fillRect(bounds.origin.x, bounds.origin.y, bounds.dimension.x, bounds.dimension.y);
    }

    if (view instanceof LineView) {
      LineView lineView = (LineView) view;
      g.setColor(toAwtColor(lineView.color().get()));
      Vector start = lineView.start().get();
      Vector end = lineView.end().get();
      g.drawLine(start.x, start.y, end.x, end.y);
    }

    if (view instanceof TextView) {
      TextView textView = (TextView) view;
      String text = textView.text().get();
      Vector origin = bounds.origin;

      Font font = FONT;
      if (textView.bold().get()) {
        font = font.deriveFont(Font.BOLD, font.getSize());
      }
      g.setFont(font);

      g.setColor(toAwtColor(textView.textColor().get()));
      g.drawString(text, origin.x, origin.y + textView.baseLine());

      if (textView.selectionVisible().get()) {
        int start = textView.selectionStart().get();
        int end = textView.caretPosition().get();

        int left = Math.min(start, end);
        int xLeft = xOffset(g, text, left);
        int right = Math.max(start, end);
        int xRight = xOffset(g, text, right);
        g.setColor(toAwtColor(SELECTION_COLOR));
        g.fillRect(origin.x + xLeft, origin.y, xRight - xLeft - 1, bounds.dimension.y - 1);


        Graphics g2 = g.create();
        g2.setColor(toAwtColor(Color.WHITE));
        g2.drawString(text.substring(left, right), origin.x + xLeft, origin.y + textView.baseLine());
      }

      if (textView.caretVisible().get() && myCaretVisible && myFocused) {
        int xOffset = xOffset(g, text, textView.caretPosition().get());
        g.drawLine(origin.x + xOffset, origin.y, origin.x + xOffset, origin.y + bounds.dimension.y - 1);
      }
    }

    if (view instanceof ScrollView) {
      ScrollView scrollView = (ScrollView) view;
      jetbrains.jetpad.geometry.Rectangle additionalClip = scrollView.bounds().get();
      if (scrollView.isHorizontalScroller()) {
        paintScroller(g, scrollView, false);
        additionalClip = additionalClip.changeDimension(additionalClip.dimension.sub(new Vector(0, scrollView.xScrollWidth())));
      }

      if (scrollView.isVerticalScroller()) {
        paintScroller(g, scrollView, true);
        additionalClip = additionalClip.changeDimension(additionalClip.dimension.sub(new Vector(scrollView.yScrollWidth(), 0)));
      }

      g.clipRect(additionalClip.origin.x, additionalClip.origin.y, additionalClip.dimension.x, additionalClip.dimension.y);
    }

    if (view instanceof MultiPointView) {
      MultiPointView multiPoint = (MultiPointView) view;
      g.setColor(toAwtColor(multiPoint.color().get()));

      int n = multiPoint.points.size();
      int[] xs = new int[n];
      int[] ys = new int[n];

      for (int i = 0; i < n; i++) {
        Vector point = multiPoint.points.get(i);
        xs[i] = point.x;
        ys[i] = point.y;
      }

      if (!multiPoint.points.isEmpty()) {
        if (view instanceof PolyLineView) {
          g.drawPolyline(xs, ys, n);
        } else {
          g.fillPolygon(xs, ys, n);
        }
      }
    }

    for (View child : view.children()) {
      paint(g.create(), child);
    }
  }

  private void paintScroller(Graphics g, ScrollView scrollView, boolean vertical) {
    jetbrains.jetpad.geometry.Rectangle bounds = scrollView.bounds().get();

    Color scrollerBackground = Color.GRAY;
    Color scrollerColor = Color.BLACK;

    g.setColor(toAwtColor(scrollerBackground));

    if (vertical) {
      g.fillRect(bounds.origin.x + bounds.dimension.x - scrollView.yScrollWidth(), bounds.origin.y, scrollView.yScrollWidth(), bounds.dimension.y);
    } else {
      g.fillRect(bounds.origin.x, bounds.origin.y + bounds.dimension.y - scrollView.xScrollWidth(), bounds.dimension.x, scrollView.xScrollWidth());
    }

    Vector intBounds = scrollView.internalsBounds();
    Vector offset = scrollView.offset().get();
    Vector maxDim = scrollView.maxDimension().get();

    double total = vertical ? intBounds.y : intBounds.x;
    double startPercentage = (- (vertical ? offset.y : offset.x) / total);
    double heightPercentage = ((vertical ? maxDim.y : maxDim.x) / total);

    int max = vertical ? maxDim.y : maxDim.x;

    double sOffset = startPercentage * max;

    g.setColor(toAwtColor(scrollerColor));


    if (vertical) {
      g.fillRect(
        bounds.origin.x + bounds.dimension.x - scrollView.yScrollWidth(),
        bounds.origin.y + (int) sOffset,
        scrollView.yScrollWidth(),
        (int) (heightPercentage * max));
    } else {
      g.fillRect(
        bounds.origin.x + (int) sOffset,
        bounds.origin.y + bounds.dimension.x - scrollView.xScrollWidth(),
        (int) (heightPercentage * max),
        scrollView.xScrollWidth());
    }
  }

  private int xOffset(Graphics g, String text, int pos) {
    return Math.max(0, g.getFontMetrics().stringWidth(text.substring(0, pos)));
  }

  private FontMetrics getFontMetrics() {
    return Toolkit.getDefaultToolkit().getFontMetrics(FONT);
  }

  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  @Override
  public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
    return getFontMetrics().getHeight();
  }

  @Override
  public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
    if (orientation == SwingConstants.VERTICAL) {
      return visibleRect.width;
    } else {
      return visibleRect.height;
    }
  }

  @Override
  public boolean getScrollableTracksViewportWidth() {
    JViewport viewPort = (JViewport) getParent();
    return viewPort.getWidth() > getPreferredSize().width;
  }

  private void repaint(jetbrains.jetpad.geometry.Rectangle rect) {
    repaint(new Rectangle(rect.origin.x, rect.origin.y, rect.dimension.x, rect.dimension.y));
  }

  @Override
  public boolean getScrollableTracksViewportHeight() {
    JViewport viewPort = (JViewport) getParent();
    return viewPort.getHeight() > getPreferredSize().height;
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
        ViewContainerComponent.this.repaint(view.bounds().get());
      } else {
        myDirtyViews.add(view);
      }
    }

    @Override
    public jetbrains.jetpad.geometry.Rectangle visibleRect() {
      Rectangle visibleRect = getVisibleRect();
      return new jetbrains.jetpad.geometry.Rectangle(visibleRect.x, visibleRect.y, visibleRect.width, visibleRect.height);
    }

    @Override
    public void scrollTo(View view) {
      myContainer.root().validate();

      jetbrains.jetpad.geometry.Rectangle bounds = view.bounds().get();
      scrollRectToVisible(new Rectangle(bounds.origin.x, bounds.origin.y, bounds.dimension.x, bounds.dimension.y));
    }

    @Override
    public void boundsChanged(View view, PropertyChangeEvent<jetbrains.jetpad.geometry.Rectangle> change) {
      if (!view.visible().get()) return;

      if (myMovedViews.contains(view)) return;

      myMovedViews.add(view);
      myDirtyAreas.add(change.getOldValue());
    }

    @Override
    public int textHeight() {
      return getFontMetrics().getHeight();
    }

    @Override
    public int textBaseLine() {
      FontMetrics fm = getFontMetrics();
      return fm.getLeading() + fm.getAscent();
    }

    @Override
    public int textWidth(String text) {
      return getFontMetrics().stringWidth(text);
    }
  }
}