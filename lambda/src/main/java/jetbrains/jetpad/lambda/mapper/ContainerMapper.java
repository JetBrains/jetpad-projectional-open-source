package jetbrains.jetpad.lambda.mapper;

import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.indent.IndentRootCell;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.lambda.model.Container;

import static jetbrains.jetpad.cell.util.CellFactory.*;

public class ContainerMapper extends Mapper<Container, ContainerMapper.ContainerCell> {
  public ContainerMapper(Container source) {
    super(source, new ContainerCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);
    conf.add(LambdaSynchronizers.exprSynchronizer(this, getSource().expr, getTarget().expr));
  }

  static class ContainerCell extends IndentRootCell {
    final IndentCell expr = new IndentCell();

    ContainerCell() {
      to(this,
        label("Container"),
        indent(true, newLine(),
          expr
        ));
    }
  }
}
