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
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.projectional.svg.SvgSvgElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;

public class SvgRootDocumentMapper extends Mapper<SvgSvgElement, OMSVGSVGElement> {
  private SvgElementMapper<SvgSvgElement, OMSVGSVGElement> myRootMapper;
  private SvgGwtPeer myPeer;

  public SvgRootDocumentMapper(SvgSvgElement source, OMSVGSVGElement target) {
    super(source, target);
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);

    if (!getSource().isAttached()) {
      throw new IllegalStateException("Element must be attached");
    }
    myPeer = new SvgGwtPeer();
    getSource().container().setPeer(myPeer);

    myRootMapper = new SvgElementMapper<>(getSource(), getTarget(), myPeer);
    myRootMapper.attachRoot();
  }

  @Override
  protected void onDetach() {
    myRootMapper.detachRoot();
    myRootMapper = null;

    if (getSource().isAttached()) {
      getSource().container().setPeer(null);
    }
    myPeer = null;

    super.onDetach();
  }
}
