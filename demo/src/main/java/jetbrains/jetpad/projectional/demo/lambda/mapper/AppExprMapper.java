package jetbrains.jetpad.projectional.demo.lambda.mapper;

import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;
import jetbrains.jetpad.projectional.demo.lambda.model.AppExpr;
import jetbrains.jetpad.projectional.demo.lambda.model.Expr;
import jetbrains.jetpad.projectional.demo.lambda.model.LambdaNode;

import static jetbrains.jetpad.cell.util.CellFactory.*;

class AppExprMapper extends Mapper<AppExpr, AppExprMapper.AppExprCell> {
  AppExprMapper(AppExpr source) {
    super(source, new AppExprCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(ProjectionalSynchronizers.<LambdaNode, Expr>forSingleRole(this, getSource().fun, getTarget().fun, new ExprMapperFactory()));
    conf.add(ProjectionalSynchronizers.<LambdaNode, Expr>forSingleRole(this, getSource().arg, getTarget().arg, new ExprMapperFactory()));
  }

  static final class AppExprCell extends IndentCell {
    final IndentCell fun = new IndentCell();
    final IndentCell arg = new IndentCell();

    AppExprCell() {
      to(this, fun, space(), arg);
    }
  }
}
