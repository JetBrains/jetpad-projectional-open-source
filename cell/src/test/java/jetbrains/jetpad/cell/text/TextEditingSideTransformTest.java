package jetbrains.jetpad.cell.text;

import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionTestCase;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TextEditingSideTransformTest extends CompletionTestCase {



  @Test
  public void nonMenuCompletionItemWithSideTransform() {
    TextCell cell = new TextCell();

    cell.set(Completion.COMPLETION, new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        List<CompletionItem> result = new ArrayList<CompletionItem>();
        if (!cp.isMenu()) {
          result.add(new SimpleCompletionItem("a") {
            @Override
            public Runnable complete(String text) {
              return Runnables.EMPTY;
            }
          });
        }
        return result;
      }
    });
    cell.set(Completion.RIGHT_TRANSFORM, createCompletion("z"));
    cell.set(TextEditing.RT_ON_END, true);
    cell.text().set("a");
    cell.addTrait(TextEditing.textEditing());
    myCellContainer.root.children().add(cell);

    cell.focus();

    cell.caretPosition().set(1);

    complete();
    enter();

    assertCompleted("z");
  }

}
