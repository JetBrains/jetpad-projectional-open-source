package jetbrains.jetpad.hybrid;

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

/**
 * Note that this class sets a never-disposing listener on
 * the {@code tokens} collection so their lifetime is always aligned.
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
    myValue = parse();
    update();
    mySourceTokens.addListener(new CollectionListener<Token>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends Token> event) {
        update();
      }

      @Override
      public void onItemSet(CollectionItemEvent<? extends Token> event) {
        update();
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends Token> event) {
        update();
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
  public Registration addHandler(final EventHandler<? super PropertyChangeEvent<ModelT>> handler) {
    if (myHandlers == null) {
      myHandlers = new Listeners<>();
    }
    return myHandlers.add(handler);
  }

  @Override
  public ObservableList<Token> getTokens() {
    return myPrettyTokens;
  }

  private void update() {
    ModelT newValue = parse();
    if (!Objects.equals(myValue, newValue))
      updateValue(newValue);

    myInUpdate = true;
    try {
      if (myValue != null) {
        PrettyPrinterContext<? super ModelT> printCtx = new PrettyPrinterContext<>(myPrinter);
        printCtx.print(myValue);
        syncTokens(printCtx.tokens());
      } else {
        syncTokens(mySourceTokens);
      }
    } finally {
      myInUpdate = false;
    }
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

  private void syncTokens(List<Token> newTokens) {
    int i;
    for (i = 0; i < newTokens.size(); i++) {
      Token p = newTokens.get(i);
      if (i < myPrettyTokens.size()) {
        if (!Objects.equals(p, myPrettyTokens.get(i))) {
          myPrettyTokens.set(i, p);
        }
      } else {
        myPrettyTokens.add(p);
      }
    }

    myPrettyTokens.subList(newTokens.size(), myPrettyTokens.size()).clear();
  }

  @Override
  public String getPropExpr() {
    return "ParsingHybridProperty["+mySourceTokens+"]";
  }

  private class MyList extends ObservableArrayList<Token> {
    @Override
    protected void afterItemAdded(int index, Token item, boolean success) {
      if (!myInUpdate && success) {
        mySourceTokens.add(index, item);
      }
    }

    @Override
    protected void afterItemSet(int index, Token oldItem, Token newItem, boolean success) {
      if (!myInUpdate && success) {
        mySourceTokens.set(index, newItem);
      }
    }

    @Override
    protected void afterItemRemoved(int index, Token item, boolean success) {
      if (!myInUpdate && success) {
        mySourceTokens.remove(index);
      }
    }
  }
}
