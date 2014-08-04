package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

interface SvgElementListener {
  <ValueT> void onPropertySet(SvgPropertySpec<ValueT> spec, PropertyChangeEvent<ValueT> event);

  void onSvgElementAttached();
  void onSvgElementDetached();

  void onChildAdded(CollectionItemEvent<SvgElement> event);
  void onChildRemoved(CollectionItemEvent<SvgElement> event);

  void onParentChanged(PropertyChangeEvent<SvgElement> event);
}
