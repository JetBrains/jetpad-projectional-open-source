package jetbrains.jetpad.grammar.base;

import jetbrains.jetpad.grammar.*;
import jetbrains.jetpad.grammar.parser.LRParserAction;
import jetbrains.jetpad.grammar.parser.LRParserState;
import jetbrains.jetpad.grammar.parser.LRParserTable;

import java.util.*;

import static java.util.Collections.singleton;

public abstract class BaseLRTableGenerator<ItemT extends LRItem<ItemT>> {
  private Grammar myGrammar;
  private Map<Set<ItemT>, Set<ItemT>> myClosureCache = new HashMap<>();

  public BaseLRTableGenerator(Grammar grammar) {
    myGrammar = grammar;
  }

  protected abstract Set<ItemT> closure(Set<ItemT> result, ItemT item);

  protected abstract ItemT initialItem();

  protected abstract void addFinal(LRState<ItemT> state, ItemT item);

  protected List<LRState<ItemT>> generateStates() {
    NonTerminal initial = grammar().getStart();
    if (initial.getRules().size() != 1) {
      throw new IllegalStateException("There should be one rule from inital non terminal");
    }

    Map<Set<ItemT>, LRState<ItemT>> states = new LinkedHashMap<>();

    int index = 0;
    LRState<ItemT> init = new LRState<>(index++, closure(singleton(initialItem())));
    Set<LRState<ItemT>> newItems = new LinkedHashSet<>();
    newItems.add(init);
    states.put(init.getItems(), init);

    while (!newItems.isEmpty()) {
      Set<LRState<ItemT>> items = newItems;
      newItems = new LinkedHashSet<>();
      for (LRState<ItemT> state : items) {
        for (Symbol s : grammar().getSymbols()) {
          Set<ItemT> nextSet = nextSet(state.getItems(), s);
          if (nextSet.isEmpty()) continue;
          LRState<ItemT> targetItem = states.get(nextSet);
          if (targetItem == null) {
            targetItem = new LRState<>(index++, nextSet);
            states.put(nextSet, targetItem);
            newItems.add(targetItem);
          }
          state.addTransition(new LRTransition<>(targetItem, s));
        }
      }
    }

    Rule firstRule = grammar().getStart().getFirstRule();
    for (LRState<ItemT> state : states.values()) {
      for (ItemT item : state.getItems()) {
        if (item.isFinal()) {
          addFinal(state, item);
        } else {
          Symbol s = item.getNextSymbol();
          LRState<ItemT> nextState = state.getState(s);
          if (nextState != null && s instanceof Terminal) {
            state.addRecord((Symbol) s, new LRActionRecord<>(item, LRParserAction.shift(nextState)));
          }
        }
      }
    }

    return new ArrayList<>(states.values());
  }

  public LRParserTable generateTable() {
    checkGrammar();

    final List<LRState<ItemT>> states = generateStates();

    LRParserTable result = new LRParserTable(grammar());

    Map<LRState<ItemT>, LRParserState> statesMap = new HashMap<>();
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

      Map<Terminal, Set<LRParserAction<LRParserState>>> actions = new LinkedHashMap<>();
      for (Terminal s : grammar().getTerminals()) {
        actions.put(s, new LinkedHashSet<LRParserAction<LRParserState>>());
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

  private Set<ItemT> nextSet(Set<ItemT> source, Symbol s) {
    Set<ItemT> newSet = new LinkedHashSet<>();
    for (ItemT item : source) {
      if (item.getNextSymbol() == s) {
        newSet.add(item.getNextItem());
      }
    }
    return getClosure(newSet);
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
    Set<ItemT> result = new LinkedHashSet<>();
    result.addAll(items);
    boolean hasChanges = true;
    while (hasChanges) {
      Set<ItemT> toAdd = new LinkedHashSet<>();
      for (ItemT item : result) {
        toAdd.addAll(closure(result, item));
      }
      hasChanges = result.addAll(toAdd);
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
          List<String> actions = new ArrayList<>();
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
