package jetbrains.jetpad.projectional.view.gwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.ImageData;
import jetbrains.jetpad.projectional.view.ImageView;

class ImageViewMapper extends BaseViewMapper<ImageView, Element> {
  ImageViewMapper(View2DomContext ctx, ImageView source) {
    super(ctx, source, DOM.createImg());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperty(getSource().image, new WritableProperty<ImageData>() {
      @Override
      public void set(ImageData value) {
        getTarget().setPropertyInt("width", value.getDimension().x);
        getTarget().setPropertyInt("height", value.getDimension().y);

        if (value instanceof ImageData.BinaryImageData) {
          //todo
        } else {
          getTarget().setPropertyString("src", null);
        }
      }
    }));
  }
}
