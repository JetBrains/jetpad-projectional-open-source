package jetbrains.jetpad.cell.toDom;

import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.dom.DomCell;
import jetbrains.jetpad.cell.event.FocusEvent;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;

import static com.google.gwt.query.client.GQuery.$;

class DomCellMapper extends BaseCellMapper<DomCell> {
  DomCellMapper(DomCell source, CellToDomContext ctx) {
    super(source, ctx, DOM.createDiv());

    getTarget().addClassName(CellContainerToDomMapper.CSS.domCell());
  }

  @Override
  protected boolean managesChildren() {
    return true;
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

    conf.add(Synchronizers.forRegistration(getSource().addTrait(new CellTrait() {
      @Override
      public void onFocusGained(Cell cell, FocusEvent event) {
        if (getSource().node.get() != null) {
          $(getSource().node.get()).focus();
        }
        super.onFocusGained(cell, event);
      }
    })));
  }
}
