package jetbrains.jetpad.grammar.base;

import com.google.common.base.*;
import com.google.common.base.Objects;
import jetbrains.jetpad.grammar.*;
import jetbrains.jetpad.grammar.parser.LRParserAction;
import jetbrains.jetpad.grammar.parser.LRParserState;
import jetbrains.jetpad.grammar.parser.LRParserTable;

import java.util.*;

import static java.util.Collections.singleton;

public abstract class BaseLRTableGenerator<ItemT extends LRItem<ItemT>> {
  private Grammar myGrammar;
  private Map<Set<ItemT>, Set<ItemT>> myClosureCache = new HashMap<Set<ItemT>, Set<ItemT>>();

  public BaseLRTableGenerator(Grammar grammar) {
    myGrammar = grammar;
  }

  protected abstract boolean closure(Set<ItemT> result, ItemT item);

  protected abstract ItemT initialItem();

  protected abstract void addFinal(LRState<ItemT> state, ItemT item);

  protected List<LRState<ItemT>> generateStates() {
    NonTerminal initial = grammar().getStart();
    if (initial.getRules().size() != 1) {
      throw new IllegalStateException("There should be one rule from inital non terminal");
    }

    Map<Set<ItemT>, LRState<ItemT>> states = new LinkedHashMap<Set<ItemT>, LRState<ItemT>>();

    int index = 0;
    LRState<ItemT> init = new LRState<ItemT>(index++, closure(singleton(initialItem())));
    Set<LRState<ItemT>> newItems = new LinkedHashSet<LRState<ItemT>>();
    newItems.add(init);
    states.put(init.getItems(), init);

    while (!newItems.isEmpty()) {
      Set<LRState<ItemT>> items = newItems;
      newItems = new LinkedHashSet<LRState<ItemT>>();
      for (LRState<ItemT> state : items) {
        Set<ItemT> stateItems = state.getItems();
        Map<Symbol, Set<ItemT>> splitSets = splitSet(stateItems);
        for (Map.Entry<Symbol, Set<ItemT>> e : splitSets.entrySet()) {
          Symbol s = e.getKey();
          Set<ItemT> nextItems = getClosure(e.getValue());
          LRState<ItemT> targetItem = states.get(nextItems);
          if (targetItem == null) {
            targetItem = new LRState<ItemT>(index++, nextItems);
            states.put(nextItems, targetItem);
            newItems.add(targetItem);
          }
          state.addTransition(new LRTransition<ItemT>(targetItem, s));
        }

      }
    }

    for (LRState<ItemT> state : states.values()) {
      for (ItemT item : state.getItems()) {
        if (item.isFinal()) {
          addFinal(state, item);
        } else {
          Symbol s = item.getNextSymbol();
          LRState<ItemT> nextState = state.getState(s);
          if (nextState != null && s instanceof Terminal) {
            state.addRecord(s, new LRActionRecord<ItemT>(item, LRParserAction.shift(nextState)));
          }
        }
      }
    }

    return new ArrayList<LRState<ItemT>>(states.values());
  }

  private Map<Symbol, Set<ItemT>> splitSet(Set<ItemT> items) {
    Map<Symbol, Set<ItemT>> result = new HashMap<Symbol, Set<ItemT>>();
    for (ItemT item : items) {
      if (item.isFinal()) continue;

      Symbol symbol = item.getNextSymbol();
      Set<ItemT> target = result.get(symbol);
      if (target == null) {
        target = new HashSet<ItemT>();
        result.put(symbol, target);
      }
      target.add(item.getNextItem());
    }
    return result;
  }

  public LRParserTable generateTable() {
    checkGrammar();

    final List<LRState<ItemT>> states = generateStates();

    LRParserTable result = new LRParserTable(grammar());

    Map<LRState<ItemT>, LRParserState> statesMap = new HashMap<LRState<ItemT>, LRParserState>();
    statesMap.put(states.get(0), result.getInitialState());
    for (LRState<ItemT> state : states) {
      if (state == states.get(0)) continue;
      statesMap.put(state, result.newState(state.getName()));
    }

    for (LRState<ItemT> state : states) {
      LRParserState lrState = statesMap.get(state);

      for (LRTransition<ItemT> trans : state.getTransitions()) {
        if (trans.getSymbol() instanceof NonTerminal) {
          NonTerminal nt = (NonTerminal) trans.getSymbol();
          lrState.addNextState(nt, statesMap.get(trans.getTarget()));
        }
      }

      for (Symbol s : grammar().getSymbols()) {
        if (!(s instanceof Terminal)) continue;

        Terminal t = (Terminal) s;

        if (!state.hasRecords(s)) continue;

        LRActionRecord<ItemT> rec = state.getRecord(t);

        LRParserAction<LRParserState> action;
        if (rec.getAction() instanceof LRParserAction.Shift<?>) {
          LRParserAction.Shift<LRState<ItemT>> shift = (LRParserAction.Shift<LRState<ItemT>>) rec.getAction();
          action = LRParserAction.shift(statesMap.get(shift.getState()));
        } else if (rec.getAction() instanceof LRParserAction.Reduce<?>) {
          LRParserAction.Reduce<LRState<ItemT>> reduce = (LRParserAction.Reduce<LRState<ItemT>>) rec.getAction();
          action = LRParserAction.reduce(reduce.getRule());
        } else if (rec.getAction() instanceof LRParserAction.Accept<?>) {
          action = LRParserAction.accept();
        } else if (rec.getAction() instanceof LRParserAction.Error<?>) {
          action = LRParserAction.error();
        } else {
          throw new IllegalStateException();
        }

        lrState.addAction(t, action);
      }
    }

    return result;
  }

  private Set<ItemT> getClosure(Set<ItemT> items) {
    Set<ItemT> result = myClosureCache.get(items);
    if (result != null) {
      return result;
    }
    result = closure(items);
    myClosureCache.put(items, result);
    return result;
  }

  private Set<ItemT> closure(Set<ItemT> items) {
    Set<ItemT> result = new LinkedHashSet<ItemT>();
    result.addAll(items);
    boolean hasChanges = true;
    while (hasChanges) {
      hasChanges = false;
      for (ItemT item : new ArrayList<ItemT>(result)) {
        if (closure(result, item)) {
          hasChanges = true;
        }
      }
    }
    return result;
  }

  protected Grammar grammar() {
    return myGrammar;
  }

  private void checkGrammar() {
    NonTerminal start = myGrammar.getStart();
    if (start.getRules().size() != 1) throw new IllegalArgumentException();
    Rule firstRule = start.getRules().iterator().next();
    if (firstRule.getSymbols().size() != 1) throw new IllegalArgumentException();
    if (!(firstRule.getSymbols().get(0) instanceof NonTerminal)) throw new IllegalArgumentException();
  }

  public void dumpTable() {
    List<LRState<ItemT>> states = generateStates();
    System.out.println("Table : \n");
    for (LRState<ItemT> state : states) {
      System.out.println(state);
      System.out.println("Transitions:");
      for (LRTransition<ItemT> t : state.getTransitions()) {
        System.out.println(t);
      }

      for (Terminal t : grammar().getTerminals()) {
        Set<LRActionRecord<ItemT>> records = state.getMergedRecords(t);
        if (records.isEmpty()) continue;

        StringBuilder text = new StringBuilder();
        text.append("on ").append(t).append(" ");

        if (!state.hasAmbiguity(t)) {
          text.append(toString(records.iterator().next().getAction()));
        } else {
          List<String> actions = new ArrayList<String>();
          for (LRActionRecord<ItemT> rec : records) {
            actions.add(toString(rec.getAction()));
          }
          text.append(actions).append("  !CONFLICT!  ");
        }
        System.out.println(text);
      }

      System.out.println("");
    }
  }

  private String toString(LRParserAction<LRState<ItemT>> action) {
    if (action instanceof LRParserAction.Shift) {
      return "shift " + ((LRParserAction.Shift<LRState<ItemT>>) action).getState().getName();
    }
    return action.toString();
  }
}
