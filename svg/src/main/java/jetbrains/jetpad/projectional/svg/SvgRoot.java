package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

public class SvgRoot extends SvgElement {
  public final Property<Integer> height = new ValueProperty<>(100);
  public final Property<Integer> width = new ValueProperty<>(100);
}
