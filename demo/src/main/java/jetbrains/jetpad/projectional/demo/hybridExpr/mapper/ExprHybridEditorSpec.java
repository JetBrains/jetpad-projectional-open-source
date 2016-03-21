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
package jetbrains.jetpad.projectional.demo.hybridExpr.mapper;

import com.google.common.base.Function;
import jetbrains.jetpad.completion.*;
import jetbrains.jetpad.hybrid.*;
import jetbrains.jetpad.hybrid.parser.*;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.*;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.types.FieldDescriptor;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.types.MethodDescriptor;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.types.Type;
import jetbrains.jetpad.projectional.demo.hybridExpr.model.types.Types;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinterContext;

import java.util.ArrayList;
import java.util.List;

public class ExprHybridEditorSpec extends BaseHybridEditorSpec<Expression> {
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
          return;
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
  public PairSpec getPairSpec() {
    return PairSpec.EMPTY;
  }

  @Override
  public CompletionSupplier getTokenCompletion(Completer completer, final Function<Token, Runnable> tokenHandler) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        List<CompletionItem> result = new ArrayList<>();
        TokenCompletionItems items = new TokenCompletionItems(tokenHandler);
        result.addAll(items.forTokens(
          Tokens.PLUS, Tokens.MINUS, Tokens.MUL, Tokens.DIV, Tokens.LEFT_PAREN,
          Tokens.RIGHT_PAREN, Tokens.INCREMENT, Tokens.DECREMENT, Tokens.DOT, Tokens.TRUE, Tokens.FALSE, Tokens.COMMA));
        result.add(items.forId());
        result.add(items.forNumber());
        return result;
      }
    };
  }


  @Override
  public CompletionSupplier getAdditionalCompletion(final CompletionContext ctx, final Completer completer) {
    List<Token> input = new ArrayList<>(ctx.getPrefix());
    input.add(new IdentifierToken("dummy"));
    Expression result = getParser().parse(getParsingContextFactory().getParsingContext(input));

    final Type type;
    if (ctx.getTargetIndex() < ctx.getViews().size() && ctx.getObjects().get(ctx.getTargetIndex()) instanceof Operation) {
      Operation op = (Operation) ctx.getObjects().get(ctx.getTargetIndex());
      DotExpression dotExpr = (DotExpression) op.parent().get();
      Expression operand = dotExpr.operand.get();
      type = operand == null ? Types.OBJECT : operand.getType();
    } else {
      type = result == null ? Types.OBJECT : getContextType(result);
    }


    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        List<CompletionItem> result = new ArrayList<>();
        for (final FieldDescriptor fd : type.getFields()) {
          result.add(new SimpleCompletionItem(fd.getName()) {
            @Override
            public Runnable complete(String text) {
              return completer.complete(new IdentifierToken(fd.getName()));
            }
          });
        }

        for (final MethodDescriptor md : type.getMethods()) {
          result.add(new SimpleCompletionItem(md.getName(), md.toString()) {
            @Override
            public Runnable complete(String text) {
              if (ctx.getTargetIndex() + 1 < ctx.getViews().size() && ctx.getTokens().get(ctx.getTargetIndex() + 1) == Tokens.LEFT_PARENT_METHOD_CALL) {
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