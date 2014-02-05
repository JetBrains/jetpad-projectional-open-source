package jetbrains.jetpad.lambda.mapper;

import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.lambda.model.AppExpr;

import static jetbrains.jetpad.cell.util.CellFactory.space;
import static jetbrains.jetpad.cell.util.CellFactory.to;

class AppExprMapper extends Mapper<AppExpr, AppExprMapper.AppExprCell> {
  AppExprMapper(AppExpr source) {
    super(source, new AppExprCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(LambdaSynchronizers.exprSynchronizer(this, getSource().fun, getTarget().fun));
    conf.add(LambdaSynchronizers.exprSynchronizer(this, getSource().arg, getTarget().arg));
  }

  static final class AppExprCell extends IndentCell {
    final IndentCell fun = new IndentCell();
    final IndentCell arg = new IndentCell();

    AppExprCell() {
      focusable().set(true);
      to(this, CellFactory.label("(", true, false), fun, space(), arg, CellFactory.label(")", false, true));
    }
  }
}
