package jetbrains.jetpad.cell.toView;

import jetbrains.jetpad.cell.ImageCell;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.view.ImageView;

class ImageCellMapper extends BaseCellMapper<ImageCell, ImageView> {
  ImageCellMapper(ImageCell source, CellToViewContext ctx) {
    super(source, new ImageView(), ctx);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);
    conf.add(Synchronizers.forProperty(getSource().image, getTarget().image));
  }
}
