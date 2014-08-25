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
package jetbrains.jetpad.projectional.view.toAwt;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.animation.Animation;
import jetbrains.jetpad.base.animation.Animations;
import jetbrains.jetpad.base.base64.Base64Coder;
import jetbrains.jetpad.base.edt.AwtEventDispatchThread;
import jetbrains.jetpad.base.edt.EventDispatchThread;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.event.awt.EventTranslator;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.base.ImageData;
import jetbrains.jetpad.projectional.svg.SvgSvgElement;
import jetbrains.jetpad.projectional.svg.toAwt.SvgRootDocumentMapper;
import jetbrains.jetpad.projectional.view.*;
import jetbrains.jetpad.projectional.view.spi.NullViewContainerPeer;
import jetbrains.jetpad.projectional.view.spi.ViewContainerPeer;
import jetbrains.jetpad.values.Color;
import jetbrains.jetpad.values.Font;
import jetbrains.jetpad.values.FontFamily;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.event.AWTEventDispatcher;
import org.apache.batik.gvt.event.EventDispatcher;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static jetbrains.jetpad.projectional.view.toAwt.AwtConverters.toAwtColor;

public class ViewContainerComponent extends JComponent implements Scrollable {
  private static final String MONOSPACED_FONT = java.awt.Font.MONOSPACED;

  private static String toFontName(FontFamily fontFamily) {
    if (fontFamily == FontFamily.MONOSPACED) {
      return MONOSPACED_FONT;
    } else {
      return fontFamily.toString();
    }
  }

  static final Color SELECTION_COLOR = Color.DARK_BLUE;

  private ViewContainer myContainer;
  private Registration myContainerReg = Registration.EMPTY;

  private Set<View> myMovedViews = new LinkedHashSet<>();
  private Set<jetbrains.jetpad.geometry.Rectangle> myDirtyAreas = new LinkedHashSet<>();
  private Set<View> myDirtyViews = new LinkedHashSet<>();
  private boolean myCaretVisible;
  private boolean myFocused;
  private Timer myTimer;
  private long myLastActionTime;
  private Set<TextView> myWithCaretVisible = new HashSet<>();
  private MyViewContainerPeer myPeer = new MyViewContainerPeer();

  private Map<View, PaintHelper> myViewPaintHelpers = new HashMap<>();

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
      public void mouseEntered(MouseEvent e) {
        handleMouseEvent(e, new Handler<jetbrains.jetpad.event.MouseEvent>() {
          @Override
          public void handle(jetbrains.jetpad.event.MouseEvent e) {
            myContainer.mouseEntered(e);
          }
        }, false);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        handleMouseEvent(e, new Handler<jetbrains.jetpad.event.MouseEvent>() {
          @Override
          public void handle(jetbrains.jetpad.event.MouseEvent e) {
            myContainer.mouseLeft(e);
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
              Vector offset = scrollView.offset().get().sub(new Vector(0, getDefaultFontMetrics().getHeight() * e.getWheelRotation()));
              offset = offset.min(Vector.ZERO);
              offset = offset.max(scrollView.internalsBounds().sub(scrollView.maxDimension().get()).max(Vector.ZERO).negate());
              scrollView.offset().set(offset);
              e.consume();
              return;
            }
          }
          current = current.parent().get();
        }

        // hack: since java 1.7 mousewheel events aren't bubbling so we have to use this to fix that
        // otherwise we won't be able to scroll with the mouse wheel.
        if (getParent() != null) {
          getParent().dispatchEvent(e);
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
            if (e.is(KeyStrokeSpecs.PASTE)) {
              if (cb.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                try {
                  String text = (String) cb.getData(DataFlavor.stringFlavor);
                  myContainer.paste(new PasteEvent(new TextClipboardContent(text)));
                  e.consume();
                  return;
                } catch (UnsupportedFlavorException | IOException ex) {
                  throw new RuntimeException(ex);
                }
              }
            }

            if (e.is(KeyStrokeSpecs.COPY) || e.is(KeyStrokeSpecs.CUT)) {
              CopyCutEvent event;
              if (e.is(KeyStrokeSpecs.CUT)) {
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

  private void paint(Graphics graphics, View view) {
    if (!view.visible().get()) return;
    Graphics2D g = (Graphics2D) graphics;
    java.awt.Rectangle clip = g.getClipBounds();
    jetbrains.jetpad.geometry.Rectangle bounds = view.bounds().get();
    if (clip != null) {
      jetbrains.jetpad.geometry.Rectangle rect = new jetbrains.jetpad.geometry.Rectangle(clip.x, clip.y, clip.width, clip.height);

      if (!rect.intersects(bounds)) return;
    }
    g.clipRect(bounds.origin.x, bounds.origin.y, bounds.dimension.x, bounds.dimension.y);


    paintContent(view, (Graphics2D) g.create());

    for (View child : view.children()) {
      paint(g.create(), child);
    }
  }

  private void paintContent(final View view, final Graphics2D g) {
    final jetbrains.jetpad.geometry.Rectangle bounds = view.bounds().get();

    Color background = view.background().get();
    if (!(view instanceof EllipseView) && background != null) {
      g.setColor(toAwtColor(background));
      g.fillRect(bounds.origin.x, bounds.origin.y, bounds.dimension.x, bounds.dimension.y);
    }
    final Color border = view.border().get();
    if (border != null) {
      g.setColor(toAwtColor(border));
      g.drawRect(bounds.origin.x, bounds.origin.y, bounds.dimension.x - 1, bounds.dimension.y - 1);
    }

    if (view instanceof EllipseView) {
      EllipseView ellipseView = (EllipseView) view;
      g.setColor(toAwtColor(ellipseView.background().get()));

      final double from = (ellipseView.from().get() * 360) / (2 * Math.PI);
      final double to = (ellipseView.to().get() * 360) / (2 * Math.PI);

      int borderWidth = ellipseView.borderWidth().get();
      final Vector borderVec = new Vector(borderWidth / 2, borderWidth / 2);

      final jetbrains.jetpad.geometry.Rectangle innerBounds = new jetbrains.jetpad.geometry.Rectangle(bounds.origin.add(borderVec), bounds.dimension.sub(borderVec.mul(2)));

      g.fill(new Arc2D.Double(innerBounds.origin.x, innerBounds.origin.y, innerBounds.dimension.x - 1, innerBounds.dimension.y - 1, from, to - from, Arc2D.PIE));

      if (borderWidth > 0) {
        g.setColor(toAwtColor(ellipseView.borderColor().get()));
        withStroke(g, new BasicStroke(borderWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER), new Runnable() {
          @Override
          public void run() {
            final jetbrains.jetpad.geometry.Rectangle borderBounds = innerBounds;
            g.draw(new Arc2D.Double(borderBounds.origin.x, borderBounds.origin.y, borderBounds.dimension.x - 1, borderBounds.dimension.y - 1, from, to - from, Arc2D.PIE));
          }
        });
      }
    }

    if (view instanceof LineView) {
      LineView lineView = (LineView) view;
      g.setColor(toAwtColor(lineView.color().get()));
      g.setStroke(new BasicStroke(lineView.width().get()));
      Vector start = lineView.start().get();
      Vector end = lineView.end().get();
      g.drawLine(start.x, start.y, end.x, end.y);
    }

    if (view instanceof TextView) {
      TextView textView = (TextView) view;
      String text = textView.text().get();
      Vector origin = bounds.origin;

      java.awt.Font font = new java.awt.Font(toFontName(textView.fontFamily().get()), java.awt.Font.PLAIN, textView.fontSize().get());
      if (textView.bold().get()) {
        font = font.deriveFont(java.awt.Font.BOLD, font.getSize());
      }
      if (textView.italic().get()) {
        font = font.deriveFont(java.awt.Font.ITALIC, font.getSize());
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

        g.setColor(toAwtColor(Color.WHITE));
        g.drawString(text.substring(left, right), origin.x + xLeft, origin.y + textView.baseLine());
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
      g.setStroke(new BasicStroke(multiPoint.width().get()));

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

    if (view instanceof ImageView) {
      ImageView imageView = (ImageView) view;
      ImageData imageData = imageView.image.get();

      if (imageData instanceof ImageData.EmptyImageData) {
        //ignore
      } else if (imageData instanceof ImageData.BinaryImageData || imageData instanceof ImageData.UrlImageData) {
        BufferedImage image;
        try {
          if (imageData instanceof ImageData.BinaryImageData) {
            ImageData.BinaryImageData data = (ImageData.BinaryImageData) imageData;
            image = ImageIO.read(new ByteArrayInputStream(data.getData()));
          } else {
            String url = ((ImageData.UrlImageData) imageData).getUrl();
            String pngPrefix = "data:image/png;base64,";
            String jpgPrefix = "data:image/jpeg;base64,";
            if (url.startsWith(pngPrefix) || url.startsWith(jpgPrefix)) {
              String base64;
              if (url.startsWith(pngPrefix)) {
                base64 = url.substring(pngPrefix.length());
              } else {
                base64 = url.substring(jpgPrefix.length());
              }
              byte[] data = Base64Coder.decodeBytes(base64);
              image = ImageIO.read(new ByteArrayInputStream(data));
            } else {
              image = ImageIO.read(new URL(url));
            }
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        g.drawImage(image, bounds.origin.x, bounds.origin.y, new ImageObserver() {
          @Override
          public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            return true;
          }
        });
      } else {
        throw new UnsupportedOperationException("Unsupported Image : " + imageData);
      }
    }

    if (view instanceof SvgView) {
      if (myViewPaintHelpers.containsKey(view)) {
        myViewPaintHelpers.get(view).paint(view, (Graphics2D) g.create());
      } else {
        SvgPaintHelper helper = new SvgPaintHelper((SvgView) view);
        myViewPaintHelpers.put(view, helper);

        helper.paint((SvgView) view, (Graphics2D) g.create());
      }
    }
  }

  private void paintScroller(Graphics2D g, ScrollView scrollView, boolean vertical) {
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

  private FontMetrics getFontMetrics(Font font) {
    int style = java.awt.Font.PLAIN;
    if (font.isBold()) {
      style |= java.awt.Font.BOLD;
    }
    if (font.isItalic()) {
      style |= java.awt.Font.ITALIC;
    }
    return Toolkit.getDefaultToolkit().getFontMetrics(new java.awt.Font(toFontName(font.getFamily()), style, font.getSize()));
  }

  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  @Override
  public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
    return getDefaultFontMetrics().getHeight();
  }

  private FontMetrics getDefaultFontMetrics() {
    return getFontMetrics(TextView.DEFAULT_FONT);
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

  private void withStroke(Graphics2D g, Stroke s, Runnable r) {
    Stroke oldStroke = g.getStroke();
    g.setStroke(s);
    try {
      r.run();
    } finally {
      g.setStroke(oldStroke);
    }
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
    public void scrollTo(jetbrains.jetpad.geometry.Rectangle rect, View view) {
      myContainer.root().validate();

      jetbrains.jetpad.geometry.Rectangle viewBounds = view.bounds().get();

      if (!viewBounds.contains(rect.add(viewBounds.origin))) {
        throw new IllegalArgumentException();
      }

      jetbrains.jetpad.geometry.Rectangle bounds = new jetbrains.jetpad.geometry.Rectangle(viewBounds.origin.add(rect.origin), rect.dimension);

      if (bounds.origin.x + bounds.dimension.x < getVisibleRect().width) {
        bounds = new jetbrains.jetpad.geometry.Rectangle(0, bounds.origin.y, bounds.origin.x + bounds.dimension.x, bounds.dimension.y);
      }

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
    public int textHeight(Font font) {
      return getFontMetrics(font).getHeight();
    }

    @Override
    public int textBaseLine(Font font) {
      FontMetrics fm = getFontMetrics(font);
      return fm.getLeading() + fm.getAscent();
    }

    @Override
    public int textWidth(Font font, String text) {
      return getFontMetrics(font).stringWidth(text);
    }

    @Override
    public void requestFocus() {
      ViewContainerComponent.this.requestFocus();
    }

    @Override
    public EventDispatchThread getEdt() {
      return AwtEventDispatchThread.INSTANCE;
    }

    @Override
    public Animation fadeIn(View view, int duration) {
      return Animations.finishedAnimation();
    }

    @Override
    public Animation fadeOut(View view, int duration) {
      return Animations.finishedAnimation();
    }

    @Override
    public Animation showSlide(View view, int duration) {
      return Animations.finishedAnimation();
    }

    @Override
    public Animation hideSlide(View view, int duration) {
      return Animations.finishedAnimation();
    }

    @Override
    public Object getMappedTo(View view) {
      return null;
    }
  }

  private static interface PaintHelper<ViewT extends View> {
    void paint(ViewT view, Graphics2D g);
    void update(ViewT view);
    void dispose();
  }

  private class SvgPaintHelper implements PaintHelper<SvgView> {
    private GraphicsNode myGraphicsNode;
    private SvgRootDocumentMapper myMapper;
    private UserAgent myUserAgent;
    private BridgeContext myBridgeContext;


    SvgPaintHelper(final SvgView view) {
      myUserAgent = new UserAgentAdapter() {
        AWTEventDispatcher dispatcher = new AWTEventDispatcher();

        @Override
        public EventDispatcher getEventDispatcher() {
          return dispatcher;
        }
      };

      ViewContainerComponent.this.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          e.translatePoint(-view.bounds().get().origin.x, -view.bounds().get().origin.y);
          myUserAgent.getEventDispatcher().dispatchEvent(e);
          e.translatePoint(view.bounds().get().origin.x, view.bounds().get().origin.y);
        }
      });

      createGraphicsNode(view);
      view.root().addHandler(new EventHandler<PropertyChangeEvent<SvgSvgElement>>() {
        @Override
        public void onEvent(PropertyChangeEvent<SvgSvgElement> event) {
          SvgPaintHelper.this.update(view);
        }
      });
    }

    private void createGraphicsNode(SvgView view) {
      myBridgeContext = new BridgeContext(myUserAgent);
      myBridgeContext.setDynamic(true);

      myMapper = new SvgRootDocumentMapper(view.root().get());
      myMapper.attachRoot();

      GVTBuilder builder = new GVTBuilder();
      myGraphicsNode = builder.build(myBridgeContext, myMapper.getTarget());

      myUserAgent.getEventDispatcher().setRootNode(myGraphicsNode);
    }

    @Override
    public void paint(SvgView view, Graphics2D g) {
      g.translate(view.bounds().get().origin.x, view.bounds().get().origin.y);
      myGraphicsNode.paint(g);
    }

    @Override
    public void update(SvgView view) {
      dispose();
      createGraphicsNode(view);
      view.invalidate();
    }

    @Override
    public void dispose() {
      myMapper.detachRoot();
      myBridgeContext.dispose();
      myGraphicsNode = null;
    }
  }
}