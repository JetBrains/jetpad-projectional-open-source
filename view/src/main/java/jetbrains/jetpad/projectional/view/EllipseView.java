package jetbrains.jetpad.projectional.view;

import com.google.common.collect.Range;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.values.Color;

public class EllipseView extends View {
  private static final ViewPropertySpec<Vector> CENTER = new ViewPropertySpec<>("center", ViewPropertyKind.RELAYOUT_PARENT, new Vector(0, 0));
  private static final ViewPropertySpec<Double> FROM = new ViewPropertySpec<>("from", ViewPropertyKind.RELAYOUT, 0.0);
  private static final ViewPropertySpec<Double> TO = new ViewPropertySpec<>("to", ViewPropertyKind.RELAYOUT, 2 * Math.PI);

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

  public Property<Double> from() {
    return angleProperty(FROM);
  }

  public Property<Double> to() {
    return angleProperty(TO);
  }

  private Property<Double> angleProperty(ViewPropertySpec<Double> spec) {
    final Property<Double> prop = prop(spec);

    return new Property<Double>() {
      @Override
      public String getPropExpr() {
        return prop.getPropExpr();
      }

      @Override
      public Double get() {
        return prop.get();
      }

      @Override
      public void set(Double value) {
        if (value < -2 * Math.PI || value > 2 * Math.PI) throw new IllegalArgumentException();
        prop.set(value);
      }

      @Override
      public Registration addHandler(EventHandler<? super PropertyChangeEvent<Double>> handler) {
        return prop.addHandler(handler);
      }
    };
  }

  @Override
  public boolean contains(Vector loc) {
    Vector r = radius().get();
    Vector nl = loc.sub(center().get());
    double eps = 0.001;
    if (nl.length() <= eps) return true;
    if (!(r.x * r.x * nl.x * nl.y + r.y * r.y * nl.y * nl.y <= r.x * r.x * r.y * r.y)) return false;

    double phi = Math.atan2(nl.y, nl.x);
    double from = from().get();
    double to = to().get();

    for (int i = -1 ; i <= 1; i++) {
      if (Range.closed(from + 2 * Math.PI * i, to + 2 * Math.PI * i).contains(phi)) return true;
    }

    return false;
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    ctx.bounds(new Rectangle(center().get().sub(radius().get()), radius().get().mul(2)), 0);
  }
}
