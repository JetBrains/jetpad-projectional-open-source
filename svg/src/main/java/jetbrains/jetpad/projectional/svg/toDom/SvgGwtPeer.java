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

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.svg.SvgNode;
import jetbrains.jetpad.projectional.svg.SvgPlatformPeer;
import jetbrains.jetpad.projectional.svg.SvgTextContent;
import org.vectomatic.dom.svg.OMNode;
import org.vectomatic.dom.svg.OMSVGTextContentElement;

import java.util.HashMap;
import java.util.Map;

public class SvgGwtPeer implements SvgPlatformPeer{
  private Map<SvgNode, Mapper<? extends SvgNode, ? extends OMNode>> myMappingMap = new HashMap<>();

  void registerMapper(SvgNode source, SvgNodeMapper<? extends SvgNode, ? extends OMNode> mapper) {
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
    
    return ((OMSVGTextContentElement) myMappingMap.get(node).getTarget()).getComputedTextLength();
  }
}
