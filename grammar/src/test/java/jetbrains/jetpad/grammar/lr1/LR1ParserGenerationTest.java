package jetbrains.jetpad.grammar.lr1;

import jetbrains.jetpad.grammar.BaseParserGenerationTest;
import jetbrains.jetpad.grammar.Grammar;
import jetbrains.jetpad.grammar.lr.LRTable;

public class LR1ParserGenerationTest extends BaseParserGenerationTest {
  @Override
  protected LRTable generateTable(Grammar g) {
    return new LR1TableGenerator(g).generateTable();
  }
}
