package jetbrains.jetpad.cell;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.base.ImageData;

public class ImageCell extends Cell {
  public static final CellPropertySpec<ImageData> IMAGE = new CellPropertySpec<>("image", ImageData.emptyImage(Vector.ZERO));

  public final Property<ImageData> image = getProp(IMAGE);
}
