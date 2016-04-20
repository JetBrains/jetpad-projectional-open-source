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
import jetbrains.jetpad.hybrid.parser.CommentToken;
import jetbrains.jetpad.hybrid.parser.Parser;
import jetbrains.jetpad.hybrid.parser.ParsingContextFactory;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinterContext;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

import java.util.List;
import java.util.Objects;

/**
 * Makes a {@link HybridProperty} from a list of tokens and
 * a parsing/printing methods.
 *
 * Note that this class sets a never-disposing listener on
 * the {@code tokens} collection so their lifetimes are always aligned.
 */
public class ParsingHybridProperty<ModelT> implements HybridProperty<ModelT> {

  private final Parser<? extends ModelT> myParser;
  private final PrettyPrinter<? super ModelT> myPrinter;
  private final ObservableList<Token> mySourceTokens;
  private final ParsingContextFactory myParsingContextFactory;
  private final ObservableList<Token> myPrettyTokens = new MyList();

  private Listeners<EventHandler<? super PropertyChangeEvent<ModelT>>> myHandlers;
  private ModelT myValue;
  private boolean myInUpdate;

  public ParsingHybridProperty(
      Parser<? extends ModelT> parser,
      PrettyPrinter<? super ModelT> printer,
      ObservableList<Token> tokens, ParsingContextFactory parsingContextFactory) {
    myParser = parser;
    myPrinter = printer;
    mySourceTokens = tokens;
    myParsingContextFactory = parsingContextFactory;
    initUpdate();
    myValue = parse();
    update();
    mySourceTokens.addListener(new CollectionListener<Token>() {
      @Override
      public void onItemAdded(final CollectionItemEvent<? extends Token> event) {
        if (event.getNewItem() instanceof CommentToken) {
          executeInUpdate(new Runnable() {
            @Override
            public void run() {
              myPrettyTokens.add(event.getIndex(), event.getNewItem());
            }
          });
        } else {
          update();
        }
      }

      @Override
      public void onItemSet(final CollectionItemEvent<? extends Token> event) {
        if (event.getNewItem() instanceof CommentToken) {
          executeInUpdate(new Runnable() {
            @Override
            public void run() {
              myPrettyTokens.set(event.getIndex(), event.getNewItem());
            }
          });
          if (!(event.getOldItem() instanceof CommentToken)) {
            update();
          }
        } else if (event.getOldItem() instanceof CommentToken) {
          myPrettyTokens.set(event.getIndex(), event.getNewItem());
          update();
        } else {
          update();
        }
      }

      @Override
      public void onItemRemoved(final CollectionItemEvent<? extends Token> event) {
        if (event.getOldItem() instanceof CommentToken) {
          executeInUpdate(new Runnable() {
            @Override
            public void run() {
              myPrettyTokens.remove(event.getIndex());
            }
          });
        } else {
          update();
        }
      }
    });
  }

  @Override
  public final ModelT get() {
    return myValue;
  }

  private ModelT parse() {
    return myParser.parse(myParsingContextFactory.getParsingContext(mySourceTokens));
  }

  @Override
  public Registration addHandler(EventHandler<? super PropertyChangeEvent<ModelT>> handler) {
    if (myHandlers == null) {
      myHandlers = new Listeners<>();
    }
    return myHandlers.add(handler);
  }

  @Override
  public ObservableList<Token> getTokens() {
    return myPrettyTokens;
  }

  private void initUpdate() {
    executeInUpdate(new Runnable() {
      @Override
      public void run() {
        updatePrettyTokens(mySourceTokens);
      }
    });
  }

  private void update() {
    executeInUpdate(new Runnable() {
      @Override
      public void run() {
        ModelT newValue = parse();
        myInUpdate = true;
        try {
          if (newValue != null) {
            PrettyPrinterContext<? super ModelT> printCtx = new PrettyPrinterContext<>(myPrinter);
            printCtx.print(newValue);
            updatePrettyTokens(printCtx.tokens());
          } else {
            myPrettyTokens.clear();
            myPrettyTokens.addAll(mySourceTokens);
          }
        } finally {
          myInUpdate = false;
        }
        if (!Objects.equals(myValue, newValue)) {
          updateValue(newValue);
        }
      }
    });
  }

  private void updateValue(ModelT newValue) {
    final PropertyChangeEvent<ModelT> event = new PropertyChangeEvent<>(myValue, newValue);
    myValue = newValue;

    if (myHandlers != null) {
      myHandlers.fire(new ListenerCaller<EventHandler<? super PropertyChangeEvent<ModelT>>>() {
        @Override
        public void call(EventHandler<? super PropertyChangeEvent<ModelT>> item) {
          item.onEvent(event);
        }
      });
    }
  }

  private void updatePrettyTokens(List<Token> newTokens) {
    int i;
    for (i = 0; i < newTokens.size(); i++) {
      Token p = newTokens.get(i);
      if (i < myPrettyTokens.size()) {
        if (myPrettyTokens.get(i) instanceof CommentToken) {
          myPrettyTokens.add(i, p);
        } else if (!Objects.equals(p, myPrettyTokens.get(i))) {
          myPrettyTokens.set(i, p);
        }
      } else {
        myPrettyTokens.add(p);
      }
    }
    while (newTokens.size() < myPrettyTokens.size() && !(myPrettyTokens.get(newTokens.size()) instanceof CommentToken)) {
      myPrettyTokens.remove(newTokens.size());
    }
  }

  @Override
  public String getPropExpr() {
    return "ParsingHybridProperty[" + mySourceTokens + "]";
  }

  private class MyList extends ObservableArrayList<Token> {
    @Override
    protected void afterItemAdded(final int index, final Token item, boolean success) {
      executeInUpdate(new Runnable() {
        @Override
        public void run() {
          mySourceTokens.add(index, item);
        }
      });
      if (!(item instanceof CommentToken)) {
        update();
      }
    }

    @Override
    protected void afterItemSet(final int index, Token oldItem, final Token newItem, boolean success) {
      executeInUpdate(new Runnable() {
        @Override
        public void run() {
          mySourceTokens.set(index, newItem);
        }
      });
      if (!(oldItem instanceof CommentToken && newItem instanceof CommentToken)) {
        update();
      }
    }

    @Override
    protected void afterItemRemoved(final int index, Token item, boolean success) {
      executeInUpdate(new Runnable() {
        @Override
        public void run() {
          mySourceTokens.remove(index);
        }
      });
      if (!(item instanceof CommentToken)) {
        update();
      }
    }
  }

  private void executeInUpdate(Runnable body) {
    if (!myInUpdate) {
      myInUpdate = true;
      try {
        body.run();
      } finally {
        myInUpdate = false;
      }
    }
  }
}
