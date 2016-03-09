package jetbrains.jetpad.hybrid;

import com.google.common.collect.ImmutableList;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.event.ClipboardContent;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.values.Color;
import org.junit.Test;

import static jetbrains.jetpad.hybrid.TokensUtil.*;
import static org.junit.Assert.assertEquals;

public class HybridEditorCopyPasteTest extends BaseHybridEditorEditingTestCase {
  @Test
  public void copyPasteToken() {
    setTokens(Tokens.ID, Tokens.PLUS);
    select(0, true);
    press(Key.RIGHT, ModifierKey.SHIFT);

    ClipboardContent clipboardContent = copy();
    assertEquals("id", clipboardContent.toString());

    press(KeyStrokeSpecs.PASTE);
    assertTokens(Tokens.ID, Tokens.ID, Tokens.PLUS);
  }

  @Test
  public void cutToken() {
    setTokens(Tokens.ID, Tokens.PLUS);
    select(0, true);
    press(Key.RIGHT, ModifierKey.SHIFT);

    ClipboardContent clipboardContent = cut();
    assertEquals("id", clipboardContent.toString());

    assertTokens(Tokens.PLUS);

  }

  @Test
  public void cutPasteToken() {
    setTokens(Tokens.ID, Tokens.PLUS);
    select(0, true);
    press(Key.RIGHT, ModifierKey.SHIFT);

    ClipboardContent clipboardContent = cut();
    assertEquals("id", clipboardContent.toString());

    press(KeyStrokeSpecs.PASTE);
    press(KeyStrokeSpecs.PASTE);
    assertTokens(Tokens.ID, Tokens.ID, Tokens.PLUS);
  }

  @Test
  public void pasteTokenToEmpty() {
    setTokens(Tokens.ID);
    select(0, true);
    press(Key.RIGHT, ModifierKey.SHIFT);

    ClipboardContent clipboardContent = cut();
    assertEquals("id", clipboardContent.toString());

    press(KeyStrokeSpecs.PASTE);
    assertTokens(Tokens.ID);
  }

  @Test
  public void copySimpleExpr() {
    type("10+25");
    selectLeft(3);
    ClipboardContent clipboardContent = copy();
    assertEquals("10 + 25", clipboardContent.toString());
  }

  @Test
  public void pasteSimpleExprAsText() {
    paste("10+25");
    assertTokens(integer(10), Tokens.PLUS, integer(25));
    TextCell text = (TextCell) myCellContainer.focusedCell.get();
    assertEquals("25", text.text().get());
    assertEquals(2, (int) text.caretPosition().get());
  }

  @Test
  public void pasteIncorrectText() {
    paste("10+bad");
    assertTokens(integer(10), Tokens.PLUS, error("bad"));
    TextCell text = (TextCell) myCellContainer.focusedCell.get();
    assertEquals("bad", text.text().get());
    assertEquals(3, (int) text.caretPosition().get());
    assertEquals(Color.RED, text.textColor().get());
  }

  @Test
  public void pasteToNonempty() {
    type("id");
    paste("+ 10");
    assertTokens(Tokens.ID, Tokens.PLUS, integer(10));
  }

  @Test
  public void copyStringLiteralWithOtherTokens() {
    String text = "10+'text 1";   // Closing quote autocompletes
    type(text);
    right();
    selectLeft(text.length() + 1);
    ClipboardContent clipboardContent = copy();
    assertEquals("10 + 'text 1'", clipboardContent.toString());
  }

  @Test
  public void pasteStringLiteralWithOtherTokens() {
    paste("\"text 1\" + 10");
    assertTokensEqual(ImmutableList.of(doubleQtd("text 1"), Tokens.PLUS, integer(10)), sync.tokens());
  }

  private void selectLeft(int steps) {
    for (int i = 0; i < steps; i++) {
      press(Key.LEFT, ModifierKey.SHIFT);
    }
  }
}
