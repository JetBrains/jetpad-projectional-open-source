package jetbrains.jetpad.hybrid.parser.prettyprint;

public interface TokenProperties {
  <ValueT> void set(TokenProperty<ValueT> prop, ValueT val);
}
