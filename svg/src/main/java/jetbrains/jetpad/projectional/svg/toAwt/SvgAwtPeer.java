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
package jetbrains.jetpad.projectional.svg.toAwt;

import jetbrains.jetpad.geometry.DoubleRectangle;
import jetbrains.jetpad.geometry.DoubleVector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.svg.*;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.svg.SVGOMPoint;
import org.apache.batik.dom.svg.SVGOMTextContentElement;
import org.w3c.dom.Node;
import org.w3c.dom.svg.*;

import java.util.HashMap;
import java.util.Map;

class SvgAwtPeer implements SvgPlatformPeer {
  private Map<SvgNode, Mapper<? extends SvgNode, ? extends Node>> myMappingMap = new HashMap<>();

  private void ensureElementConsistency(SvgNode source, Node target) {
    if (source instanceof SvgElement && !(target instanceof SVGOMElement)) {
      throw new IllegalStateException("Target of SvgElement must be SVGOMElement");
    }
  }

  private void ensureLocatableConsistency(SvgNode source, Node target) {
    if (source instanceof SvgLocatable && !(target instanceof SVGLocatable)) {
      throw new IllegalStateException("Target of SvgLocatable must be SVGLocatable");
    }
  }

  private void ensureTextContentConsistency(SvgNode source, Node target) {
    if (source instanceof SvgTextContent && !(target instanceof SVGOMTextContentElement)) {
      throw new IllegalStateException("Target of SvgTextContent must be SVGOMTextContentElement");
    }
  }

  private void ensureTransformableConsistency(SvgNode source, Node target) {
    if (source instanceof SvgTransformable && !(target instanceof SVGTransformable)) {
      throw new IllegalStateException("Target of SvgTransformable must be SVGTransformable");
    }
  }

  private void ensureSourceTargetConsistency(SvgNode source, Node target) {
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

  void registerMapper(SvgNode source, SvgNodeMapper<? extends SvgNode, ? extends Node> mapper) {
    ensureSourceTargetConsistency(source, mapper.getTarget());
    myMappingMap.put(source, mapper);
  }

  void unregisterMapper(SvgNode source) {
    myMappingMap.remove(source);
  }

  @Override
  public double getComputedTextLength(SvgTextContent node) {
    ensureSourceRegistered((SvgNode) node);

    Node target = myMappingMap.get(node).getTarget();
    return ((SVGOMTextContentElement) target).getComputedTextLength();
  }

  private DoubleVector transformCoordinates(SvgLocatable relative, DoubleVector point, boolean inverse) {
    ensureSourceRegistered((SvgNode) relative);

    Node relativeTarget = myMappingMap.get(relative).getTarget();
    SVGMatrix matrix = ((SVGLocatable) relativeTarget).
        getTransformToElement(((SVGOMElement) relativeTarget).getOwnerSVGElement());
    if (inverse) {
      matrix = matrix.inverse();
    }
    SVGPoint pt = new SVGOMPoint((float) point.x, (float) point.y);
    SVGPoint inversePt = pt.matrixTransform(matrix);
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

  @Override
  public DoubleRectangle getBBox(SvgLocatable element) {
    ensureSourceRegistered((SvgNode) element);

    Node target = myMappingMap.get(element).getTarget();
    SVGRect bBox = ((SVGLocatable) target).getBBox();
    return new DoubleRectangle(bBox.getX(), bBox.getY(), bBox.getWidth(), bBox.getHeight());
  }
}