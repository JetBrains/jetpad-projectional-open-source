package jetbrains.jetpad.cell.toView;

import jetbrains.jetpad.cell.view.ViewCell;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.VerticalView;
import jetbrains.jetpad.projectional.view.View;

class ViewCellMapper extends BaseCellMapper<ViewCell, View> {
  ViewCellMapper(ViewCell source, CellToViewContext ctx) {
    super(source, new VerticalView(), ctx);
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperty(getSource().view, new WritableProperty<View>() {
      @Override
      public void set(View value) {
        getTarget().children().clear();
        if (value != null) {
          getTarget().children().add(value);
        }
      }
    }));
  }
}
