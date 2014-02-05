package jetbrains.jetpad.projectional.demo.indentDemo.mapper;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.hybrid.HybridSynchronizer;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizer;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.demo.indentDemo.hybrid.LambdaHybridPositionSpec;
import jetbrains.jetpad.projectional.demo.indentDemo.model.Expr;
import jetbrains.jetpad.projectional.demo.indentDemo.model.LambdaExpr;
import jetbrains.jetpad.projectional.demo.indentDemo.model.LambdaNode;

class LambdaSynchronizers {
  static Synchronizer exprSynchronizer(
      Mapper<? extends LambdaNode, ? extends Cell> mapper,
      Property<Expr> exprProp,
      Cell targetCell) {
    HybridSynchronizer<Expr> result = new HybridSynchronizer<Expr>(mapper, exprProp, targetCell, new LambdaHybridPositionSpec());

    result.setMapperFactory(new MapperFactory<Object, Cell>() {
      @Override
      public Mapper<?, ? extends Cell> createMapper(Object source) {
        if (source instanceof LambdaExpr) {
          return new LambdaExprMapper((LambdaExpr) source);
        }

        return null;
      }
    });

    return result;
  }
}
