/*
 * Copyright 2012-2016 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.cell.text;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.base.edt.TestEventDispatchThread;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainerEdtUtil;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.CellCompletionController;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionConfig;
import jetbrains.jetpad.cell.completion.CompletionItems;
import jetbrains.jetpad.cell.completion.CompletionTestCase;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.trait.DerivedCellTrait;
import jetbrains.jetpad.completion.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class ValidTextCompletionTest extends CompletionTestCase {
  private TextCell text = new TextCell();
  private boolean lowPriorityCompleted;
  private boolean rtCompleted;
  private boolean bulkCompleted;
  private TestEventDispatchThread edt = new TestEventDispatchThread();

  @Before
  public void init() {
    CellContainerEdtUtil.resetEdt(myCellContainer, edt);
    myCellContainer.root.children().add(text);

    text.text().set("");
    text.focusable().set(true);

    text.addTrait(new DerivedCellTrait() {
      @Override
      protected CellTrait getBase(Cell cell) {
        return
          TextEditing.validTextEditing(
            new Predicate<String>() {
              @Override
              public boolean apply(String input) {
                return "".equals(input) || "u".equals(input) || "qaz".equals(input);
              }
            });
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return new CompletionSupplier() {
            @Override
            public List<CompletionItem> get(CompletionParameters cp) {
              class LowPriorityCompletionItem extends SimpleCompletionItem {
                LowPriorityCompletionItem(String matchingText) {
                  super(matchingText);
                }

                @Override
                public int getMatchPriority() {
                  return super.getMatchPriority() - 1;
                }

                @Override
                public Runnable complete(String text) {
                  lowPriorityCompleted = true;
                  return Runnables.EMPTY;
                }
              }

              class ActuallySetTextToCompletionItem extends SetTextToCompletionItem {
                public ActuallySetTextToCompletionItem(String match, String completion) {
                  super(match, completion);
                }

                @Override
                public Runnable complete(String text) {
                  ValidTextCompletionTest.this.text.text().set(myCompletion);
                  return super.complete(text);
                }
              }

              class BulkCompletionItem extends BaseCompletionItem {
                private final CompletionItems items;
                private final Splitter splitter = Splitter.on(' ');

                private BulkCompletionItem(Iterable<CompletionItem> simpleItems) {
                  this.items = new CompletionItems(simpleItems);
                }

                @Override
                public String visibleText(String text) {
                  throw new IllegalStateException("Must not be visible");
                }

                @Override
                public int getMatchPriority() {
                  return super.getMatchPriority() - 1;
                }

                @Override
                public boolean isStrictMatchPrefix(String text) {
                  if (text.isEmpty()) {
                    return true;
                  }
                  for (Iterator<String> i = splitter.split(text).iterator(); ; ) {
                    String part = i.next();
                    if (i.hasNext()) {
                      if (!items.hasSingleMatch(part, false)) {
                        return false;
                      }
                    } else {
                      return items.strictlyPrefixedBy(part).size() > 0;
                    }
                  }
                }

                @Override
                public boolean isMatch(String text) {
                  for (String part : splitter.split(text)) {
                    if (!items.hasSingleMatch(part, false)) {
                      return false;
                    }
                  }
                  return true;
                }

                @Override
                public Runnable complete(final String text) {
                  return new Runnable() {
                    @Override
                    public void run() {
                      for (String part : splitter.split(text)) {
                        items.completeFirstMatch(part);
                      }
                      bulkCompleted = true;
                    }
                  };
                }
              }

              final List<CompletionItem> simpleItems = new ArrayList<>();
              simpleItems.addAll(FluentIterable.from(createCompletion("a", "c", "ae", "zz", "d", "u", "q", "p", "ppp").get(cp)).toList());
              simpleItems.add(new LowPriorityCompletionItem("d"));
              simpleItems.add(new LowPriorityCompletionItem("xx"));
              simpleItems.add(new LowPriorityCompletionItem("qq"));
              simpleItems.add(new LowPriorityCompletionItem("v"));
              simpleItems.add(new LowPriorityCompletionItem("va"));
              simpleItems.add(new SetTextToCompletionItem("var"));
              simpleItems.add(new SetTextToCompletionItem("foobar"));
              simpleItems.add(new ActuallySetTextToCompletionItem("foo", "qaz"));
              List<CompletionItem> result = new ArrayList<>(simpleItems);
              if (cp.isBulkCompletionRequired()) {
                result.add(new BulkCompletionItem(simpleItems));
              }
              return result;
            }
          };
        }

        if (spec == Completion.RIGHT_TRANSFORM) {
          return new CompletionSupplier() {
            @Override
            public List<CompletionItem> get(CompletionParameters cp) {
              List<CompletionItem> result = new ArrayList<>();
              result.add(new SimpleCompletionItem("z") {
                @Override
                public Runnable complete(String text) {
                  rtCompleted = true;
                  return Runnables.EMPTY;
                }
              });
              return result;
            }
          };
        }

        if (spec == TextEditing.RT_ON_END) {
          return true;
        }

        if (spec == Completion.COMPLETION_CONFIG) {
          return new CompletionConfig() {
            @Override
            public boolean canDoRightTransform(CompletionItems completionItems, String prefixText) {
              List<CompletionItem> matches = completionItems.matches(prefixText);
              return matches.size() == 1;
            }
          };
        }

        return super.get(cell, spec);
      }
    });

    myCellContainer.focusedCell.set(text);
  }

  @Test
  public void simpleCompletion() {
    type('c');
    edt.executeUpdates(TextEditing.AFTER_TYPE_DELAY);
    assertCompleted("c");
  }

  @Test
  public void asyncCompletion() {
    text.addTrait(createAsyncCompletionTrait("fff", "yyyy"));

    complete();
    type("fff");

    enter();
    assertCompleted("fff");
  }

  @Test
  public void completionConflict() {
    type('a');
    assertNotCompleted();
  }

  @Test
  public void completionConflictResolutionWithEnter() {
    type('a');
    enter();

    assertCompleted("a");
  }

  @Test
  public void completeImmediatelyInCaseOfSingleCase() {
    type("z");
    complete();

    assertCompleted("zz");
  }

  @Test
  public void completionPopup() {
    complete();
    enter();

    assertCompleted("a");
  }

  @Test
  public void completionPopupNavigationDown() {
    complete();
    down();
    enter();

    assertCompleted("ae");
  }

  @Test
  public void completionPopupNavigationUp() {
    complete();
    down();
    down();
    up();
    enter();

    assertCompleted("ae");
  }

  @Test
  public void escapeDismissesCompletion() {
    complete();
    assertCompletionActive();
    escape();
    assertCompletionInactive();
  }

  @Test
  public void focusLossDismissesCompletion() {
    complete();
    assertCompletionActive();
    myCellContainer.focusedCell.set(null);
    assertCompletionInactive();
  }

  @Test
  public void focusLostThenGainedKeepsValidCompletionState() {
    focusLossDismissesCompletion();
    text.focus();
    complete();
    assertCompletionActive();
  }

  @Test
  public void itemRemoveLeadsToCompletionDismiss() {
    complete();
    assertCompletionActive();
    text.removeFromParent();
    assertCompletionInactive();
  }

  @Test
  public void completionShouldntBeShownTwice() {
    complete();

    Cell popup = text.bottomPopup().get();

    complete();

    assertSame(popup, text.bottomPopup().get());
  }

  @Test
  public void nonEagerCompletionDoesntComplete() {
    type("a");

    assertNotCompleted();
  }

  @Test
  public void eagerCompletionCompletes() {
    text.addTrait(new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == TextEditing.EAGER_COMPLETION) {
          return true;
        }

        return super.get(cell, spec);
      }
    });

    type("a");
    edt.executeUpdates(TextEditing.AFTER_TYPE_DELAY);

    assertCompleted("a");
  }

  @Test
  public void lowPriorityIsBeatenByHighPriority() {
    type("d");
    edt.executeUpdates(TextEditing.AFTER_TYPE_DELAY);
    assertFalse(lowPriorityCompleted);
    assertCompleted("d");
  }


  @Test
  public void lowPriorityWorksIfThereNoHighPriority() {
    type("xx");
    edt.executeUpdates(TextEditing.AFTER_TYPE_DELAY);
    assertTrue(lowPriorityCompleted);
    assertNotCompleted();
  }

  @Test
  public void lowPriorityPrefixAndStrictNormalPriorityDontConflict() {
    type("q");
    edt.executeUpdates(TextEditing.AFTER_TYPE_DELAY);
    assertFalse(lowPriorityCompleted);
    assertCompleted("q");
  }


  @Test
  public void rightTransformOnEnd() {
    type("xx");

    complete();
    enter();

    assertTrue(rtCompleted);
  }

  @Test
  public void rightTransformOnEnd_strictlyPrefixedNotEmpty() {
    type("p");

    complete();
    enter();

    assertTrue(rtCompleted);
  }

  @Test
  public void cancellationOfRtOnEnd() {
    type("xx");

    Cell focused = myCellContainer.focusedCell.get();

    complete();
    escape();

    assertFocused(focused);
  }

  @Test
  public void completeOnSpaceOnEnd() {
    // Tests that the following line does not throw
    type("foo ");
    edt.executeUpdates(TextEditing.AFTER_TYPE_DELAY);
    assertCompleted("qaz");
  }

  @Test
  public void lowPriorityIsntCompletedIf() {
    type("var");
    edt.executeUpdates(TextEditing.AFTER_TYPE_DELAY);
    assertFalse(lowPriorityCompleted);
    assertCompleted("var");
  }

  @Test
  public void bulkCompletionOnPaste() {
    paste("d u q");
    assertCompleted("d", "u", "q");
    assertTrue(bulkCompleted);
  }

  @Test
  public void simpleCompletionHasHigherPriority() {
    paste("zz");
    assertCompleted("zz");
    assertFalse(bulkCompleted);
  }

  private void assertCompletionActive() {
    assertHasBottomPopup(text);
    assertTrue(CellCompletionController.isCompletionActive(text));
  }

  private void assertCompletionInactive() {
    assertNoBottomPopup(text);
    assertFalse(CellCompletionController.isCompletionActive(text));
  }
}