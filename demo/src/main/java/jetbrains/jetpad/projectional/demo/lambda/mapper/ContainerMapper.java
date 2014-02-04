package jetbrains.jetpad.projectional.demo.lambda.mapper;

import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.indent.IndentRootCell;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;
import jetbrains.jetpad.projectional.demo.lambda.model.Container;
import jetbrains.jetpad.projectional.demo.lambda.model.Expr;
import jetbrains.jetpad.projectional.demo.lambda.model.LambdaNode;

import static jetbrains.jetpad.cell.util.CellFactory.*;

public class ContainerMapper extends Mapper<Container, ContainerMapper.ContainerCell> {
  public ContainerMapper(Container source) {
    super(source, new ContainerCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(ProjectionalSynchronizers.<LambdaNode, Expr>forSingleRole(this, getSource().expr, getTarget().expr, new ExprMapperFactory()));
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
