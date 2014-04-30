/*
 * Copyright 2012-2014 JetBrains s.r.o
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