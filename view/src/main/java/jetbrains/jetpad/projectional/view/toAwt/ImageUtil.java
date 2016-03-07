package jetbrains.jetpad.projectional.view.toAwt;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

final class ImageUtil {

  static Image getScaledImage(BufferedImage originalImage, int width, int height) {
    BufferedImage resultImage = new BufferedImage(width, height, originalImage.getType());
    Graphics2D graphics = resultImage.createGraphics();
    try {
      graphics.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
      graphics.drawImage(originalImage, 0, 0, width, height, null);
    } finally {
      graphics.dispose();
    }
    return resultImage;
  }

  private ImageUtil() {
  }

}
