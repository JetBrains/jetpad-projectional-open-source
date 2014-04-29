package jetbrains.jetpad.cell.toDom;

import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.cell.dom.DomCell;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;

class DomCellMapper extends BaseCellMapper<DomCell> {
  DomCellMapper(DomCell source, CellToDomContext ctx) {
    super(source, ctx, DOM.createDiv());
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperty(getSource().node, new WritableProperty<Node>() {
      @Override
      public void set(Node value) {
        getTarget().removeAllChildren();
        if (value != null) {
          getTarget().appendChild(value);
        }
      }
    }));
  }
}
