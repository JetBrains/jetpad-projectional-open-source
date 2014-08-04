package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.model.property.PropertyChangeEvent;

public interface SvgContainerListener {
  void onPropertySet(SvgElement element, SvgPropertySpec<?> spec, PropertyChangeEvent<?> event);
  void onElementAttached(SvgElement element);
  void onElementDetached(SvgElement element);
}
