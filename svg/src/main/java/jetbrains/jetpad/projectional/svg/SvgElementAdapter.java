package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public class SvgElementAdapter implements SvgElementListener {
  @Override
  public <ValueT> void onPropertySet(SvgPropertySpec<ValueT> spec, PropertyChangeEvent<ValueT> event) {
  }

  @Override
  public void onSvgElementAttached() {
  }

  @Override
  public void onSvgElementDetached() {
  }

  @Override
  public void onChildAdded(CollectionItemEvent<SvgElement> event) {
  }

  @Override
  public void onChildRemoved(CollectionItemEvent<SvgElement> event) {
  }

  @Override
  public void onParentChanged(PropertyChangeEvent<SvgElement> event) {
  }
}
