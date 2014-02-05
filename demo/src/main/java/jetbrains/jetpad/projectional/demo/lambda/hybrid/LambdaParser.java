package jetbrains.jetpad.projectional.demo.lambda.hybrid;

import com.google.common.base.Predicate;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.grammar.*;
import jetbrains.jetpad.grammar.lr.Lexeme;
import jetbrains.jetpad.hybrid.parser.IdentifierToken;
import jetbrains.jetpad.hybrid.parser.Parser;
import jetbrains.jetpad.hybrid.parser.simple.SimpleParserSpecification;
import jetbrains.jetpad.projectional.demo.lambda.model.*;

class LambdaParser {
  static final Parser<Expr> EXPR;

  static {
    SimpleParserSpecification<Expr> spec = new SimpleParserSpecification<Expr>();
    spec.changeGrammar(new Handler<SimpleParserSpecification.SimpleGrammarContext>() {
      @Override
      public void handle(SimpleParserSpecification.SimpleGrammarContext ctx) {
        Grammar g = ctx.grammar();
        NonTerminal expr = ctx.expr();

        Terminal wildcard = ctx.terminal(Tokens.WILDCARD);
        Terminal lp = ctx.terminal(Tokens.LP);
        Terminal rp = ctx.terminal(Tokens.RP);
        Terminal id = ctx.id();
        Terminal valExpr = ctx.value("valExpr", new Predicate<Object>() {
          @Override
          public boolean apply(Object o) {
            return o instanceof Expr;
          }
        });

        g.newRule(expr, wildcard).setPriority(10).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            return new WildCardExpr();
          }
        });
        g.newRule(expr, id).setPriority(10).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            VarExpr result = new VarExpr();
            Lexeme lexeme = (Lexeme) ctx.get(0);
            IdentifierToken idToken = (IdentifierToken) lexeme.getValue();
            result.name.set(idToken.text());
            return result;
          }
        });
        g.newRule(expr, lp, expr, rp).setPriority(10).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            Expr expr = (Expr) ctx.get(1);
            ParensExpr result = new ParensExpr();
            result.expr.set(expr);
            return result;
          }
        });
        g.newRule(expr, valExpr).setPriority(10).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            return (Expr) ctx.get(0);
          }
        });
        g.newRule(expr, expr, expr).setPriority(0).setHandler(new RuleHandler() {
          @Override
          public Object handle(RuleContext ctx) {
            Expr fun = (Expr) ctx.get(0);
            Expr arg = (Expr) ctx.get(1);

            AppExpr result = new AppExpr();
            result.fun.set(fun);
            result.arg.set(arg);
            return result;
          }
        });
      }
    });

    EXPR = spec.buildParser();
  }

  public static void main(String[] args) {


  }
}
