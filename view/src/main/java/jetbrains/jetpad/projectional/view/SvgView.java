package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.svg.SvgRoot;

public class SvgView extends View {
  public static final ViewPropertySpec<SvgRoot> SVG_ROOT = new ViewPropertySpec<>("svgRoot", ViewPropertyKind.RELAYOUT);

  public SvgView(SvgRoot root) {
    root().set(root);
  }

  public Property<SvgRoot> root() {
    return getProp(SVG_ROOT);
  }

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    Vector bounds = new Vector((int) Math.ceil(root().get().width.get()), (int) Math.ceil(root().get().height.get()));
    ctx.bounds(bounds, baseLine());
  }
}
