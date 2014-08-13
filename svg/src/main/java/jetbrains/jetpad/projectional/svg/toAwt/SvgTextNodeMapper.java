
package jetbrains.jetpad.projectional.svg.toAwt;

import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.svg.SvgTextNode;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Text;

public class SvgTextNodeMapper extends SvgNodeMapper<SvgTextNode, Text> {
  public SvgTextNodeMapper(SvgTextNode source, Text target, AbstractDocument doc) {
    super(source, target, doc);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forPropsOneWay(getSource().getTextContent(), new WritableProperty<String>() {
      @Override
      public void set(String value) {
        getTarget().setNodeValue(value);
      }
    }));
  }
}
