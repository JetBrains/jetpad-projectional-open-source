package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.values.Color;

public class EllipseView extends View {
  private static final ViewPropertySpec<Vector> CENTER = new ViewPropertySpec<>("center", ViewPropertyKind.RELAYOUT_PARENT, new Vector(0, 0));
  public static final ViewPropertySpec<Vector> RADIUS = new ViewPropertySpec<>("dimension", ViewPropertyKind.RELAYOUT, new Vector(10, 10));

  public EllipseView() {
    background().set(Color.BLACK);
  }

  public Property<Vector> radius() {
    return prop(RADIUS);
  }

  public Property<Vector> center() {
    return toParentOffsetProp(CENTER);
  }

  @Override
  protected boolean contains(Vector loc) {
    Vector r = radius().get();
    Vector nl = loc.sub(center().get());
    return r.x * r.x * nl.x * nl.y + r.y * r.y * nl.y * nl.y <= r.x * r.x * r.y * r.y;
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    ctx.bounds(new Rectangle(center().get().sub(radius().get()), radius().get().mul(2)), 0);
  }
}
