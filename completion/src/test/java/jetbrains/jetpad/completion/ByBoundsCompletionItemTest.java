package jetbrains.jetpad.completion;

import jetbrains.jetpad.base.Runnables;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public final class ByBoundsCompletionItemTest {

  @Test
  public void noSuffix() {
    ByBoundsCompletionItem byBoundsCompletionItem = new ByBoundsCompletionItem("#") {
      @Override
      public Runnable complete(String text) {
        return Runnables.EMPTY;
      }
    };
    assertTrue(byBoundsCompletionItem.isMatch("# + 3"));
  }

}
