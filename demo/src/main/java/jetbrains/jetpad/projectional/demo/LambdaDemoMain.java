package jetbrains.jetpad.projectional.demo;

import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.view.MapperCell2View;
import jetbrains.jetpad.projectional.demo.lambda.LambdaDemo;
import jetbrains.jetpad.projectional.view.ViewContainer;
import jetbrains.jetpad.projectional.view.awt.AwtDemo;

public class LambdaDemoMain {
  public static void main(String[] args) {
    CellContainer cellContainer = LambdaDemo.create();
    ViewContainer viewContainer = new ViewContainer();
    MapperCell2View.map(cellContainer, viewContainer);
    AwtDemo.show(viewContainer);
  }
}
