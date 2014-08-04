package jetbrains.jetpad.projectional.svg;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;

public class SvgContainer {
  private Property<SvgRoot> mySvgRoot = new ValueProperty<SvgRoot>() {
    @Override
    public void set(SvgRoot value) {
      if (mySvgRoot.get() != null) {
        mySvgRoot.get().detach();
      }
      super.set(value);
      mySvgRoot.get().attach(SvgContainer.this);
    }
  };
  private Listeners<SvgContainerListener> myListeners = new Listeners<>();

  public SvgContainer(SvgRoot root) {
    mySvgRoot.set(root);
  }

  public Property<SvgRoot> root() {
    return mySvgRoot;
  }

  public Registration addListener(SvgContainerListener l) {
    return myListeners.add(l);
  }

  void propertyChanged(final SvgElement element, final SvgPropertySpec<?> spec, final PropertyChangeEvent<?> event) {
    myListeners.fire(new ListenerCaller<SvgContainerListener>() {
      @Override
      public void call(SvgContainerListener l) {
        l.onPropertySet(element, spec, event);
      }
    });
  }

  void svgElementAttached(final SvgElement element) {
    myListeners.fire(new ListenerCaller<SvgContainerListener>() {
      @Override
      public void call(SvgContainerListener l) {
        l.onElementAttached(element);
      }
    });
  }

  void svgElementDetached(final SvgElement element) {
    myListeners.fire(new ListenerCaller<SvgContainerListener>() {
      @Override
      public void call(SvgContainerListener l) {
        l.onElementDetached(element);
      }
    });
  }
}
