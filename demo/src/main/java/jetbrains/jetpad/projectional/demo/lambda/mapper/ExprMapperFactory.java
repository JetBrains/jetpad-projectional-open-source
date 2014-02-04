package jetbrains.jetpad.projectional.demo.lambda.mapper;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.projectional.demo.lambda.model.Expr;

class ExprMapperFactory implements MapperFactory<Expr, Cell> {
  @Override
  public Mapper<? extends Expr, ? extends Cell> createMapper(Expr source) {
    return null;
  }
}
