package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.values.Color;

public class SvgEllipse extends SvgElement {
  public static final SvgPropertySpec<Double> CX = new SvgPropertySpec<>("cx", 0.0);
  public static final SvgPropertySpec<Double> CY = new SvgPropertySpec<>("cy", 0.0);
  public static final SvgPropertySpec<Double> RX = new SvgPropertySpec<>("rx", 0.0);
  public static final SvgPropertySpec<Double> RY = new SvgPropertySpec<>("ry", 0.0);
  public static final SvgPropertySpec<Color> FILL = new SvgPropertySpec<>("fill", Color.BLACK);
}
