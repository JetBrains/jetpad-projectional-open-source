/*
 * Copyright 2012-2014 JetBrains s.r.o
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

import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.hybrid.parser.ErrorToken;
import jetbrains.jetpad.hybrid.parser.SimpleToken;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.util.CellLists;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.projectional.util.CellNavigationController;
import jetbrains.jetpad.values.Color;

class TextTokenCell extends TextCell {
  private HybridSynchronizer<?> mySync;
  private boolean myFirst;
  private Token myToken;
  private Token myNextToken;

  TextTokenCell(HybridSynchronizer<?> sync, Token token) {
    mySync = sync;
    myToken = token;

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
    return new TokenCellTraits.TokenCellTrait(false) {
      @Override
      protected CellTrait[] getBaseTraits(Cell cell) {
        return new CellTrait[] {
          new TokenCellTraits.LeftLeafTokenCellTrait(),
          new TokenCellTraits.RightLeafTokenCellTrait(),
          TextEditing.validTextEditing(myToken instanceof ErrorToken ? Predicates.<String>alwaysFalse() : Predicates.equalTo(myToken.text()), tokenTextColor(), false)
        };
      }

      @Override
      public Object get(final Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == TextEditing.FIRST_ALLOWED) return !noSpaceToLeft();
        if (spec == TextEditing.LAST_ALLOWED) return !noSpaceToRight();
        if (spec == TextEditing.DOT_LIKE_RT) return myToken.isDotLike();
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

        return super.get(cell, spec);
      }

      @Override
      protected void provideProperties(Cell cell, PropertyCollector collector) {
        collector.add(CellNavigationController.PAIR_CELL, mySync.getPair(TextTokenCell.this));
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
}