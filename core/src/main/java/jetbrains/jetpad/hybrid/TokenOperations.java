/*
 * Copyright 2012-2013 JetBrains s.r.o
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
import jetbrains.jetpad.cell.action.CellAction;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.CompletionHelper;
import jetbrains.jetpad.cell.completion.CompletionItem;
import jetbrains.jetpad.cell.util.CellLists;
import jetbrains.jetpad.hybrid.parser.ErrorToken;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.projectional.cell.*;

import java.util.List;

import static jetbrains.jetpad.hybrid.SelectionPosition.*;
import static jetbrains.jetpad.cell.action.CellActions.toFirstFocusable;
import static jetbrains.jetpad.cell.action.CellActions.toPosition;

class TokenOperations<SourceT> {
  private HybridSynchronizer<SourceT> mySync;

  TokenOperations(HybridSynchronizer<SourceT> sync) {
    mySync = sync;
  }

  private List<Cell> tokenViews() {
    return mySync.tokenCells();
  }

  private List<Token> tokens() {
    return mySync.tokens();
  }

  CellAction selectOnCreation(int index, SelectionPosition pos) {
    Cell cell = tokenViews().get(index);

    CellAction onCreate = cell.getRaw(ProjectionalSynchronizers.ON_CREATE);
    if (onCreate != null) {
      return onCreate;
    }

    return select(index, pos);
  }

  public CellAction select(int index, SelectionPosition pos) {
    final Cell cell = tokenViews().get(index);

    boolean noSpaceToLeft = cell.get(CellLists.NO_SPACE_TO_LEFT) || !cell.focusable().get();
    boolean noSpaceToRight = cell.get(CellLists.NO_SPACE_TO_RIGHT) || !cell.focusable().get();

    if (pos == FIRST) {
      if (!noSpaceToLeft || index == 0) {
        return CellActions.toFirstFocusable(cell);
      } else {
        return CellActions.toLastFocusable(tokenViews().get(index - 1));
      }
    } else if (pos == LAST) {
      if (!noSpaceToRight || index == tokenViews().size() - 1) {
        return CellActions.toLastFocusable(cell);
      } else {
        return CellActions.toFirstFocusable(tokenViews().get(index + 1));
      }
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public CellAction select(int index, int pos) {
    if (pos == 0) {
      return select(index, FIRST);
    }
    return toPosition(tokenViews().get(index), pos);
  }


  boolean canDelete(Cell contextCell, int delta) {
    final int index = tokenViews().indexOf(contextCell);

    if (index + delta < 0) {
      return false;
    } else if (index + delta >= tokens().size()) {
      return false;
    }
    return true;
  }

  CellAction deleteToken(Cell contextCell, final int delta) {
    final int index = tokenViews().indexOf(contextCell);
    tokens().remove(index + delta);

    if (tokens().isEmpty()) {
      return mySync.lastItemDeleted();
    }

    if (delta < 0) {
      return select(index - 1, FIRST);
    } else if (delta > 0) {
      return select(index, LAST);
    } else if (index > 0) {
      return select(index - 1, LAST);
    } else if (!tokens().isEmpty()) {
      return select(0, FIRST);
    } else {
      return toFirstFocusable(mySync.target());
    }
  }

  boolean canMerge(Cell contextCell, int delta) {
    final int index = tokenViews().indexOf(contextCell);

    if (tokens().get(index) instanceof ValueToken) {
      return false;
    }
    if (tokens().get(index + delta) instanceof ValueToken) {
      return false;
    }

    return true;
  }

  CellAction mergeTokens(Cell contextCell, boolean backward) {
    abstract class TokenHandler {
      abstract void handle(Token token);
    }

    final int index = tokenViews().indexOf(contextCell);
    Token token = tokens().get(index);

    final TokenHandler tokenHandler;
    Function<Token, CellAction> completer;
    final String newTokenText;
    final int pos;
    if (backward) {
      Token prevToken = tokens().get(index - 1);
      String prevText = prevToken.text();
      if (prevToken.noSpaceToRight() || token.noSpaceToLeft()) {
        prevText = prevText.substring(0, prevText.length() - 1);
      }
      pos = prevText.length();
      newTokenText = prevText + token.text();
      tokenHandler = new TokenHandler() {
        @Override
        public void handle(Token item) {
          tokens().remove(index);
          tokens().set(index - 1, item);
        }
      };
      completer = new Function<Token, CellAction>() {
        @Override
        public CellAction apply(Token token) {
          tokenHandler.handle(token);
          return select(index - 1, 0);
        }
      };
    } else {
      String currentText = token.text();
      pos = currentText.length();
      Token nextToken = tokens().get(index + 1);
      String nextText = nextToken.text();
      if (token.noSpaceToRight() || nextToken.noSpaceToLeft()) {
        nextText = nextText.substring(1);
      }
      newTokenText = currentText + nextText;
      tokenHandler = new TokenHandler() {
        @Override
        public void handle(Token item) {
          tokens().remove(index + 1);
          tokens().set(index, item);
        }
      };
      completer = new Function<Token, CellAction>() {
        @Override
        public CellAction apply(Token token) {
          tokenHandler.handle(token);
          return select(index, 0);
        }
      };
    }

    CompletionHelper completion = mySync.tokenCompletion().completion(completer);
    List<CompletionItem> matches = completion.matches(newTokenText);
    if (matches.size() == 1) {
      matches.get(0).complete(newTokenText);
    } else {
      tokenHandler.handle(new ErrorToken(newTokenText));
    }

    return select(backward ? index - 1 : index, pos);
  }

  CellAction expandToError(Cell tokenCell, String text, int delta) {
    int targetIndex = tokenViews().indexOf(tokenCell) + delta;
    tokens().add(targetIndex, new ErrorToken(text));
    return select(targetIndex, LAST);
  }

  boolean afterType(TextCell textView) {
    TokenCompletion tc = mySync.tokenCompletion();

    String text = textView.text().get();
    int caret = textView.caretPosition().get();
    char ch = text.charAt(caret - 1);
    if (ch == ' ') {
      String firstTokenText = text.substring(0, caret - 1);
      String secondTokenText = text.substring(caret);
      if (firstTokenText.length() > 0 && secondTokenText.length() > 0) {
        Token firstToken = tc.completeToken(firstTokenText);
        Token secondToken = tc.completeToken(secondTokenText);

        int index = tokenViews().indexOf(textView);

        tokens().set(index, firstToken != null ? firstToken : new ErrorToken(firstTokenText));
        tokens().add(index + 1, secondToken != null ? secondToken : new ErrorToken(secondTokenText));
        select(index + 1, FIRST).execute();
        return true;
      }
    }

    CompletionHelper completion = tc.completion(new Function<Token, CellAction>() {
      @Override
      public CellAction apply(Token token) {
        return CellAction.EMPTY;
      }
    });
    if (completion.isBoundary(text, caret - 1) && completion.isBoundary(text.substring(caret - 1), 1)) {
      Token first = tc.completeToken(text.substring(0, caret - 1));
      Token second = tc.completeToken(text.substring(caret - 1, caret));
      Token third = tc.completeToken(text.substring(caret));

      int index = tokenViews().indexOf(textView);
      tokens().remove(index);
      tokens().add(index, first);
      tokens().add(index + 1, second);
      tokens().add(index + 2, third);
      select(index + 1, LAST).execute();
      return true;
    }

    return false;
  }

}