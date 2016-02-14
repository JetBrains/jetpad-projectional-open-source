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
package jetbrains.jetpad.projectional.svg.toDom;

import elemental.dom.Node;
import jetbrains.jetpad.geometry.DoubleRectangle;
import jetbrains.jetpad.geometry.DoubleVector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.svg.*;

import java.util.HashMap;
import java.util.Map;

class SvgGwtPeer implements SvgPlatformPeer {
  private Map<SvgNode, Mapper<? extends SvgNode, ? extends Node>> myMappingMap = new HashMap<>();


  private void ensureSourceRegistered(SvgNode source) {
    if (!myMappingMap.containsKey(source)) {
      throw new IllegalStateException("Trying to call platform peer method of unmapped node");
    }
  }

  void registerMapper(SvgNode source, SvgNodeMapper<? extends SvgNode, ? extends Node> mapper) {
    myMappingMap.put(source, mapper);
  }

  void unregisterMapper(SvgNode source) {
    myMappingMap.remove(source);
  }

  @Override
  public double getComputedTextLength(SvgTextContent node) {
    ensureSourceRegistered((SvgNode) node);

    Node target = myMappingMap.get(node).getTarget();
    return getComputedTextLength(target);
  }

  private DoubleVector transformCoordinates(SvgLocatable relative, DoubleVector point, boolean inverse) {
    ensureSourceRegistered((SvgNode) relative);

    Node relativeTarget = myMappingMap.get(relative).getTarget();

    return transformCoordinates(relativeTarget, point.x, point.y, inverse);
  }

  private native DoubleVector transformCoordinates(Node relativeTarget, double x, double y, boolean inverse) /*-{
    matrix = relativeTarget.getTransformToElement(relativeTarget.ownerSVGElement);
    if (inverse) {
      matrix = matrix.inverse();
    }
    pt = relativeTarget.ownerSVGElement.createSVGPoint();
    pt.x = x;
    pt.y = y;
    inversePoint = pt.matrixTransform(matrix);

    return @jetbrains.jetpad.geometry.DoubleVector::new(DD)(inversePoint.x, inversePoint.y);
  }-*/;

  public DoubleVector inverseScreenTransform(SvgElement relative, DoubleVector point) {
    ensureSourceRegistered(relative);

    SvgSvgElement owner = relative.getOwnerSvgElement();
    ensureSourceRegistered(owner);

    Node ownerTarget = myMappingMap.get(owner).getTarget();
    return inverseScreenTransform(ownerTarget, point.x, point.y);
  }

  private native DoubleVector inverseScreenTransform(Node ownerTarget, double x, double y) /*-{
    matrix = ownerTarget.getScreenCTM().inverse();
    pt = ownerTarget.createSVGPoint();
    pt.x = x;
    pt.y = y;
    pt = pt.matrixTransform(matrix);
    return @jetbrains.jetpad.geometry.DoubleVector::new(DD)(pt.x, pt.y);
  }-*/;


  @Override
  public DoubleVector invertTransform(SvgLocatable relative, DoubleVector point) {
    return transformCoordinates(relative, point, true);
  }

  @Override
  public DoubleVector applyTransform(SvgLocatable relative, DoubleVector point) {
    return transformCoordinates(relative, point, false);
  }

  @Override
  public DoubleRectangle getBBox(SvgLocatable element) {
    ensureSourceRegistered((SvgNode) element);

    Node target = myMappingMap.get(element).getTarget();
    return getBoundingBox(target);
  }

  private native double getComputedTextLength(Node target) /*-{
    return target.getComputedTextLength();
  }-*/;

  private native DoubleRectangle getBoundingBox(Node target) /*-{
    bbox = target.getBBox();
    return @jetbrains.jetpad.geometry.DoubleRectangle::new(DDDD)(bbox.x, bbox.y, bbox.width, bbox.height);
  }-*/;

}