package jetbrains.jetpad.projectional.view.toGwt;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.SvgSvgElement;
import jetbrains.jetpad.projectional.svg.toDom.SvgSvgElementMapper;
import jetbrains.jetpad.projectional.view.SvgView;
import org.vectomatic.dom.svg.OMSVGSVGElement;

public class SvgViewMapper extends BaseViewMapper<SvgView, Element> {
  private static Registration map(SvgSvgElement root, OMSVGSVGElement element) {
    final SvgSvgElementMapper mapper = new SvgSvgElementMapper(root, element);
    mapper.attachRoot();
    return new Registration() {
      @Override
      public void remove() {
        mapper.detachRoot();
      }
    };
  }

  private Registration myReg = Registration.EMPTY;

  public SvgViewMapper(ViewToDomContext ctx, SvgView source) {
    super(ctx, source, DOM.createDiv());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forPropsOneWay(getSource().root(), new WritableProperty<SvgSvgElement>() {
      @Override
      public void set(SvgSvgElement value) {
        OMSVGSVGElement element = new OMSVGSVGElement();

        getTarget().removeAllChildren();
        getTarget().appendChild(element.getElement());

        myReg.remove();
        myReg = map(value, element);
      }
    }));
  }

  @Override
  protected void onDetach() {
    super.onDetach();

    myReg.remove();
  }
}
