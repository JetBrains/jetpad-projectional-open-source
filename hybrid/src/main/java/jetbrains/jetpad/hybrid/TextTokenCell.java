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

import com.google.common.base.Supplier;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.util.CellLists;
import jetbrains.jetpad.hybrid.parser.SimpleToken;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;
import jetbrains.jetpad.projectional.util.CellNavigationController;
import jetbrains.jetpad.values.Color;

class TextTokenCell extends TextCell {
  private BaseHybridSynchronizer<?, ?> mySync;
  private boolean myFirst;
  private Token myToken;
  private Token myNextToken;
  private final TokensEditPostProcessor<?> myPostProcessor;

  TextTokenCell(BaseHybridSynchronizer<?, ?> sync, Token token, TokensEditPostProcessor<?> postProcessor) {
    mySync = sync;
    myToken = token;
    myPostProcessor = postProcessor;

    textColor().set(tokenTextColor());
    bold().set(token instanceof SimpleToken && ((SimpleToken) token).isBold());
    text().set(token.text());

    addTrait(createTrait());
    updateFocusability();
  }

  Token getToken() {
    return myToken;
  }

  private Color tokenTextColor() {
    return myToken instanceof SimpleToken ? ((SimpleToken) myToken).getColor() : Color.BLACK;
  }

  public void setFirst(boolean first) {
    myFirst = first;
    updateFocusability();
  }

  public void setNextToken(Token token) {
    myNextToken = token;
    updateFocusability();
  }

  private void updateFocusability() {
    int posCount = myToken.text().length() + 1;
    if (noSpaceToLeft()) posCount--;
    if (noSpaceToRight()) posCount--;
    focusable().set(posCount > 0);
  }

  private CellTrait createTrait() {
    final CellTrait[] baseTraits = (myPostProcessor == null)
        ? new CellTrait[] {
            new TokenCellTraits.LeftLeafTokenCellTrait(),
            new TokenCellTraits.RightLeafTokenCellTrait(),
            TextEditing.validTextEditing(myToken.getValidator(), tokenTextColor(), false)
        }
        : new CellTrait[] {
            new TokenCellTraits.LeftLeafTokenCellTrait(),
            new TokenCellTraits.RightLeafTokenCellTrait(),
            TextEditing.validTextEditing(myToken.getValidator(), tokenTextColor(), false),
            new TokensEditPostProcessorTrait(mySync, myPostProcessor)
        };

    return new TokenCellTraits.TokenCellTrait(false) {
      @Override
      protected CellTrait[] getBaseTraits(Cell cell) {
        return baseTraits;
      }

      @Override
      public Object get(final Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == TextEditing.FIRST_ALLOWED) return !noSpaceToLeft();
        if (spec == TextEditing.LAST_ALLOWED) return !noSpaceToRight();
        if (spec == TextEditing.RT_ON_END) return myToken.isRtOnEnd();
        if (spec == TextEditing.EAGER_COMPLETION) return true;

        if (myToken.noSpaceToLeft() || myToken.noSpaceToRight()) {
          if (spec == ProjectionalSynchronizers.DELETE_ON_EMPTY) return true;
          if (spec == CellLists.NO_SPACE_TO_LEFT) return noSpaceToLeft();
          if (spec == CellLists.NO_SPACE_TO_RIGHT) return noSpaceToRight();
        }

        if (spec == TextEditing.AFTER_TYPE) {
          return new Supplier<Boolean>() {
            @Override
            public Boolean get() {
              return tokenOperations(cell).afterType(TextTokenCell.this);
            }
          };
        }

        if (spec == TextEditing.AFTER_PASTE) {
          return new Supplier<Boolean>() {
            @Override
            public Boolean get() {
              return tokenOperations(cell).afterPaste(TextTokenCell.this);
            }
          };
        }

        return super.get(cell, spec);
      }

      @Override
      protected void provideProperties(Cell cell, PropertyCollector collector) {
        collector.add(CellNavigationController.PAIR_CELL, new Supplier<Cell>() {
          @Override
          public Cell get() {
            return mySync.getPair(TextTokenCell.this);
          }
        });
        super.provideProperties(cell, collector);
      }
    };
  }

  boolean noSpaceToRight() {
    return myToken.noSpaceToRight() && myNextToken != null && !myNextToken.noSpaceToLeft();
  }

  boolean noSpaceToLeft() {
    return myToken.noSpaceToLeft() && !myFirst;
  }

  @Override
  public String toString() {
    return "TextTokenCell('" + text().get() + "')@" + Integer.toHexString(hashCode());
  }
}