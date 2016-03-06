package jetbrains.jetpad.projectional.view.toAwt;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

final class ImageUtil {

  static Image getScaledImage(Image image, int width, int height) {
    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = bufferedImage.createGraphics();
    graphics.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
    graphics.drawImage(image, 0, 0, width, height, null);
    return bufferedImage;
  }

  private ImageUtil() {
  }

}
