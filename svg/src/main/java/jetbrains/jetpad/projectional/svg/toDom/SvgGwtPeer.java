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

import jetbrains.jetpad.geometry.DoubleRectangle;
import jetbrains.jetpad.geometry.DoubleVector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.svg.*;
import org.vectomatic.dom.svg.*;
import org.vectomatic.dom.svg.itf.ISVGLocatable;
import org.vectomatic.dom.svg.itf.ISVGTransformable;

import java.util.HashMap;
import java.util.Map;

class SvgGwtPeer implements SvgPlatformPeer {
  private Map<SvgNode, Mapper<? extends SvgNode, ? extends OMNode>> myMappingMap = new HashMap<>();

  private void ensureElementConsistency(SvgNode source, OMNode target) {
    if (source instanceof SvgElement && !(target instanceof OMSVGElement)) {
      throw new IllegalStateException("Target of SvgElement must be OMSVGElement");
    }
  }

  private void ensureLocatableConsistency(SvgNode source, OMNode target) {
    if (source instanceof SvgLocatable && !(target instanceof ISVGLocatable)) {
      throw new IllegalStateException("Target of SvgLocatable must be ISVGLocatable");
    }
  }

  private void ensureTextContentConsistency(SvgNode source, OMNode target) {
    if (source instanceof SvgTextContent && !(target instanceof OMSVGTextContentElement)) {
      throw new IllegalStateException("Target of SvgTextContent must be OMSVGTextContentElement");
    }
  }

  private void ensureTransformableConsistency(SvgNode source, OMNode target) {
    if (source instanceof SvgTransformable && !(target instanceof ISVGTransformable)) {
      throw new IllegalStateException("Target of SvgTransformable must be ISVGTransformable");
    }
  }

  private void ensureSourceTargetConsistency(SvgNode source, OMNode target) {
    ensureElementConsistency(source, target);
    ensureLocatableConsistency(source, target);
    ensureTextContentConsistency(source, target);
    ensureTransformableConsistency(source, target);
  }

  private void ensureSourceRegistered(SvgNode source) {
    if (!myMappingMap.containsKey(source)) {
      throw new IllegalStateException("Trying to call platform peer method of unmapped node");
    }
  }

  void registerMapper(SvgNode source, SvgNodeMapper<? extends SvgNode, ? extends OMNode> mapper) {
    ensureSourceTargetConsistency(source, mapper.getTarget());
    myMappingMap.put(source, mapper);
  }

  void unregisterMapper(SvgNode source) {
    myMappingMap.remove(source);
  }

  @Override
  public double getComputedTextLength(SvgTextContent node) {
    ensureSourceRegistered((SvgNode) node);

    OMNode target = myMappingMap.get(node).getTarget();
    return ((OMSVGTextContentElement) target).getComputedTextLength();
  }

  private DoubleVector transformCoordinates(SvgLocatable relative, DoubleVector point, boolean inverse) {
    ensureSourceRegistered((SvgNode) relative);

    OMNode relativeTarget = myMappingMap.get(relative).getTarget();
    OMSVGMatrix matrix = ((ISVGLocatable) relativeTarget)
        .getTransformToElement(((OMSVGElement) relativeTarget).getOwnerSVGElement());
    if (inverse) {
      matrix = matrix.inverse();
    }
    OMSVGPoint pt = ((OMSVGElement) relativeTarget).getOwnerSVGElement().createSVGPoint((float) point.x, (float) point.y);
    OMSVGPoint inversePt = pt.matrixTransform(matrix);
    return new DoubleVector(inversePt.getX(), inversePt.getY());
  }

  @Override
  public DoubleVector invertTransform(SvgLocatable relative, DoubleVector point) {
    return transformCoordinates(relative, point, true);
  }

  @Override
  public DoubleVector applyTransform(SvgLocatable relative, DoubleVector point) {
    return transformCoordinates(relative, point, false);
  }

  public DoubleVector inverseScreenTransform(SvgElement relative, DoubleVector point) {
    ensureSourceRegistered(relative);

    SvgSvgElement owner = relative.getOwnerSvgElement();
    ensureSourceRegistered(owner);

    OMNode ownerTarget = myMappingMap.get(owner).getTarget();
    OMSVGMatrix matrix = ((ISVGLocatable) ownerTarget).getScreenCTM().inverse();
    OMSVGPoint pt = ((OMSVGSVGElement) ownerTarget).createSVGPoint((float) point.x, (float) point.y);
    pt = pt.matrixTransform(matrix);

    return new DoubleVector(pt.getX(), pt.getY());
  }

  @Override
  public DoubleRectangle getBBox(SvgLocatable element) {
    ensureSourceRegistered((SvgNode) element);

    OMNode target = myMappingMap.get(element).getTarget();
    OMSVGRect bBox = ((ISVGLocatable) target).getBBox();
    return new DoubleRectangle(bBox.getX(), bBox.getY(), bBox.getWidth(), bBox.getHeight());
  }
}