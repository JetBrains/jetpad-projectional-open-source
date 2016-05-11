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
package jetbrains.jetpad.hybrid;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.trait.CellTraitEventSpec;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.completion.*;
import jetbrains.jetpad.event.Event;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.generic.Role;
import jetbrains.jetpad.projectional.generic.RoleCompletion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static jetbrains.jetpad.hybrid.SelectionPosition.LAST;

public class HybridWrapperRoleCompletion<ContainerT, WrapperT, TargetT> implements RoleCompletion<ContainerT, WrapperT> {
  private static boolean isNotBlank(String string) {
    return CharMatcher.whitespace().negate().matchesAnyOf(string);
  }

  private SimpleHybridEditorSpec<TargetT> mySpec;
  private Supplier<WrapperT> myFactory;
  private Function<Mapper<?, ?>, ? extends BaseHybridSynchronizer<TargetT, ?>> mySyncProvider;
  private boolean myHideTokensInMenu;


  public HybridWrapperRoleCompletion(SimpleHybridEditorSpec<TargetT> spec, Supplier<WrapperT> targetFactory,
      Function<Mapper<?, ?>, ? extends BaseHybridSynchronizer<TargetT, ?>> syncProvider) {
    this(spec, targetFactory, syncProvider, false);
  }

  public HybridWrapperRoleCompletion(SimpleHybridEditorSpec<TargetT> spec, Supplier<WrapperT> targetFactory,
      Function<Mapper<?, ?>, ? extends BaseHybridSynchronizer<TargetT, ?>> syncProvider,
      boolean hideTokensInMenus) {
    mySpec = spec;
    myFactory = targetFactory;
    mySyncProvider = syncProvider;
    myHideTokensInMenu = hideTokensInMenus;
  }

  @Override
  public CompletionSupplier createRoleCompletion(final Mapper<?, ?> mapper, ContainerT contextNode, final Role<WrapperT> target) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(final CompletionParameters cp) {
        List<CompletionItem> result = new ArrayList<>();

        final BaseCompleter completer = new BaseCompleter() {
          @Override
          public Runnable complete(int selectionIndex, Token... tokens) {
            WrapperT targetItem = myFactory.get();
            target.set(targetItem);
            Mapper<?, ?> targetItemMapper =  mapper.getDescendantMapper(targetItem);
            BaseHybridSynchronizer<?, ?> sync = mySyncProvider.apply(targetItemMapper);
            sync.setTokens(Arrays.asList(tokens));
            CellTraitEventSpec<Event> traitEvent = cp.isMenu() ? Cells.AFTER_COMPLETED : Cells.AFTER_EDITED;
            sync.getTargetList().iterator().next().dispatch(new Event(), traitEvent);
            return targetItemMapper.isAttached()
                ? sync.selectOnCreation(selectionIndex, LAST)
                : Runnables.EMPTY;
          }
        };

        if (cp.isBulkCompletionRequired()) {
          result.add(new BaseCompletionItem() {
            @Override
            public String visibleText(String text) {
              throw new IllegalStateException("This completion item must not be visible");
            }

            @Override
            public int getMatchPriority() {
              return super.getMatchPriority() - 1;
            }

            @Override
            public boolean isStrictMatchPrefix(String text) {
              return !isMatch(text);
            }

            @Override
            public boolean isMatch(String text) {
              return isNotBlank(text);
            }

            @Override
            public Runnable complete(final String text) {
              return new Runnable() {
                @Override
                public void run() {
                  CompletionTokenizer tokenizer = new CompletionTokenizer(mySpec);
                  List<Token> tokens = tokenizer.tokenize(text);
                  if (!tokens.isEmpty()) {
                    completer.complete(tokens.toArray(new Token[tokens.size()])).run();
                  }
                }
              };
            }
          });
        } else {
          if (!(cp.isMenu() && myHideTokensInMenu)) {
            for (final CompletionItem ci : mySpec.getTokenCompletion(new Function<Token, Runnable>() {
              @Override
              public Runnable apply(Token input) {
                return completer.complete(input);
              }
            }).get(cp)) {
              result.add(new WrapperCompletionItem(ci) {
                @Override
                public int getMatchPriority() {
                  return super.getMatchPriority() - 1;
                }
              });
            }
          }

          if (cp.isMenu()) {
            CompletionSupplier compl = mySpec.getAdditionalCompletion(new CompletionContext() {
              @Override
              public int getTargetIndex() {
                return 0;
              }

              @Override
              public List<Token> getPrefix() {
                return Collections.emptyList();
              }

              @Override
              public List<Cell> getViews() {
                return Collections.emptyList();
              }

              @Override
              public List<Token> getTokens() {
                return Collections.emptyList();
              }

              @Override
              public List<Object> getObjects() {
                return Collections.emptyList();
              }

              @Override
              public Mapper<?, ?> getContextMapper() {
                return mapper;
              }

              @Override
              public Object getTarget() {
                return target.get();
              }
            }, completer);
            result.addAll(FluentIterable.from(compl.get(new BaseCompletionParameters())).toList());
          }
        }
        return result;
      }
    };
  }
}
