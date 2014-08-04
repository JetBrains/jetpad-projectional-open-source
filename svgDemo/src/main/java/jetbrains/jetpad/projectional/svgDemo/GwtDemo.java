package jetbrains.jetpad.projectional.svgDemo;

import com.google.gwt.core.client.EntryPoint;
import jetbrains.jetpad.projectional.view.toGwt.ViewToDom;

import static com.google.gwt.query.client.GQuery.$;

public class GwtDemo implements EntryPoint {
  public void onModuleLoad() {
    ViewToDom.map(DemoModel.demoViewContainer(), $("#svg").get(0));
  }
}
