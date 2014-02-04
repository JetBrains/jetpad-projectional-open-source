package jetbrains.jetpad.projectional.demo.lambda.mapper;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.cell.ProjectionalRoleSynchronizer;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;
import jetbrains.jetpad.projectional.demo.lambda.model.*;
import jetbrains.jetpad.projectional.generic.Role;
import jetbrains.jetpad.projectional.generic.RoleCompletion;

import java.util.ArrayList;
import java.util.List;

class LambdaSynchronizers {
  static ProjectionalRoleSynchronizer<LambdaNode, Expr> exprSynchronizer(
      Mapper<? extends LambdaNode, ? extends Cell> mapper,
      Property<Expr> exprProp,
      Cell targetCell) {
    ProjectionalRoleSynchronizer<LambdaNode, Expr> result = ProjectionalSynchronizers.<LambdaNode, Expr>forSingleRole(mapper, exprProp, targetCell, new ExprMapperFactory());

    result.setCompletion(new RoleCompletion<LambdaNode, Expr>() {
      @Override
      public List<CompletionItem> createRoleCompletion(CompletionParameters ctx, Mapper<?, ?> mapper, LambdaNode contextNode, final Role<Expr> target) {
        List<CompletionItem> result = new ArrayList<CompletionItem>();

        result.add(new SimpleCompletionItem("var") {
          @Override
          public Runnable complete(String text) {
            VarExpr result = new VarExpr();
            result.name.set("");
            return target.set(result);
          }
        });

        result.add(new SimpleCompletionItem("app") {
          @Override
          public Runnable complete(String text) {
            AppExpr result = new AppExpr();
            return target.set(result);
          }
        });

        result.add(new SimpleCompletionItem("\\") {
          @Override
          public Runnable complete(String text) {
            LambdaExpr result = new LambdaExpr();
            return target.set(result);
          }
        });

        result.add(new SimpleCompletionItem("(") {
          @Override
          public Runnable complete(String text) {
            ParensExpr result = new ParensExpr();
            return target.set(result);
          }
        });

        return result;
      }
    });

    return result;
  }
}
