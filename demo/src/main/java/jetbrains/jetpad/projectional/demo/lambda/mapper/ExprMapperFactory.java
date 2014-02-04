package jetbrains.jetpad.projectional.demo.lambda.mapper;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.projectional.demo.lambda.model.AppExpr;
import jetbrains.jetpad.projectional.demo.lambda.model.Expr;
import jetbrains.jetpad.projectional.demo.lambda.model.LambdaExpr;
import jetbrains.jetpad.projectional.demo.lambda.model.VarExpr;

class ExprMapperFactory implements MapperFactory<Expr, Cell> {
  @Override
  public Mapper<? extends Expr, ? extends Cell> createMapper(Expr source) {
    if (source instanceof VarExpr) {
      return new VarExprMapper((VarExpr) source);
    }

    if (source instanceof AppExpr) {
      return new AppExprMapper((AppExpr) source);
    }

    if (source instanceof LambdaExpr) {
      return new LambdaExprMapper((LambdaExpr) source);
    }

    return null;
  }
}
