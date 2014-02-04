package jetbrains.jetpad.projectional.demo.lambda;

import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.projectional.demo.lambda.mapper.ContainerMapper;
import jetbrains.jetpad.projectional.demo.lambda.model.Container;
import jetbrains.jetpad.projectional.util.RootController;

public class LambdaDemo {
  public static CellContainer createContainer(Container container) {
    CellContainer cellContainer = new CellContainer();
    RootController.install(cellContainer);
    ContainerMapper rootMapper = new ContainerMapper(container);
    rootMapper.attachRoot();
    cellContainer.root.children().add(rootMapper.getTarget());
    return cellContainer;
  }
}
