package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.svg.*;
import jetbrains.jetpad.projectional.svg.event.SvgAttributeEvent;

public class SvgView extends View {
  public static final ViewPropertySpec<SvgSvgElement> SVG_ROOT = new ViewPropertySpec<>("svgRoot", ViewPropertyKind.RELAYOUT);

  public final SvgContainer svgContainer;

  public SvgView(SvgSvgElement root) {
    root().set(root);
    svgContainer = new SvgContainer(root);
    root().addHandler(new EventHandler<PropertyChangeEvent<SvgSvgElement>>() {
      @Override
      public void onEvent(PropertyChangeEvent<SvgSvgElement> event) {
        svgContainer.root().set(event.getNewValue());
        invalidate();
      }
    });
    svgContainer.addListener(new SvgContainerAdapter() {
      @Override
      public void onAttrSet(SvgNode node, SvgAttributeEvent event) {
        if (node == root().get() &&
            (event.getAttrSpec().toString().equalsIgnoreCase("height") || (event.getAttrSpec().toString().equalsIgnoreCase("width")))){
          invalidate();
        }
        repaint();
      }

      @Override
      public void onElementAttached(SvgNode node) {
        repaint();
      }

      @Override
      public void onElementDetached(SvgNode node) {
        repaint();
      }
    });

    addTrait(new ViewTraitBuilder().on(ViewEvents.MOUSE_PRESSED, new ViewEventHandler<MouseEvent>() {
      @Override
      public void handle(View view, MouseEvent e) {
        MouseEvent translatedEvent = translateMouseEvent(e);
        svgContainer.mousePressed(translatedEvent);
        if (translatedEvent.isConsumed()) {
          e.consume();
        }
      }
    })
    .on(ViewEvents.MOUSE_RELEASED, new ViewEventHandler<MouseEvent>() {
      @Override
      public void handle(View view, MouseEvent e) {
        MouseEvent translatedEvent = translateMouseEvent(e);
        svgContainer.mouseReleased(translatedEvent);
        if (translatedEvent.isConsumed()) {
          e.consume();
        }
      }
    })
    .on(ViewEvents.KEY_PRESSED, new ViewEventHandler<KeyEvent>() {
      @Override
      public void handle(View view, KeyEvent e) {
        svgContainer.keyPressed(e);
      }
    })
    .on(ViewEvents.KEY_RELEASED, new ViewEventHandler<KeyEvent>() {
      @Override
      public void handle(View view, KeyEvent e) {
        svgContainer.keyReleased(e);
      }
    })
    .on(ViewEvents.KEY_TYPED, new ViewEventHandler<KeyEvent>() {
      @Override
      public void handle(View view, KeyEvent e) {
        svgContainer.keyTyped(e);
      }
    })
    .build());
  }

  public Property<SvgSvgElement> root() {
    return getProp(SVG_ROOT);
  }

  private MouseEvent translateMouseEvent(MouseEvent e) {
    return new MouseEvent(e.x() - bounds().get().origin.x, e.y() - bounds().get().origin.y);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    Vector bounds = new Vector((int) Math.ceil(root().get().getWidth().get()),
        (int) Math.ceil(root().get().getHeight().get()));
    ctx.bounds(bounds, baseLine());
  }
}
