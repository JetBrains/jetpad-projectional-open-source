package jetbrains.jetpad.projectional.demo.lambda.mapper;

import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.demo.lambda.model.ParensExpr;

class ParensExprMapper extends Mapper<ParensExpr, ParensExprMapper.ParensExprCell> {
  ParensExprMapper(ParensExpr source) {
    super(source, new ParensExprCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);
    conf.add(LambdaSynchronizers.exprSynchronizer(this, getSource().expr, getTarget().expr));
  }

  static class ParensExprCell extends IndentCell {
    final IndentCell expr = new IndentCell();

    ParensExprCell() {
      CellFactory.to(this, CellFactory.label("(", true, false), expr, CellFactory.label(")", false, true));
    }
  }
}
