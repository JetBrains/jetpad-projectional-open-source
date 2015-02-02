/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.mps.diagram.contentDemo.mapper;

import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.mps.diagram.contentDemo.model.Content;
import jetbrains.mps.diagram.dataflow.mapper.BlockMapper;
import jetbrains.mps.diagram.dataflow.model.Block;

public class BlockWithContentMapper extends BlockMapper {
  private Content myContent;
  private ContentRootMapper.ContentProperties myProperties;

  public BlockWithContentMapper(Block source, Content content, ContentRootMapper.ContentProperties properties) {
    super(source);
    myContent = content;
    myProperties = properties;
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forConstantRole(this, myContent, getTarget().contentView.children(), new MapperFactory<Content, View>() {
      @Override
      public Mapper<? extends Content, ? extends View> createMapper(Content source) {
        return new ContentMapper(source);
      }
    }));

    conf.add(Synchronizers.forProperty(myProperties.x, new Runnable() {
      @Override
      public void run() {
        getTarget().minimalSize().set(new Vector(myProperties.x.get(), getTarget().minimalSize().get().y));
      }
    }));
    conf.add(Synchronizers.forProperty(myProperties.y, new Runnable() {
      @Override
      public void run() {
        getTarget().minimalSize().set(new Vector(getTarget().minimalSize().get().x, myProperties.y.get()));
      }
    }));
  }
}