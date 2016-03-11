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
package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyBinding;
import jetbrains.jetpad.model.property.ReadableProperty;

public class HybridSynchronizer<SourceT> extends BaseHybridSynchronizer<SourceT, HybridEditorSpec<SourceT>> {
  private Property<SourceT> myWritableSource;

  public HybridSynchronizer(Mapper<?, ?> contextMapper, Property<SourceT> source, Cell target,
      HybridEditorSpec<SourceT> spec) {
    this(contextMapper, source, target, Properties.constant(spec));
  }

  public HybridSynchronizer(Mapper<?, ?> contextMapper, Property<SourceT> source, Cell target,
      ReadableProperty<HybridEditorSpec<SourceT>> spec) {
    super(contextMapper, source, target, spec, new TokenListEditor<>(spec, new ObservableArrayList<Token>(), true));
    myWritableSource = source;
  }

  @Override
  protected Registration onAttach(CollectionListener<Token> tokensListener) {
    return new CompositeRegistration(
      PropertyBinding.bindTwoWay(myWritableSource, myTokenListEditor.value),
      myTokenListEditor.tokens.addListener(tokensListener));
  }
}
