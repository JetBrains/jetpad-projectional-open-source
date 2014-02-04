package jetbrains.jetpad.projectional.demo.lambda.mapper;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;
import jetbrains.jetpad.projectional.demo.lambda.model.Container;
import jetbrains.jetpad.projectional.demo.lambda.model.Expr;
import jetbrains.jetpad.projectional.demo.lambda.model.LambdaNode;

public class ContainerMapper extends Mapper<Container, ContainerCell> {
  public ContainerMapper(Container source) {
    super(source, new ContainerCell());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(ProjectionalSynchronizers.<LambdaNode, Expr>forSingleRole(this, getSource().expr, getTarget().expr, new ExprMapperFactory()));
  }
}
