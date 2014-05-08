package jetbrains.jetpad.projectional.view.dom;

import com.google.gwt.dom.client.Element;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewPropertyKind;
import jetbrains.jetpad.projectional.view.ViewPropertySpec;

public class DomView extends View {
  public static final ViewPropertySpec<Element> ELEMENT = new ViewPropertySpec<>("element", ViewPropertyKind.RELAYOUT);

  public final Property<Element> element = getProp(ELEMENT);

  @Override
  protected void doValidate(ValidationContext ctx) {
    super.doValidate(ctx);
    Element e = this.element.get();
    if (e != null) {
      ctx.bounds(new Vector(e.getScrollWidth(), e.getScrollHeight()), 0);
    } else {
      ctx.bounds(Vector.ZERO, 0);
    }
  }
}
