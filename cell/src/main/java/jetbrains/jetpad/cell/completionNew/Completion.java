package jetbrains.jetpad.cell.completionNew;

import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.completion.CompletionFragment;

import java.util.Collections;
import java.util.List;

public class Completion {
  public static final CellTraitPropertySpec<List<CompletionFragment>> COMPLETION = new CellTraitPropertySpec<>("completion", Collections.<CompletionFragment>emptyList());
  public static final CellTraitPropertySpec<List<CompletionFragment>> LEFT_TRANSFORM = new CellTraitPropertySpec<>("leftTransform", Collections.<CompletionFragment>emptyList());
  public static final CellTraitPropertySpec<List<CompletionFragment>> RIGHT_TRANSFORM = new CellTraitPropertySpec<>("rightTransform", Collections.<CompletionFragment>emptyList());

}

