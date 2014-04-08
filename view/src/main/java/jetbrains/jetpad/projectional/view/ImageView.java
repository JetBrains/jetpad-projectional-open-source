package jetbrains.jetpad.projectional.view;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;

public class ImageView extends View {
  public static final ViewPropertySpec<ImageData> IMAGE = new ViewPropertySpec<>("image", ViewPropertyKind.RELAYOUT_AND_REPAINT, ImageData.emptyImage(new Vector(100, 100)));

  public final Property<ImageData> image = prop(IMAGE);

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    ImageData imageData = image.get();
    ctx.bounds(imageData.getDimension(), 0);
  }
}
