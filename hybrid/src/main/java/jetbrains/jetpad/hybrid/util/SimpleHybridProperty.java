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
package jetbrains.jetpad.hybrid.util;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.hybrid.HybridProperty;
import jetbrains.jetpad.hybrid.parser.Parser;
import jetbrains.jetpad.hybrid.parser.ParsingContext;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.BaseDerivedProperty;

public class SimpleHybridProperty<ModelT> extends BaseDerivedProperty<ModelT> implements HybridProperty<ModelT> {

  private final Parser<ModelT> myParser;
  private final ObservableList<Token> myTokens;

  private Registration myRegistration;

  public SimpleHybridProperty(Parser<ModelT> parser, ObservableList<Token> tokens) {
    super(parser.parse(new ParsingContext(tokens)));
    myParser = parser;
    myTokens = tokens;
  }

  @Override
  protected void doAddListeners() {
    myRegistration = myTokens.addHandler(new EventHandler<CollectionItemEvent<? extends Token>>() {
      @Override
      public void onEvent(CollectionItemEvent<? extends Token> event) {
        somethingChanged();
      }
    });
  }

  @Override
  protected void doRemoveListeners() {
    myRegistration.remove();
  }

  @Override
  protected ModelT doGet() {
    return myParser.parse(new ParsingContext(myTokens));
  }

  @Override
  public ObservableList<Token> getSource() {
    return myTokens;
  }
}
