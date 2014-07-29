package jetbrains.jetpad.projectional.svgDemo;

import com.google.gwt.core.client.EntryPoint;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.projectional.svg.SvgRoot;
import jetbrains.jetpad.projectional.view.*;
import jetbrains.jetpad.projectional.view.toGwt.ViewToDom;
import jetbrains.jetpad.values.Color;

import static com.google.gwt.query.client.GQuery.$;

public class GwtDemo implements EntryPoint {
  public void onModuleLoad() {
    final SvgRoot model = DemoModel.createModel();
    final SvgRoot altModel = DemoModel.createAltModel();
    final SvgView svgView = new SvgView(model);
    svgView.border().set(Color.GRAY);

    ViewContainer container = new ViewContainer();
    HorizontalView hView = new HorizontalView();
    VerticalView vView = new VerticalView();
    TextView textView = new TextView("Press any key to change to alternative model");
    TextView textView2 = new TextView("Use mouse clicks to add some black circles");
    hView.children().add(svgView);
    hView.children().add(textView);
    vView.children().add(hView);
    vView.children().add(textView2);
    container.contentRoot().children().add(vView);

    final Value<Boolean> state = new Value<>(true);

    container.root().addTrait(new ViewTraitBuilder().on(ViewEvents.MOUSE_PRESSED, new ViewEventHandler<MouseEvent>() {
      @Override
      public void handle(View view, MouseEvent e) {
        DemoModel.addCircle(svgView.root().get(), e.x(), e.y());
      }
    })
    .on(ViewEvents.KEY_PRESSED, new ViewEventHandler<KeyEvent>() {
      @Override
      public void handle(View view, KeyEvent e) {
        if (state.get()) {
          svgView.root().set(altModel);
        } else {
          svgView.root().set(model);
        }
        state.set(!state.get());
      }
    })
    .build());

    ViewToDom.map(container, $("#svg").get(0));
  }
}
