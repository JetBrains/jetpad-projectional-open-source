/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.cell.hybrid;

import com.google.common.base.Objects;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.parser.ParsingContext;
import jetbrains.jetpad.projectional.parser.Token;
import jetbrains.jetpad.projectional.parser.ValueToken;
import jetbrains.jetpad.projectional.parser.prettyprint.PrettyPrinterContext;
import jetbrains.jetpad.projectional.parser.prettyprint.ParseNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TokenListEditor<SourceT> {
  private Property<Boolean> myValid = new ValueProperty<Boolean>(true);
  private ParseNode myParseNode;
  private HybridPositionSpec<SourceT> mySpec;
  private boolean mySyncing;
  private List<Token> myPrintedTokens;
  private boolean myRestoringState;
  private Registration myChangeReg = Registration.EMPTY;

  final ObservableList<Token> tokens = new ObservableArrayList<Token>();
  final Property<SourceT> value = new ValueProperty<SourceT>();
  final ReadableProperty<Boolean> valid = myValid;

  TokenListEditor(HybridPositionSpec<SourceT> spec) {
    mySpec = spec;

    tokens.addHandler(new EventHandler<CollectionItemEvent<Token>>() {
      @Override
      public void onEvent(CollectionItemEvent<Token> event) {
        sync(new Runnable() {
          @Override
          public void run() {
            reparse();
          }
        });
      }
    });
    value.addHandler(new EventHandler<PropertyChangeEvent<SourceT>>() {
      @Override
      public void onEvent(PropertyChangeEvent<SourceT> event) {
        sync(new Runnable() {
          @Override
          public void run() {
            update();
          }
        });
      }
    });
  }

  ParseNode parseNode() {
    return myParseNode;
  }

  List<Object> objects() {
    if (myParseNode == null) return Collections.emptyList();
    List<Object> result = new ArrayList<Object>();
    toObjects(myParseNode, result);
    return result;
  }

  private void toObjects(ParseNode node, List<Object> result) {
    for (ParseNode child : node.children()) {
      if (child.value() instanceof Token) {
        result.add(node.value());
      } else {
        toObjects(child, result);
      }
    }
  }

  private void sync(Runnable r) {
    if (mySyncing) return;
    mySyncing = true;
    try {
      r.run();
    } finally {
      mySyncing = false;
    }
  }

  private void reparse() {
    if (myRestoringState) return;

    if (tokens.size() == 0) {
      value.set(null);
      myValid.set(true);
      myParseNode = null;
      myPrintedTokens = new ArrayList<Token>();
      myChangeReg.remove();
      myChangeReg = Registration.EMPTY;
    } else {
      SourceT result = mySpec.getParser().parse(new ParsingContext(tokens));
      if (result != null) {
        value.set(result);
        myValid.set(true);
        reprint();
        if (myPrintedTokens.size() != tokens.size()) {
          throw new IllegalStateException();
        }
      } else {
        myValid.set(false);
        myParseNode = null;
        myPrintedTokens = null;
      }
    }
  }

  private void update() {
    PrettyPrinterContext ctx = reprint();
    myValid.set(true);
    tokens.clear();
    tokens.addAll(ctx.tokens());
  }

  private PrettyPrinterContext reprint() {
    PrettyPrinterContext ctx = new PrettyPrinterContext(mySpec.getPrettyPrinter());
    ctx.print(value.get());
    myParseNode = ctx.result();
    myPrintedTokens = ctx.tokens();

    myChangeReg.remove();
    myChangeReg = ctx.changeSource().addHandler(new EventHandler() {
      @Override
      public void onEvent(Object event) {
        sync(new Runnable() {
          @Override
          public void run() {
            update();
          }
        });
      }
    });
    return ctx;
  }

  void updateToPrintedTokens() {
    if (myPrintedTokens == null) return;

    List<Token> newTokens = myPrintedTokens;
    for (int i = 0; i < newTokens.size(); i++) {
      Token newToken = newTokens.get(i);
      Token token = tokens.get(i);
      if (newToken instanceof ValueToken) continue;
      if (!Objects.equal(newToken, token)) {
        tokens.set(i, newToken);
      }
    }
  }

  void restoreState(List<Token> state) {
    if (myRestoringState) throw new IllegalStateException();
    myRestoringState = true;
    try {
      if (state != null) {
        tokens.clear();
        tokens.addAll(state);
      } else if (!myValid.get()) {
        update();
      }
    } finally {
      myRestoringState = false;
    }
    if (state != null) {
      reparse();
    }
  }

}