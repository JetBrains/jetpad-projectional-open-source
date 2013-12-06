/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.view.gwt;

import com.google.gwt.dom.client.Style;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.View;
import org.vectomatic.dom.svg.OMSVGSVGElement;

class GwtViewSynchronizers {
  static Synchronizer svgBoundsSyncrhonizer(View view, final OMSVGSVGElement svg) {
    return Synchronizers.forProperty(view.bounds(), new WritableProperty<Rectangle>() {
      @Override
      public void set(Rectangle value) {
        svg.setWidth(Style.Unit.PX, value.dimension.x + 1);
        svg.setHeight(Style.Unit.PX, value.dimension.y + 1);
      }
    });
  }
}