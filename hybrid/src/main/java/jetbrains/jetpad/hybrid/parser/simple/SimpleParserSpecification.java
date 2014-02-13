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
package jetbrains.jetpad.hybrid.parser.simple;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.grammar.*;
import jetbrains.jetpad.grammar.lr1.LR1TableGenerator;
import jetbrains.jetpad.grammar.parser.LRParser;
import jetbrains.jetpad.grammar.parser.LRParserTable;
import jetbrains.jetpad.grammar.parser.Lexeme;
import jetbrains.jetpad.grammar.slr.SLRTableGenerator;
import jetbrains.jetpad.hybrid.parser.*;

import java.util.*;

public class SimpleParserSpecification<ExprT> {
  private Grammar myGrammar =  new Grammar();
  private NonTerminal myExpr = myGrammar.newNonTerminal("E");
  private Map<Token, Terminal> myTokenToTerminal = new HashMap<>();

  private Terminal myId = myGrammar.newTerminal("id");
  private Terminal myIntNumber =  myGrammar.newTerminal("int");
  private Terminal myBool = myGrammar.newTerminal("bool");
  private Terminal myError = myGrammar.newTerminal("error");
  private Map<Predicate<Object>, Terminal> myValueTerminals = new HashMap<>();

  public SimpleParserSpecification() {
    myGrammar.newRule(myGrammar.getStart(), myExpr);
  }

  public SimpleParserSpecification<ExprT> addBinaryOperator(Token token, final BinaryExpressionFactory<ExprT> factory, int priority, boolean leftAssoc) {
    Terminal term = getOrDeclareTerminal(token);
    myGrammar.newRule(myExpr, myExpr, term, myExpr).setHandler(new RuleHandler() {
      @Override
      public Object handle(RuleContext ctx) {
        return factory.create(ctx.getParams(), (ExprT) ctx.get(0), (ExprT) ctx.get(2));
      }
    }).setPriority(priority).setAssociativity(leftAssoc ? Associativity.LEFT : Associativity.RIGHT);
    return this;
  }

  public SimpleParserSpecification<ExprT> addPrefix(Token token, final UnaryExpressionFactory<ExprT> factory, int priority) {
    Terminal term = getOrDeclareTerminal(token);
    myGrammar.newRule(myExpr, term, myExpr).setHandler(new RuleHandler() {
      @Override
      public Object handle(RuleContext ctx) {
        return factory.create(ctx.getParams(), (ExprT) ctx.get(1));
      }
    }).setPriority(priority);
    return this;
  }

  public SimpleParserSpecification<ExprT> addSuffix(Token token, final UnaryExpressionFactory<ExprT> factory, int priority) {
    Terminal term = getOrDeclareTerminal(token);
    myGrammar.newRule(myExpr, myExpr, term).setHandler(new RuleHandler() {
      @Override
      public Object handle(RuleContext ctx) {
        return factory.create(ctx.getParams(), (ExprT) ctx.get(0));
      }
    }).setPriority(priority);
    return this;
  }

  public SimpleParserSpecification<ExprT> changeGrammar(Handler<SimpleGrammarContext> handler) {
    handler.handle(new SimpleGrammarContext() {
      @Override
      public Grammar grammar() {
        return myGrammar;
      }

      @Override
      public Terminal terminal(Token token) {
        return getOrDeclareTerminal(token);
      }

      @Override
      public NonTerminal expr() {
        return myExpr;
      }

      @Override
      public Terminal id() {
        return myId;
      }

      @Override
      public Terminal number() {
        return myIntNumber;
      }

      @Override
      public Terminal bool() {
        return myBool;
      }

      @Override
      public Terminal value(String name, Predicate<Object> predicate) {
        Terminal terminal = myGrammar.newTerminal(name);
        myValueTerminals.put(predicate, terminal);
        return terminal;
      }
    });
    return this;
  }

  private Terminal getOrDeclareTerminal(Token token) {
    Terminal result = myTokenToTerminal.get(token);
    if (result != null) return result;
    result = myGrammar.newTerminal(myGrammar.uniqueName(token.text(), false));
    myTokenToTerminal.put(token, result);
    return result;
  }

  private Lexeme getLexeme(Token token) {
    Terminal terminal = null;
    if (token instanceof IdentifierToken) {
      terminal = myId;
    } else if (token instanceof IntValueToken) {
      terminal = myIntNumber;
    } else if (token instanceof BoolValueToken) {
      terminal = myBool;
    } else if (token instanceof ValueToken) {
      for (Map.Entry<Predicate<Object>, Terminal> e : myValueTerminals.entrySet()) {
        if (e.getKey().apply(((ValueToken) token).value())) {
          terminal = e.getValue();
          break;
        }
      }
    } else {
      terminal = myTokenToTerminal.get(token);
    }

    if (terminal == null) {
      terminal = myError;
    }

    return new Lexeme(terminal, token);
  }

  private LRParserTable buildTable() {
    SLRTableGenerator generator = new SLRTableGenerator(myGrammar);
    return generator.generateTable();
  }

  public Function<ParserParameters, Parser<ExprT>> buildParameterizedParser() {
    final LRParserTable table = buildTable();
    return new Function<ParserParameters, Parser<ExprT>>() {
      @Override
      public Parser<ExprT> apply(final ParserParameters parserParameters) {
        return new Parser<ExprT>() {
          @Override
          public ExprT parse(ParsingContext ctx) {
            LRParser parser = new LRParser(table, parserParameters);
            return (ExprT) parser.parse(toLexemes(ctx));
          }
        };
      }
    };
  }

  private List<Lexeme> toLexemes(ParsingContext ctx) {
    List<Lexeme> lexemes = new ArrayList<>();
    while (ctx.current() != null) {
      lexemes.add(getLexeme(ctx.current()));
      ctx.advance();
    }
    lexemes.add(new Lexeme(myGrammar.getEnd(), null));
    return lexemes;
  }

  public Parser<ExprT> buildParser() {
    return buildParameterizedParser().apply(ParserParameters.EMPTY);
  }

  public interface SimpleGrammarContext {
    Grammar grammar();

    Terminal terminal(Token token);

    NonTerminal expr();
    Terminal id();
    Terminal number();
    Terminal bool();
    Terminal value(String name, Predicate<Object> predicate);
  }
}