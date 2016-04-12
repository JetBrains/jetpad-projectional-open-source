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

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Registration;
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

public class PrettyHybridProperty<SourceT, ModelT> implements HybridProperty<ModelT> {
  private final ObservableList<Token> myTokens = new MyList();
  private ModelT myValue;

  private final ObservableList<SourceT> mySource;
  private final IndexedTransform<Token, SourceT> myFrom;
  private final Handler<Integer> myRemoveHandler;
  private final Handler<Runnable> myUpdateHandler;
  private final Parser<? extends ModelT> myParser;
  private final PrettyPrinter<? super ModelT> myPrinter;
  private final ParsingContextFactory myParsingContextFactory;

  private boolean myInSync = false;
  private Listeners<EventHandler<? super PropertyChangeEvent<ModelT>>> myHandlers;

  public PrettyHybridProperty(
    ObservableList<SourceT> source,
    final IndexedTransform<SourceT, Token> to,
    IndexedTransform<Token, SourceT> from,
    Handler<Integer> removeHandler,
    Handler<Runnable> updateHandler,
    Parser<? extends ModelT> parser,
    PrettyPrinter<? super ModelT> printer,
    ParsingContextFactory parsingContextFactory) {
    mySource = source;
    myFrom = from;
    myRemoveHandler = removeHandler;
    myUpdateHandler = updateHandler;
    myParser = parser;
    myPrinter = printer;
    myParsingContextFactory = parsingContextFactory;

    inSync(new Runnable() {
      @Override
      public void run() {
        for (int i  = 0; i < mySource.size(); i++) {
          myTokens.add(to.apply(i, mySource.get(i)));
        }
      }
    });
    myValue = parse();

    mySource.addListener(new CollectionListener<SourceT>() {
      @Override
      public void onItemAdded(final CollectionItemEvent<? extends SourceT> event) {
        inSync(new Runnable() {
          @Override
          public void run() {
            myTokens.add(event.getIndex(), to.apply(event.getIndex(), event.getNewItem()));
          }
        });
      }

      @Override
      public void onItemSet(final CollectionItemEvent<? extends SourceT> event) {
        inSync(new Runnable() {
          @Override
          public void run() {
            myTokens.set(event.getIndex(), to.apply(event.getIndex(), event.getNewItem()));
          }
        });
      }

      @Override
      public void onItemRemoved(final CollectionItemEvent<? extends SourceT> event) {
        inSync(new Runnable() {
          @Override
          public void run() {
            myRemoveHandler.handle(event.getIndex());
            myTokens.remove(event.getIndex());
          }
        });
      }
    });
  }

  @Override
  public ModelT get() {
    return myValue;
  }

  @Override
  public ObservableList<Token> getTokens() {
    return myTokens;
  }

  @Override
  public String getPropExpr() {
    return "PrettyHybridProperty[" + myTokens + "]";
  }

  @Override
  public Registration addHandler(final EventHandler<? super PropertyChangeEvent<ModelT>> handler) {
    if (myHandlers == null) {
      myHandlers = new Listeners<>();
    }
    return myHandlers.add(handler);
  }

  private ModelT parse() {
    return myParser.parse(myParsingContextFactory.getParsingContext(myTokens));
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

  private void inSync(Runnable r) {
    if (!myInSync) {
      myInSync = true;
      try {
        r.run();
      } finally {
        myInSync = false;
      }
    }
  }

  private void sourceAdd(final int index, final Token item) {
    myUpdateHandler.handle(new Runnable() {
      @Override
      public void run() {
        mySource.add(myFrom.apply(index, item));
      }
    });
  }

  private void sourceSet(final int index, final Token newItem) {
    myUpdateHandler.handle(new Runnable() {
      @Override
      public void run() {
        mySource.set(index, myFrom.apply(index, newItem));
      }
    });
  }

  private void sourceRemove(final int index) {
    myUpdateHandler.handle(new Runnable() {
      @Override
      public void run() {
        mySource.remove(index);
      }
    });
  }

  public interface IndexedTransform <SourceT, TargetT> {
    TargetT apply(int index, SourceT source);
  }

  private class MyList extends ObservableArrayList<Token> {
    @Override
    protected void afterItemAdded(final int index, final Token item, boolean success) {
      if (success) {
        updateValue(parse());
        inSync(new Runnable() {
          @Override
          public void run() {
            if (myValue != null) {
              updateFromPretty(index);
            } else {
              sourceAdd(index, item);
            }
          }
        });
      }
    }

    @Override
    protected void afterItemSet(final int index, Token oldItem, final Token newItem, boolean success) {
      if (success) {
        updateValue(parse());
        inSync(new Runnable() {
          @Override
          public void run() {
            if (myValue != null) {
              updateFromPretty(index);
            } else {
              sourceSet(index, newItem);
            }
          }
        });
      }
    }

    @Override
    protected void afterItemRemoved(final int index, Token item, boolean success) {
      if (success) {
        updateValue(parse());
        inSync(new Runnable() {
          @Override
          public void run() {
            myRemoveHandler.handle(index);
            if (myValue != null) {
              updateFromPretty(index);
            } else {
              sourceRemove(index);
            }
          }
        });
      }
    }

    private void updateFromPretty(final int forceToSource) {
      PrettyPrinterContext<? super ModelT> printCtx = new PrettyPrinterContext<>(myPrinter);
      printCtx.print(myValue);
      List<Token> prettyTokens = printCtx.tokens();
      boolean forceUpdated = false;
      for (int i = 0; i < prettyTokens.size(); i++) {
        final Token p = prettyTokens.get(i);
        if (i < myTokens.size() && i < mySource.size()) {
          // Token exists in both source and tokens.
          if (!Objects.equals(p, myTokens.get(i))) {
            myTokens.set(i, p);
            if (i == forceToSource) {
              forceUpdated = true;
            }
            sourceSet(i, p);
          }
        } else if (i < myTokens.size() && i >= mySource.size()) {
          // Token just appeared in tokens and doesn't exists in source.
          if (!Objects.equals(p, myTokens.get(i))) {
            myTokens.set(i, p);
          }
          if (i == forceToSource) {
            forceUpdated = true;
          }
          sourceAdd(i, p);
        } else {
          throw new IllegalStateException("A printer generated more tokens than where is source.");
        }
      }

      if (!forceUpdated) {
        if (forceToSource < myTokens.size()) {
          sourceSet(forceToSource, myTokens.get(forceToSource));
        } else if (forceToSource >= myTokens.size()) {
          sourceRemove(forceToSource);
        }
      }
    }
  }
}
