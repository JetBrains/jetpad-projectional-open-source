package jetbrains.jetpad.projectional.demo.stlc.core;

public class Type {
  public static class NatType extends Type {
  }

  public static class FunctionType extends Type {
    public final Type domain;
    public final Type codomain;

    public FunctionType(Type codomain, Type domain) {
      this.codomain = codomain;
      this.domain = domain;
    }
  }
}
