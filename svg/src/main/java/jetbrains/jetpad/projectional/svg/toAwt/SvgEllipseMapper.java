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
package jetbrains.jetpad.projectional.svg.toAwt;

import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.SvgEllipse;
import jetbrains.jetpad.values.Color;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGOMEllipseElement;

public class SvgEllipseMapper extends SvgElementMapper<SvgEllipse, SVGOMEllipseElement> {
  public SvgEllipseMapper(SvgEllipse source, SVGOMEllipseElement target, AbstractDocument doc) {
    super(source, target, doc);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forPropsOneWay(getSource().getProp(SvgEllipse.CX), new WritableProperty<Double>() {
      @Override
      public void set(Double value) {
        getTarget().setAttribute("cx", Double.toString(value));
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().getProp(SvgEllipse.CY), new WritableProperty<Double>() {
      @Override
      public void set(Double value) {
        getTarget().setAttribute("cy", Double.toString(value));
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().getProp(SvgEllipse.RX), new WritableProperty<Double>() {
      @Override
      public void set(Double value) {
        getTarget().setAttribute("rx", Double.toString(value));
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().getProp(SvgEllipse.RY), new WritableProperty<Double>() {
      @Override
      public void set(Double value) {
        getTarget().setAttribute("ry", Double.toString(value));
      }
    }));
    conf.add(Synchronizers.forPropsOneWay(getSource().getProp(SvgEllipse.FILL), new WritableProperty<Color>() {
      @Override
      public void set(Color value) {
        getTarget().setAttribute("fill", value.toCssColor());
      }
    }));
  }
}
