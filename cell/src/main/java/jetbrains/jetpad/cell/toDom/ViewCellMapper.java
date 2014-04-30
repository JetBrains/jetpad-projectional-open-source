package jetbrains.jetpad.cell.toDom;

import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.cell.view.ViewCell;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewContainer;
import jetbrains.jetpad.projectional.view.gwt.View2Dom;

class ViewCellMapper extends BaseCellMapper<ViewCell> {
  private ViewContainer myViewContainer;

  ViewCellMapper(ViewCell source, CellToDomContext ctx) {
    super(source, ctx, DOM.createDiv());
    myViewContainer = new ViewContainer();
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forRegistration(View2Dom.map(myViewContainer, getTarget())));

    conf.add(Synchronizers.forProperty(getSource().view, new WritableProperty<View>() {
      @Override
      public void set(View value) {
        myViewContainer.contentRoot().children().clear();

        if (value != null) {
          myViewContainer.contentRoot().children().add(value);
        }
      }
    }));
  }
}
