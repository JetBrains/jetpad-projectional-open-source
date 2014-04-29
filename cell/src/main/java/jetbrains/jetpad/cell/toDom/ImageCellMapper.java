package jetbrains.jetpad.cell.toDom;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.cell.ImageCell;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.base.ImageData;

class ImageCellMapper extends BaseCellMapper<ImageCell> {
  ImageCellMapper(ImageCell source, CellToDomContext ctx) {
    super(source, ctx, DOM.createImg());

    getTarget().getStyle().setVerticalAlign(Style.VerticalAlign.BOTTOM);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperty(getSource().image, new WritableProperty<ImageData>() {
      @Override
      public void set(ImageData value) {
        if (value != null) {
          getTarget().setAttribute("width", "" + value.getDimension().x);
          getTarget().setAttribute("height", "" + value.getDimension().y);
        } else {
          getTarget().removeAttribute("width");
          getTarget().removeAttribute("height");
        }

        if (value instanceof ImageData.UrlImageData) {
          ImageData.UrlImageData data = (ImageData.UrlImageData) value;
          getTarget().setAttribute("src", data.getUrl());
        }
      }
    }));
  }
}
