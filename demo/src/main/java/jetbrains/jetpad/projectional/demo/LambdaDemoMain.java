package jetbrains.jetpad.projectional.demo;

import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.view.MapperCell2View;
import jetbrains.jetpad.projectional.demo.lambda.LambdaDemo;
import jetbrains.jetpad.projectional.demo.lambda.model.Container;
import jetbrains.jetpad.projectional.demo.lambda.model.LambdaExpr;
import jetbrains.jetpad.projectional.demo.lambda.model.VarExpr;
import jetbrains.jetpad.projectional.demo.nanoLang.NanoLangDemo;
import jetbrains.jetpad.projectional.view.ViewContainer;
import jetbrains.jetpad.projectional.view.awt.AwtDemo;

public class LambdaDemoMain {
  public static void main(String[] args) {
    Container root = new Container();

    LambdaExpr lambda = new LambdaExpr();
    lambda.varName.set("x");

    VarExpr var = new VarExpr();
    var.name.set("x");
    lambda.body.set(var);

    root.expr.set(lambda);

    CellContainer cellContainer = LambdaDemo.createContainer(root);
    ViewContainer viewContainer = new ViewContainer();
    MapperCell2View.map(cellContainer, viewContainer);
    AwtDemo.show(viewContainer);
  }
}
