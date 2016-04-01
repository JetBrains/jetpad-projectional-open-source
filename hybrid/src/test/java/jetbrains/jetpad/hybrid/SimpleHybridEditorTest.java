package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.hybrid.testapp.mapper.SimpleExprContainerMapper;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.Expr;
import jetbrains.jetpad.hybrid.testapp.model.SimpleExprContainer;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class SimpleHybridEditorTest extends BaseHybridEditorTest<SimpleExprContainer, SimpleExprContainerMapper> {
  @Override
  protected SimpleExprContainer createContainer() {
    return new SimpleExprContainer();
  }

  @Override
  protected SimpleExprContainerMapper createMapper() {
    return new SimpleExprContainerMapper(container);
  }

  @Override
  protected BaseHybridSynchronizer<Expr, ?> getSync(SimpleExprContainerMapper mapper) {
    return mapper.hybridSync;
  }

  @Override
  protected Expr getExpr() {
    return container.expr.get();
  }

  @Test
  public void cellMapping() {
    initEditor();
    sync.tokens().addAll(Arrays.asList(Tokens.ID, Tokens.PLUS, Tokens.ID));

    assertEquals(Collections.singletonList(sync.tokenCells().get(0)), sync.getCells("0"));
    assertEquals(Collections.singletonList(sync.tokenCells().get(1)), sync.getCells("1"));
    assertEquals(Collections.singletonList(sync.tokenCells().get(2)), sync.getCells("2"));

    assertTrue(sync.getCells("3").isEmpty());

    for (Cell c : sync.tokenCells()) {
      assertNotNull(sync.getSource(c));
    }
  }

  @Test
  public void cellMappingForValues() {
    initEditor();
    sync.tokens().add(TokensUtil.singleQtd("test"));

    Cell valueTextCell = sync.tokenCells().get(0).children().get(1);
    assertEquals("0", sync.getSource(valueTextCell));
  }
}
