package jetbrains.jetpad.lambda;

import com.google.gwt.core.client.EntryPoint;
import jetbrains.jetpad.cell.dom.CellContainerToDomMapper;

import static com.google.gwt.query.client.GQuery.$;

public class LambdaEntryPoint implements EntryPoint {
  @Override
  public void onModuleLoad() {
    new CellContainerToDomMapper(LambdaDemo.create(), $("#lambdaDemo").get(0)).attachRoot();
  }
}
