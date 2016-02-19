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

import com.google.gwt.dom.client.Node;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.mapper.gwt.DomUtil;
import jetbrains.jetpad.projectional.svg.SvgNode;

class SvgNodeMapper<SourceT extends SvgNode, TargetT extends Node> extends Mapper<SourceT, TargetT> {
  private SvgGwtPeer myPeer;

  SvgNodeMapper(SourceT source, TargetT target, SvgGwtPeer peer) {
    super(source, target);
    myPeer = peer;
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forObservableRole(this, getSource().children(), DomUtil.nodeChildren(getTarget()),
        new SvgNodeMapperFactory(myPeer)));
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);

    myPeer.registerMapper(getSource(), this);
  }

  @Override
  protected void onDetach() {
    super.onDetach();

    myPeer.unregisterMapper(getSource());
  }
}