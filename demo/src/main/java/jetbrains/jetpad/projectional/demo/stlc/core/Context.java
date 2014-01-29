package jetbrains.jetpad.projectional.demo.stlc.core;

import com.google.common.base.Objects;

public abstract class Context<ValueT> {
  public static <ValueT> Context<ValueT> empty() {
    return new Context<ValueT>() {
      @Override
      public ValueT getType(String var) {
        return null;
      }
    };
  }

  public static <ValueT> Context<ValueT> add(final Context<ValueT> ctx, final String var, final ValueT type) {
    return new Context<ValueT>() {
      @Override
      public ValueT getType(String v) {
        if (Objects.equal(v, var)) return type;
        return ctx.getType(v);
      }
    };
  }

  public abstract ValueT getType(String var);
}
