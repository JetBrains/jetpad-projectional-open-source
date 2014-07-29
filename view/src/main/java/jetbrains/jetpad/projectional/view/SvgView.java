package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.svg.SvgRoot;

public class SvgView extends View {
  public final Property<SvgRoot> svgRoot = new ValueProperty<>();

  public SvgView(SvgRoot root) {
    svgRoot.set(root);
  }
}
