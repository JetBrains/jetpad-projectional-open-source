package jetbrains.jetpad.projectional.demo.indentDemo;

import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.projectional.demo.indentDemo.mapper.ContainerMapper;
import jetbrains.jetpad.projectional.demo.indentDemo.model.*;
import jetbrains.jetpad.projectional.util.RootController;

public class LambdaDemo {
  private static Container createModel() {
    Container root = new Container();
    LambdaExpr lambda = new LambdaExpr();
    lambda.varName.set("x");

    VarExpr var = new VarExpr();
    var.name.set("x");
    lambda.body.set(var);

    root.expr.set(lambda);
    return root;
  }

  public static CellContainer create() {
    CellContainer cellContainer = new CellContainer();
    RootController.install(cellContainer);
    ContainerMapper rootMapper = new ContainerMapper(createModel());
    rootMapper.attachRoot();
    cellContainer.root.children().add(rootMapper.getTarget());
    return cellContainer;
  }
}
