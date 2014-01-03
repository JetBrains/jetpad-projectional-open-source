package jetbrains.jetpad.projectional.view.util;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewPropertyKind;
import jetbrains.jetpad.projectional.view.ViewPropertySpec;

import java.util.Arrays;

public class RelativePositionerView extends View {
  private final ViewPropertySpec<Vector> RELATIVE_TO = new ViewPropertySpec<Vector>("relativeTo", ViewPropertyKind.RELAYOUT, Vector.ZERO);
  private final ViewPropertySpec<HorizontalAnchor> HORIZONTAL_ANCHOR = new ViewPropertySpec<HorizontalAnchor>("horizontalAnchor", ViewPropertyKind.RELAYOUT, HorizontalAnchor.LEFT);
  private final ViewPropertySpec<VerticalAnchor> VERTICAL_ANCHOR = new ViewPropertySpec<VerticalAnchor>("verticalAnchor", ViewPropertyKind.RELAYOUT, VerticalAnchor.BASELINE);

  public RelativePositionerView(View... views) {
    children().addAll(Arrays.asList(views));
  }

  public Property<Vector> relativeTo() {
    return toParentOffsetProp(RELATIVE_TO);
  }

  public Property<HorizontalAnchor> horizontalAnchor() {
    return prop(HORIZONTAL_ANCHOR);
  }

  public Property<VerticalAnchor> verticalAnchor() {
    return prop(VERTICAL_ANCHOR);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);

    Vector relativeTo = relativeTo().get();
    HorizontalAnchor hAnchor = horizontalAnchor().get();
    VerticalAnchor vAnchor = verticalAnchor().get();

    for (View child : children()) {
      Rectangle childBounds = child.bounds().get();
      int x = 0;
      int y = 0;

      if (hAnchor == HorizontalAnchor.LEFT) {
        x = 0;
      } else if (hAnchor == HorizontalAnchor.RIGHT) {
        x = -childBounds.dimension.x;
      } else if (hAnchor == HorizontalAnchor.CENTER) {
        x = -childBounds.dimension.x / 2;
      }

      if (vAnchor == VerticalAnchor.BASELINE) {
        y = child.baseLine();
      } else if (vAnchor == VerticalAnchor.TOP) {
        y = 0;
      } else if (vAnchor == VerticalAnchor.BOTTOM) {
        y = childBounds.dimension.y;
      } else if (vAnchor == VerticalAnchor.CENTER) {
        y = childBounds.dimension.y / 2;
      }

      child.moveTo(relativeTo.add(new Vector(x, y)));
    }

    Rectangle bounds = null;
    for (View child  : children()) {
      if (bounds == null) {
        bounds = child.bounds().get();
      } else {
        bounds = bounds.union(child.bounds().get());
      }
    }

    ctx.bounds(bounds != null ? bounds : new Rectangle(Vector.ZERO, Vector.ZERO), 0);
  }

  public static enum HorizontalAnchor {
    LEFT, RIGHT, CENTER
  }

  public static enum VerticalAnchor {
    TOP, BOTTOM, BASELINE, CENTER
  }
}
