package jetbrains.jetpad.event;

import java.util.Set;

public interface KeyStrokeSpec {
  boolean matches(KeyStroke keyStroke);
  Set<KeyStroke> keyStrokes();
}
