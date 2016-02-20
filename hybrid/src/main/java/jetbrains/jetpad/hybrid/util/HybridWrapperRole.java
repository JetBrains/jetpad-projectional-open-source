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
package jetbrains.jetpad.hybrid.util;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.completion.*;
import jetbrains.jetpad.hybrid.BaseCompleter;
import jetbrains.jetpad.hybrid.CompletionContext;
import jetbrains.jetpad.hybrid.HybridEditorSpec;
import jetbrains.jetpad.hybrid.HybridSynchronizer;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.generic.Role;
import jetbrains.jetpad.projectional.generic.RoleCompletion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static jetbrains.jetpad.hybrid.SelectionPosition.LAST;

public class HybridWrapperRole<ContainerT, WrapperT, TargetT> implements RoleCompletion<ContainerT, WrapperT> {
  private HybridEditorSpec<TargetT> mySpec;
  private Supplier<WrapperT> myFactory;
  private Function<Mapper<?, ?>, HybridSynchronizer<TargetT>> mySyncProvider;
  private boolean myHideTokensInMenu;


  public HybridWrapperRole(HybridEditorSpec<TargetT> spec, Supplier<WrapperT> targetFactory, Function<Mapper<?, ?>, HybridSynchronizer<TargetT>> syncProvider) {
    this(spec, targetFactory, syncProvider, false);
  }

  public HybridWrapperRole(HybridEditorSpec<TargetT> spec, Supplier<WrapperT> targetFactory, Function<Mapper<?, ?>, HybridSynchronizer<TargetT>> syncProvider, boolean hideTokensInMenus) {
    mySpec = spec;
    myFactory = targetFactory;
    mySyncProvider = syncProvider;
    myHideTokensInMenu = hideTokensInMenus;
  }

  @Override
  public CompletionSupplier createRoleCompletion(final Mapper<?, ?> mapper, ContainerT contextNode, final Role<WrapperT> target) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        List<CompletionItem> result = new ArrayList<>();

        final BaseCompleter completer = new BaseCompleter() {
          @Override
          public Runnable complete(int selectionIndex, Token... tokens) {
            WrapperT targetItem = myFactory.get();
            target.set(targetItem);
            Mapper<?, ?> targetItemMapper =  mapper.getDescendantMapper(targetItem);
            HybridSynchronizer<?> sync = mySyncProvider.apply(targetItemMapper);
            sync.setTokens(Arrays.asList(tokens));
            return sync.selectOnCreation(selectionIndex, LAST);

          }
        };

        if (!(cp.isMenu() && myHideTokensInMenu)) {
          for (CompletionItem ci : mySpec.getTokenCompletion(new Function<Token, Runnable>() {
            @Override
            public Runnable apply(Token input) {
              return completer.complete(input);
            }
          }).get(cp)) {
            result.add(new WrapperCompletionItem(ci) {
              @Override
              public boolean isLowMatchPriority() {
                return true;
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
        return result;
      }
    };
  }
}