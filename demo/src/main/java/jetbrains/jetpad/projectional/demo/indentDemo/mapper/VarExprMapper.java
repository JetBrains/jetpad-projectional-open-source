package jetbrains.jetpad.projectional.demo.indentDemo.mapper;

import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.cell.util.Validators;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.demo.indentDemo.model.VarExpr;

class VarExprMapper extends Mapper<VarExpr, TextCell> {
  VarExprMapper(VarExpr source) {
    super(source, CellFactory.label(""));
    getTarget().addTrait(TextEditing.validTextEditing(Validators.identifier()));
    getTarget().focusable().set(true);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperties(getSource().name, getTarget().text()));
  }
}
