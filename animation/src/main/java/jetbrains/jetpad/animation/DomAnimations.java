package jetbrains.jetpad.animation;

import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;

import static com.google.gwt.query.client.GQuery.$;

public class DomAnimations {
  public static Animation fadeIn(final Element e, final int duration) {
    return new GQueryBasedAnimation() {
      @Override
      protected GQuery createAnimation(final Runnable callback) {
        return GQuery.$(e).css("opacity", "0").animate("opacity : 1", duration, new Function() {
          @Override
          public void f() {
            callback.run();
          }
        });
      }
    };
  }

  public static Animation fadeOut(final Element e, final int duration) {
    return new GQueryBasedAnimation() {
      @Override
      protected GQuery createAnimation(final Runnable callback) {
        final GQuery target = $(e);
        return target.fadeOut(duration, new Function() {
          @Override
          public void f() {
            callback.run();
            target.show();
          }
        });
      }
    };
  }

  public static Animation slideShow(final Element e, final int duration) {
    return new GQueryBasedAnimation() {
      @Override
      protected GQuery createAnimation(final Runnable callback) {
        final GQuery target = $(e);
        target.hide();
        return target.slideToggle(duration, new Function() {
          @Override
          public void f() {
            callback.run();
          }
        });
      }
    };
  }

  public static Animation slideHide(final Element e, final int duration) {
    return new GQueryBasedAnimation() {
      @Override
      protected GQuery createAnimation(final Runnable callback) {
        final GQuery target = $(e);
        return target.slideToggle(duration, new Function() {
          @Override
          public void f() {
            callback.run();
            target.show();
          }
        });
      }
    };
  }
}
