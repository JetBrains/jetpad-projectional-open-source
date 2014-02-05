package jetbrains.jetpad.lambda.mapper;

import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.lambda.model.WildCardExpr;

class WildCardExprMapper extends Mapper<WildCardExpr, TextCell> {
  WildCardExprMapper(WildCardExpr source) {
    super(source, CellFactory.label("?"));
  }
}
