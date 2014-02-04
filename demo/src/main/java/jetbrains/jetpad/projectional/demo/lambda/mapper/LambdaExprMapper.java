package jetbrains.jetpad.projectional.demo.lambda.mapper;

import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.cell.util.Validators;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;
import jetbrains.jetpad.projectional.demo.lambda.model.Expr;
import jetbrains.jetpad.projectional.demo.lambda.model.LambdaExpr;
import jetbrains.jetpad.projectional.demo.lambda.model.LambdaNode;

class LambdaExprMapper extends Mapper<LambdaExpr, LambdaExprMapper.LambdaExprCell> {
  LambdaExprMapper(LambdaExpr source) {
    super(source, new LambdaExprCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperties(getSource().varName, getTarget().name.text()));
    conf.add(ProjectionalSynchronizers.<LambdaNode, Expr>forSingleRole(this, getSource().body, getTarget().body, new ExprMapperFactory()));
  }

  static class LambdaExprCell extends IndentCell {
    final TextCell name = new TextCell();
    final IndentCell body = new IndentCell();

    LambdaExprCell() {
      CellFactory.to(this, CellFactory.label("(\\", true, false), name, CellFactory.space(), CellFactory.label("->"), CellFactory.space(), body, CellFactory.label(")", false, true));

      name.addTrait(TextEditing.validTextEditing(Validators.identifier()));
    }
  }
}
