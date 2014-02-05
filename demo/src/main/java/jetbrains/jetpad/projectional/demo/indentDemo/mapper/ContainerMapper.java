package jetbrains.jetpad.projectional.demo.indentDemo.mapper;

import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.demo.indentDemo.model.Container;

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

  static class ContainerCell extends IndentCell {
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
