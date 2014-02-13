package jetbrains.jetpad.grammar.base;

import jetbrains.jetpad.grammar.*;
import jetbrains.jetpad.grammar.parser.LRParserAction;
import jetbrains.jetpad.grammar.parser.LRParserState;
import jetbrains.jetpad.grammar.parser.LRParserTable;

import java.util.*;

import static java.util.Collections.singleton;

public abstract class BaseLRTableGenerator<ItemT extends LRItem<ItemT>> {
  private Grammar myGrammar;

  public BaseLRTableGenerator(Grammar grammar) {
    myGrammar = grammar;
  }

  protected abstract List<LRState<ItemT>> generateStates();

  protected abstract Set<ItemT> closure(ItemT item);

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

        if (state.hasAmbiguity(t)) {
          throw new IllegalStateException("There's ambiguity. Can't generate table");
        }
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

  protected Set<ItemT> nextSet(Set<ItemT> source, Symbol s) {
    Set<ItemT> newSet = new LinkedHashSet<>();
    for (ItemT item : source) {
      if (item.getNextSymbol() == s) {
        newSet.add(item.getNextItem());
      }
    }
    return closure(newSet);
  }

  protected Set<ItemT> closure(Set<ItemT> items) {
    Set<ItemT> result = new LinkedHashSet<>();
    result.addAll(items);
    boolean hasChanges = true;
    while (hasChanges) {
      Set<ItemT> toAdd = new LinkedHashSet<>();
      for (ItemT item : result) {
        toAdd.addAll(closure(item));
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
