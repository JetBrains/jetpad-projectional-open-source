package jetbrains.jetpad.hybrid.parser.prettyprint;

public interface TokenMetaData {
  <ValueT> ValueT get(TokenProperty<ValueT> prop);
}
