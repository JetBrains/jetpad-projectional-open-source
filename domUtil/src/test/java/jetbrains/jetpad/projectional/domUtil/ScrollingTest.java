package jetbrains.jetpad.projectional.domUtil;

import jetbrains.jetpad.base.Interval;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScrollingTest {
  @Test
  public void noScrolling() {
    assertEquals(0, Scrolling.getDelta(new Interval(0, 100), new Interval(5, 50)));
  }

  @Test
  public void ifFitsAlignRight() {
    assertEquals(5, Scrolling.getDelta(new Interval(0, 100), new Interval(95, 105)));
  }

  @Test
  public void ifNotFitAlignLeft() {
    assertEquals(7, Scrolling.getDelta(new Interval(0, 100), new Interval(7, 110)));
  }
}

