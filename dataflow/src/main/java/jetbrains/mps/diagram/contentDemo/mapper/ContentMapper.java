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
package jetbrains.mps.diagram.contentDemo.mapper;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.mps.diagram.contentDemo.model.Content;
import jetbrains.mps.diagram.contentDemo.model.ContentItem;
import jetbrains.mps.diagram.contentDemo.view.ContentDemoView;

public class ContentMapper extends Mapper<Content, ContentDemoView> {
  public ContentMapper(Content source) {
    super(source, new ContentDemoView());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperties(getSource().name, getTarget().nameView.text()));

    conf.add(Synchronizers.forObservableRole(this, getSource().items, getTarget().itemsView.children(), new MapperFactory<ContentItem, View>() {
      @Override
      public Mapper<? extends ContentItem, ? extends View> createMapper(ContentItem source) {
        return new ContentItemMapper(source);
      }
    }));
  }
}