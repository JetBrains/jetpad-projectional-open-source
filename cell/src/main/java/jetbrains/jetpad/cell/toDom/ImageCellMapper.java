/*
 * Copyright 2012-2016 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    conf.add(Synchronizers.forPropsOneWay(getSource().image, value -> {
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
    }));
  }
}