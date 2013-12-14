package jetbrains.jetpad.views;

import jetbrains.jetpad.model.property.Property;

public interface HasVisibility {
  Property<Boolean> visible();
}
