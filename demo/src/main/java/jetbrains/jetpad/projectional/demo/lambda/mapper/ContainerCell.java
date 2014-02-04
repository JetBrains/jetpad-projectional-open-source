package jetbrains.jetpad.projectional.demo.lambda.mapper;

import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.indent.IndentRootCell;
import jetbrains.jetpad.cell.util.CellFactory;

import static jetbrains.jetpad.cell.util.CellFactory.*;

class ContainerCell extends IndentRootCell {
  final IndentCell expr = new IndentCell();

  ContainerCell() {
    to(this,
      label("Container"),
      indent(true, newLine(),
        expr
      ));
  }
}
