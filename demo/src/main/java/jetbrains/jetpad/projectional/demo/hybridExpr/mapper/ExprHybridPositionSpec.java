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
package jetbrains.jetpad.projectional.demo.hybridExpr.mapper;

import com.google.common.base.Function;
import jetbrains.jetpad.cell.action.CellAction;
import jetbrains.jetpad.cell.completion.*;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.*;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.types.FieldDescriptor;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.types.MethodDescriptor;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.types.Type;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.types.Types;
import jetbrains.jetpad.hybrid.Completer;
import jetbrains.jetpad.hybrid.CompletionContext;
import jetbrains.jetpad.hybrid.HybridPositionSpec;
import jetbrains.jetpad.hybrid.parser.*;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinterContext;
import jetbrains.jetpad.projectional.util.Validators;

import java.util.ArrayList;
import java.util.List;

public class ExprHybridPositionSpec implements HybridPositionSpec<Expression> {
  @Override
  public Parser<Expression> getParser() {
    return ExpressionParser.PARSER;
  }

  @Override
  public PrettyPrinter<ExpressionNode> getPrettyPrinter() {
    return new PrettyPrinter<ExpressionNode>() {
      @Override
      public void print(ExpressionNode value, PrettyPrinterContext<ExpressionNode> ctx) {
        if (value instanceof BoolExpression) {
          BoolExpression boolExpr = (BoolExpression) value;
          ctx.appendBool(boolExpr.value);
          return;
        }

        if (value instanceof DivExpression) {
          DivExpression divExpr = (DivExpression) value;
          ctx.append(divExpr.left);
          ctx.append(Tokens.DIV);
          ctx.append(divExpr.right);
          return;
        }

        if (value instanceof DotExpression) {
          DotExpression dotExpr = (DotExpression) value;
          ctx.append(dotExpr.operand);
          ctx.append(Tokens.DOT);
          ctx.append(dotExpr.operation);
          return;
        }

        if (value instanceof MinusExpression) {
          MinusExpression minusExpr = (MinusExpression) value;
          ctx.append(minusExpr.left);
          ctx.append(Tokens.MINUS);
          ctx.append(minusExpr.right);
          return;
        }

        if (value instanceof MulExpression) {
          MulExpression mulExpr = (MulExpression) value;
          ctx.append(mulExpr.left);
          ctx.append(Tokens.MUL);
          ctx.append(mulExpr.right);
          return;
        }

        if (value instanceof NumberExpression) {
          NumberExpression numExpr = (NumberExpression) value;
          ctx.appendInt(numExpr.value);
          return;
        }

        if (value instanceof ParensExpression) {
          ParensExpression parensExpr = (ParensExpression) value;
          ctx.append(Tokens.LEFT_PAREN);
          ctx.append(parensExpr.expression);
          ctx.append(Tokens.RIGHT_PAREN);
          return;
        }

        if (value instanceof PlusExpression) {
          PlusExpression plusExpr = (PlusExpression) value;
          ctx.append(plusExpr.left);
          ctx.append(Tokens.PLUS);
          ctx.append(plusExpr.right);
          return;
        }

        if (value instanceof PostfixDecrementExpression) {
          PostfixDecrementExpression posfixExpr = (PostfixDecrementExpression) value;
          ctx.append(posfixExpr.expression);
          ctx.append(Tokens.DECREMENT_RIGHT);
        }

        if (value instanceof PostifxIncrementExpression) {
          PostifxIncrementExpression posfixExpr = (PostifxIncrementExpression) value;
          ctx.append(posfixExpr.expression);
          ctx.append(Tokens.INCREMENT_RIGHT);
          return;
        }

        if (value instanceof PrefixDecrementExpression) {
          PrefixDecrementExpression prefixExpr = (PrefixDecrementExpression) value;
          ctx.append(Tokens.DECREMENT_LEFT);
          ctx.append(prefixExpr.expression);
          return;
        }

        if (value instanceof PrefixIncrementExpression) {
          PrefixIncrementExpression prefixExpr = (PrefixIncrementExpression) value;
          ctx.append(Tokens.INCREMENT_LEFT);
          ctx.append(prefixExpr.expression);
          return;
        }

        if (value instanceof VarExpression) {
          VarExpression varExpr = (VarExpression) value;
          ctx.appendId(varExpr.name);
          return;
        }

        if (value instanceof FieldReferenceOperation) {
          FieldReferenceOperation fieldRef = (FieldReferenceOperation) value;
          ctx.appendId(fieldRef.fieldName);
          return;
        }

        if (value instanceof MethodCallOperation) {
          MethodCallOperation methodCall = (MethodCallOperation) value;
          ctx.appendId(methodCall.methodName);
          ctx.append(Tokens.LEFT_PARENT_METHOD_CALL);
          ctx.append(methodCall.arguments, Tokens.COMMA);
          ctx.append(Tokens.RIGHT_PAREN);
          return;
        }

        throw new IllegalArgumentException("" + value);
      }
    };
  }

  @Override
  public CompletionSupplier getTokenCompletion(final Function<Token, CellAction> tokenHandler) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        class SimpleTokenCompletionItem extends SimpleCompletionItem {
          private Token myToken;

          SimpleTokenCompletionItem(Token token) {
            super(token.toString());
            myToken = token;
          }

          @Override
          public CellAction complete(String text) {
            return tokenHandler.apply(myToken);
          }
        }

        List<CompletionItem> result = new ArrayList<CompletionItem>();

        result.add(new SimpleTokenCompletionItem(Tokens.PLUS));
        result.add(new SimpleTokenCompletionItem(Tokens.MINUS));
        result.add(new SimpleTokenCompletionItem(Tokens.MUL));
        result.add(new SimpleTokenCompletionItem(Tokens.DIV));
        result.add(new SimpleTokenCompletionItem(Tokens.LEFT_PAREN));
        result.add(new SimpleTokenCompletionItem(Tokens.RIGHT_PAREN));
        result.add(new SimpleTokenCompletionItem(Tokens.INCREMENT));
        result.add(new SimpleTokenCompletionItem(Tokens.DECREMENT));
        result.add(new SimpleTokenCompletionItem(Tokens.DOT));
        result.add(new SimpleTokenCompletionItem(Tokens.TRUE));
        result.add(new SimpleTokenCompletionItem(Tokens.FALSE));
        result.add(new SimpleTokenCompletionItem(Tokens.COMMA));

        result.add(new BaseCompletionItem() {
          @Override
          public String visibleText(String text) {
            return "number";
          }

          @Override
          public boolean isStrictMatchPrefix(String text) {
            if ("".equals(text)) return true;
            return isMatch(text);
          }

          @Override
          public boolean isMatch(String text) {
            return Validators.integer().apply(text);
          }

          @Override
          public CellAction complete(String text) {
            int value;
            if (text == null || text.isEmpty()) {
              value = 0;
            } else {
              value = Integer.parseInt(text);
            }
            return tokenHandler.apply(new IntValueToken(value));
          }
        });

        result.add(new BaseCompletionItem() {
          @Override
          public String visibleText(String text) {
            return "identifier";
          }

          @Override
          public boolean isStrictMatchPrefix(String text) {
            if ("".equals(text)) return true;
            return isMatch(text);
          }

          @Override
          public boolean isMatch(String text) {
            return Validators.identifier().apply(text);
          }

          @Override
          public CellAction complete(String text) {
            return tokenHandler.apply(new IdentifierToken(text));
          }

          @Override
          public boolean isLowPriority() {
            return true;
          }
        });

        return result;
      }
    };
  }


  @Override
  public CompletionSupplier getAdditionalCompletion(final CompletionContext ctx, final Completer completer) {
    List<Token> input = new ArrayList<Token>(ctx.prefix());
    input.add(new IdentifierToken("dummy"));
    Expression result = getParser().parse(new ParsingContext(input));

    final Type type;
    if (ctx.targetIndex() < ctx.views().size() && ctx.objects().get(ctx.targetIndex()) instanceof Operation) {
      Operation op = (Operation) ctx.objects().get(ctx.targetIndex());
      DotExpression dotExpr = (DotExpression) op.parent().get();
      Expression operand = dotExpr.operand.get();
      type = operand == null ? Types.OBJECT : operand.getType();
    } else {
      type = result == null ? Types.OBJECT : getContextType(result);
    }

    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        List<CompletionItem> result = new ArrayList<CompletionItem>();
        for (final FieldDescriptor fd : type.getFields()) {
          result.add(new SimpleCompletionItem(fd.getName()) {
            @Override
            public CellAction complete(String text) {
              return completer.complete(new IdentifierToken(fd.getName()));
            }
          });
        }

        for (final MethodDescriptor md : type.getMethods()) {
          result.add(new SimpleCompletionItem(md.getName(), md.toString()) {
            @Override
            public CellAction complete(String text) {
              if (ctx.targetIndex() + 1 < ctx.views().size() && ctx.tokens().get(ctx.targetIndex() + 1) == Tokens.LEFT_PARENT_METHOD_CALL) {
                return completer.complete(new IdentifierToken(md.getName()));
              }

              return completer.complete(
                md.getParameterTypes().isEmpty() ? 2 : 1,
                new IdentifierToken(md.getName()),
                Tokens.LEFT_PARENT_METHOD_CALL,
                Tokens.RIGHT_PAREN);
            }
          });
        }

        return result;
      }
    };
  }

  private Type getContextType(Expression expression) {
    if (expression instanceof DotExpression) {
      return ((DotExpression) expression).operand.get().getType();
    }

    if (expression instanceof BinaryExpression) {
      return getContextType(((BinaryExpression) expression).right.get());
    }

    return Types.OBJECT;
  }
}