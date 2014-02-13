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
package jetbrains.jetpad.grammar.parser;

import com.google.common.base.Function;
import com.google.common.collect.Range;
import jetbrains.jetpad.grammar.*;

import java.util.*;

public class LRParser {
  private LRParserTable myTable;
  private ParserParameters myParameters;

  public LRParser(LRParserTable table) {
    this(table, ParserParameters.EMPTY);
  }

  public LRParser(LRParserTable table, ParserParameters params) {
    myTable = table;
    myParameters = params;
  }

  public boolean parse(Terminal... input) {
    List<Lexeme> lexemes = new ArrayList<>();
    for (Terminal t : input) {
      lexemes.add(new Lexeme(t, t.toString()));
    }
    return parse(lexemes) != null;
  }

  public Object parse(Lexeme... input) {
    return parse(Arrays.asList(input));
  }

  public Object parse(List<Lexeme> input) {
    return parse(input, new Function<Rule, RuleHandler>() {
      @Override
      public RuleHandler apply(Rule rule) {
        return rule.getHandler();
      }
    });
  }

  public Object parse(List<Lexeme> input, Function<Rule, RuleHandler> handlerProvider) {
    Stack<ParseStackItem> stack = new Stack<>();
    stack.push(new ParseStackItem(myTable.getInitialState(), -1, -1, null, null));
    int pos = 0;
    while (true) {
      Lexeme lexeme = pos < input.size() ? input.get(pos) : null;
      Terminal current = lexeme != null ? lexeme.getTerminal() : myTable.getGrammar().getEnd();
      LRParserState state = stack.peek().state;
      LRParserAction<LRParserState> action = state.getAction(current);
      if (action instanceof LRParserAction.Shift) {
        LRParserAction.Shift<LRParserState> shift = (LRParserAction.Shift<LRParserState>) action;
        stack.push(new ParseStackItem(shift.getState(), pos, pos + 1, current, lexeme));
        pos++;
      } else if (action instanceof LRParserAction.Reduce) {
        LRParserAction.Reduce<LRParserState> reduce = (LRParserAction.Reduce<LRParserState>) action;

        List<Object> handlerInput = new ArrayList<>();
        int startOffset = pos;
        List<Symbol> symbols = reduce.getRule().getSymbols();
        for (int i = 0; i < symbols.size(); i++) {
          ParseStackItem top = stack.pop();
          if (i == symbols.size() - 1) {
            startOffset = top.start;
          }
          handlerInput.add(top.result);
        }
        Collections.reverse(handlerInput);

        LRParserState nextState = stack.peek().state.getNextState(reduce.getRule().getHead());
        RuleContext ruleContext = new MyRuleContext(Range.closed(startOffset, pos), handlerInput);
        RuleHandler handler = handlerProvider.apply(reduce.getRule());
        Object result = handler != null ? handler.handle(ruleContext) : handlerInput;

        stack.push(new ParseStackItem(nextState, startOffset, pos, reduce.getRule().getHead(), result));
      } else if (action instanceof LRParserAction.Accept) {
        return stack.peek().result;
      } else {
        return null;
      }
    }
  }

  private class ParseStackItem {
    final LRParserState state;
    final int start;
    final int end;
    final Symbol symbol;
    final Object result;

    ParseStackItem(LRParserState state, int start, int end, Symbol symbol, Object result) {
      this.state = state;
      this.start = start;
      this.end = end;
      this.symbol = symbol;
      this.result = result;
    }
  }

  private class MyRuleContext implements RuleContext {
    private List<Object> myValues;
    private Range<Integer> myRange;

    private MyRuleContext(Range<Integer> range, List<Object> values) {
      myValues = values;
      myRange = range;
    }

    @Override
    public ParserParameters getParams() {
      return myParameters;
    }

    @Override
    public <ValueT> ValueT get(ParserParameter<ValueT> key) {
      return myParameters.get(key);
    }

    @Override
    public Object get(int index) {
      return myValues.get(index);
    }

    @Override
    public int getValueCount() {
      return myValues.size();
    }

    @Override
    public Range<Integer> getRange() {
      return myRange;
    }
  }
}