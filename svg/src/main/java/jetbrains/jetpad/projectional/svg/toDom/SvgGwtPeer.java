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

class SvgGwtPeer implements SvgPlatformPeer{
  private Map<SvgNode, Mapper<? extends SvgNode, ? extends OMNode>> myMappingMap = new HashMap<>();

  private void ensureElementConsistency(SvgNode source, OMNode target) {
    if (source instanceof SvgElement && !(target instanceof OMSVGElement)) {
      throw new IllegalStateException("Target of SvgElement must be OMSVGElement");
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
    ensureTextContentConsistency(source, target);
    ensureTransformableConsistency(source, target);
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
    if (!myMappingMap.containsKey(node)) {
      throw new IllegalStateException("Trying to getCompudedTextLength of umapped node");
    }

    OMNode target = myMappingMap.get(node).getTarget();
    return ((OMSVGTextContentElement) target).getComputedTextLength();
  }

  @Override
  public DoubleVector invertTransform(SvgTransformable relative, DoubleVector point) {
    if (!myMappingMap.containsKey(relative)) {
      throw new IllegalStateException("Trying to invertTransform of unmapped relative element");
    }

    OMNode relativeTarget = myMappingMap.get(relative).getTarget();
    OMSVGMatrix inverseMatrix = ((ISVGTransformable) relativeTarget)
        .getTransformToElement(((OMSVGElement) relativeTarget).getOwnerSVGElement()).inverse();
    OMSVGPoint pt = ((OMSVGElement) relativeTarget).getOwnerSVGElement().createSVGPoint((float) point.x, (float) point.y);
    OMSVGPoint inversePt = pt.matrixTransform(inverseMatrix);
    return new DoubleVector(inversePt.getX(), inversePt.getY());
  }

  @Override
  public DoubleRectangle getBBox(SvgTransformable element) {
    if (!myMappingMap.containsKey(element)) {
      throw new IllegalStateException("Trying to getBBox of unmapped element");
    }

    OMNode target = myMappingMap.get(element).getTarget();
    OMSVGRect bBox = ((ISVGLocatable) target).getBBox();
    return new DoubleRectangle(bBox.getX(), bBox.getY(), bBox.getWidth(), bBox.getHeight());
  }
}
