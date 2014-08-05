package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.svg.*;

public class SvgView extends View {
  public static final ViewPropertySpec<SvgRoot> SVG_ROOT = new ViewPropertySpec<>("svgRoot", ViewPropertyKind.RELAYOUT);

  public final SvgContainer svgContainer;

  public SvgView(SvgRoot root) {
    root().set(root);
    svgContainer = new SvgContainer(root);
    root().addHandler(new EventHandler<PropertyChangeEvent<SvgRoot>>() {
      @Override
      public void onEvent(PropertyChangeEvent<SvgRoot> event) {
        svgContainer.root().set(event.getNewValue());
        invalidate();
      }
    });
    svgContainer.addListener(new SvgContainerAdapter() {
      @Override
      public void onPropertySet(SvgElement element, SvgPropertySpec<?> spec, PropertyChangeEvent<?> event) {
        repaint();
      }

      @Override
      public void onElementAttached(SvgElement element) {
        repaint();
      }

      @Override
      public void onElementDetached(SvgElement element) {
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
    .build());
  }

  public Property<SvgRoot> root() {
    return getProp(SVG_ROOT);
  }

  private MouseEvent translateMouseEvent(MouseEvent e) {
    return new MouseEvent(e.x() - bounds().get().origin.x, e.y() - bounds().get().origin.y);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    Vector bounds = new Vector((int) Math.ceil(root().get().getProp(SvgRoot.WIDTH).get()),
        (int) Math.ceil(root().get().getProp(SvgRoot.HEIGHT).get()));
    ctx.bounds(bounds, baseLine());
  }
}
