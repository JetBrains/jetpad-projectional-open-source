package jetbrains.jetpad.projectional.demo.indentDemo.mapper;

import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.cell.util.Validators;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.demo.indentDemo.model.LambdaExpr;

class LambdaExprMapper extends Mapper<LambdaExpr, LambdaExprMapper.LambdaExprCell> {
  LambdaExprMapper(LambdaExpr source) {
    super(source, new LambdaExprCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperties(getSource().varName, getTarget().name.text()));
    conf.add(LambdaSynchronizers.exprSynchronizer(this, getSource().body, getTarget().body));
  }

  static class LambdaExprCell extends IndentCell {
    final TextCell name = new TextCell();
    final IndentCell body = new IndentCell();

    LambdaExprCell() {
      focusable().set(true);

      CellFactory.to(this, CellFactory.label("(\\", true, false), name, CellFactory.space(), CellFactory.label("->"), CellFactory.indent(true, CellFactory.newLine(), CellFactory.space(), body, CellFactory.label(")", false, true)));

      name.addTrait(TextEditing.validTextEditing(Validators.identifier()));
    }
  }
}
