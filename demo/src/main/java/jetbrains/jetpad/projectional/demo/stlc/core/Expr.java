package jetbrains.jetpad.projectional.demo.stlc.core;

public class Expr {
  public static class VarExpr extends Expr {
    public final String name;

    public VarExpr(String name) {
      this.name = name;
    }
  }

  public static class AppExpr extends Expr {
    public final Expr function;
    public final Expr argument;

    public AppExpr(Expr argument, Expr function) {
      this.argument = argument;
      this.function = function;
    }
  }

  public static class AbsExpr extends Expr {
    public final String varName;
    public final Type type;
    public final Expr body;

    public AbsExpr(Expr body, Type type, String varName) {
      this.body = body;
      this.type = type;
      this.varName = varName;
    }
  }

  public static class NumExpr extends Expr {
    public final int value;

    public NumExpr(int value) {
      this.value = value;
    }
  }
}
