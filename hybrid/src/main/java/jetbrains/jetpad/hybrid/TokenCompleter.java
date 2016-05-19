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

import com.google.common.base.Function;
import jetbrains.jetpad.base.Objects;
import com.google.common.collect.FluentIterable;
import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.Asyncs;
import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.completion.CompletionItems;
import jetbrains.jetpad.completion.CompletionController;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.hybrid.parser.CommentToken;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.collections.list.ObservableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static jetbrains.jetpad.base.Runnables.seq;
import static jetbrains.jetpad.hybrid.SelectionPosition.FIRST;
import static jetbrains.jetpad.hybrid.SelectionPosition.LAST;

class TokenCompleter {

  private static boolean isComment(Cell targetCell) {
    return targetCell instanceof TextTokenCell && ((TextTokenCell) targetCell).getToken() instanceof CommentToken;
  }

  private static int getCommentPrefixLength(Cell targetCell) {
    TextTokenCell textTokenCell = (TextTokenCell) targetCell;
    CommentToken commentToken = (CommentToken) textTokenCell.getToken();
    return commentToken.getPrefix().length();
  }

  private BaseHybridSynchronizer<?, ?> mySync;

  TokenCompleter(BaseHybridSynchronizer<?, ?> sync) {
    mySync = sync;
  }

  private SimpleHybridEditorSpec<?> getEditorSpec() {
    return mySync.editorSpec();
  }

  private TokenListEditor<?> getTokenListEditor() {
    return mySync.tokenListEditor();
  }

  private TokenOperations<?> getTokenOperations() {
    return mySync.tokenOperations();
  }

  CompletionItems completion(Function<Token, Runnable> handler) {
    return new CompletionItems(getEditorSpec().getTokenCompletion(handler).get(CompletionParameters.EMPTY));
  }

  CompletionSupplier placeholderCompletion(final Cell placeholder) {
    return tokenCompletion(new PlaceholderCompletionContext(), new BaseCompleter() {
      @Override
      public Runnable complete(int selectionIndex, Token... tokens) {
        CompletionController controller = placeholder.get(Completion.COMPLETION_CONTROLLER);
        boolean wasActive = controller.isActive();

        getTokenListEditor().tokens.addAll(Arrays.asList(tokens));
        getTokenListEditor().updateToPrintedTokens();

        Runnable result = getTokenOperations().selectOnCreation(selectionIndex, LAST);
        if (wasActive) {
          result = seq(result, activateCompletion(selectionIndex));
        }

        return result;
      }
    });
  }

  CompletionSupplier tokenCompletion(final Cell tokenCell) {
    final int index = mySync.tokenCells().indexOf(tokenCell);
    return tokenCompletion(new TokenCompletionContext(index), new BaseCompleter() {
      @Override
      public Runnable complete(int selectionIndex, Token... tokens) {
        final int caretPosition;
        String oldText = null;
        SelectionPosition position = LAST;
        if (tokenCell instanceof TextCell) {
          TextCell cell = (TextCell) tokenCell;
          caretPosition = cell.caretPosition().get();
          oldText = cell.text().get();
          if (caretPosition == 0) {
            position = FIRST;
          } else if (caretPosition == cell.text().get().length()) {
            position = LAST;
          } else {
            position = null;
          }
        } else {
          caretPosition = -1;
        }

        CompletionController controller = tokenCell.get(Completion.COMPLETION_CONTROLLER);
        final boolean wasCompletionActive = controller != null && controller.isActive();

        TokenListEditor<?> tokenListEditor = getTokenListEditor();

        if (tokens.length == 0) {
          tokenListEditor.tokens.remove(index);
        } else {
          int sourceIndex = 0;
          int targetIndex = index;
          tokenListEditor.tokens.set(targetIndex++, tokens[sourceIndex++]);
          while (sourceIndex < tokens.length) {
            tokenListEditor.tokens.add(targetIndex++, tokens[sourceIndex++]);
          }
        }

        tokenListEditor.processComments();
        tokenListEditor.updateToPrintedTokens();

        final Cell targetCell =  mySync.tokenCells().get(index + selectionIndex);
        if (!(targetCell instanceof TextCell) || !Objects.equal(((TextCell) targetCell).text().get(), oldText)) {
          if (isComment(targetCell)) {
            position = null;
          } else {
            position = LAST;
          }
        }

        Runnable result;
        if (position == null) {
          result = new Runnable() {
            @Override
            public void run() {
              targetCell.focus();
              ((TextCell) targetCell).caretPosition().set(caretPosition);
            }
          };
        } else {
          result = mySync.tokenOperations().selectOnCreation(index + selectionIndex, position);
        }

        if (wasCompletionActive) {
          result = seq(result, activateCompletion(index + selectionIndex));
        }
        return result;
      }
    });
  }

  CompletionSupplier sideTransform(Cell tokenCell, final int delta) {
    final int index = mySync.tokenCells().indexOf(tokenCell);

    return new CompletionSupplier() {

      private Completer createCompleter(final CompletionParameters cp) {
        return new BaseCompleter() {
          @Override
          public Runnable complete(int selectionIndex, Token... tokens) {
            int i = index + delta;
            TokenListEditor<?> tokenListEditor = getTokenListEditor();
            ObservableList<Token> editorTokenList = tokenListEditor.tokens;
            if (i < editorTokenList.size() && tokens.length >= 1 && tokens[0] instanceof ValueToken && editorTokenList.get(i) instanceof ValueToken) {
              editorTokenList.remove(i);
            }
            for (Token t : tokens) {
              editorTokenList.add(i++, t);
            }
            tokenListEditor.processComments();
            tokenListEditor.updateToPrintedTokens();
            Runnable result;
            int targetIndex = index + delta + selectionIndex;
            final Cell targetCell =  mySync.tokenCells().get(targetIndex);
            if (isComment(targetCell)) {
              result = new Runnable() {
                @Override
                public void run() {
                  targetCell.focus();
                  ((TextCell) targetCell).caretPosition().set(getCommentPrefixLength(targetCell));
                }
              };
            } else {
              result = getTokenOperations().selectOnCreation(targetIndex, LAST);
            }
            if (cp.isEndRightTransform() && !cp.isMenu()) {
              result = seq(result, activateCompletion(targetIndex));
            }
            return result;
          }
        };
      }

      private TokenCompletionContext createContext(CompletionParameters cp) {
        if (cp.isEndRightTransform()) {
          return new TokenCompletionContext(index + 1);
        }

        return new TokenCompletionContext(index + delta);
      }

      @Override
      public Iterable<CompletionItem> get(final CompletionParameters cp) {
        return tokenCompletion(createContext(cp), createCompleter(cp)).get(cp);
      }

      @Override
      public Async<? extends Iterable<CompletionItem>> getAsync(CompletionParameters cp) {
        if (cp.isMenu()) {
          return getEditorSpec().getAdditionalCompletion(createContext(cp), createCompleter(cp)).getAsync(cp);
        }
        return Asyncs.<Iterable<CompletionItem>>constant(new ArrayList<CompletionItem>());
      }
    };
  }

  private CompletionSupplier tokenCompletion(final CompletionContext ctx, final Completer completer) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        List<CompletionItem> result = new ArrayList<>();
        if (!(cp.isMenu() && mySync.isHideTokensInMenu())) {
          result.addAll(FluentIterable.from(getEditorSpec().getTokenCompletion(new Function<Token, Runnable>() {
            @Override
            public Runnable apply(Token input) {
              return completer.complete(input);
            }
          }).get(cp)).toList());
        }
        if (cp.isMenu()) {
          result.addAll(FluentIterable.from(getEditorSpec().getAdditionalCompletion(ctx, completer).get(cp)).toList());
          ctx.getPrefix();
        }
        return result;
      }

      @Override
      public Async<? extends Iterable<CompletionItem>> getAsync(CompletionParameters cp) {
        if (cp.isMenu()) {
          return getEditorSpec().getAdditionalCompletion(ctx, completer).getAsync(cp);
        }
        return Asyncs.<Iterable<CompletionItem>>constant(new ArrayList<CompletionItem>());
      }
    };
  }

  Token completeToken(String text) {
    final Value<Token> result = new Value<>();
    CompletionItems completion = completion(new Function<Token, Runnable>() {
      @Override
      public Runnable apply(Token token) {
        result.set(token);
        return Runnables.EMPTY;
      }
    });
    List<CompletionItem> matches = completion.matches(text);
    if (matches.size() == 1) {
      matches.get(0).complete(text);
      return result.get();
    }
    return null;
  }

  private Runnable activateCompletion(final int index) {
    return new Runnable() {
      @Override
      public void run() {
        CompletionController ctrl = mySync.tokenCells().get(index).get(Completion.COMPLETION_CONTROLLER);
        if (ctrl.hasAmbiguousMatches()) {
          ctrl.activate();
        }
      }
    };
  }

  private class PlaceholderCompletionContext implements CompletionContext {
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
      return mySync.contextMapper();
    }

    @Override
    public Object getTarget() {
      return null;
    }
  }

  private class TokenCompletionContext implements CompletionContext {
    private int myTargetIndex;

    private TokenCompletionContext(int targetIndex) {
      myTargetIndex = targetIndex;
    }

    @Override
    public int getTargetIndex() {
      return myTargetIndex;
    }

    @Override
    public List<Token> getPrefix() {
      return Collections.unmodifiableList(getTokenListEditor().tokens.subList(0, myTargetIndex));
    }

    @Override
    public List<Cell> getViews() {
      return Collections.unmodifiableList(mySync.tokenCells());
    }

    @Override
    public List<Token> getTokens() {
      return Collections.unmodifiableList(getTokenListEditor().tokens);
    }

    @Override
    public List<Object> getObjects() {
      return Collections.unmodifiableList(getTokenListEditor().getObjects());
    }

    @Override
    public Mapper<?, ?> getContextMapper() {
      return mySync.contextMapper();
    }

    @Override
    public Object getTarget() {
      return mySync.property().get();
    }
  }
}
