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

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitEventSpec;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.event.*;

class TokenTextEditPostProcessorTrait<SourceT> extends CellTrait {
  private final BaseHybridSynchronizer<SourceT, ?> mySync;
  private final TokensEditPostProcessor<SourceT> myPostProcessor;

  TokenTextEditPostProcessorTrait(BaseHybridSynchronizer<SourceT, ?> sync, TokensEditPostProcessor<SourceT> postProcessor) {
    mySync = sync;
    myPostProcessor = postProcessor;
  }

  @Override
  public void onKeyTyped(Cell cell, KeyEvent event) {
    afterTokensEdit();
  }

  @Override
  public void onKeyPressed(Cell cell, KeyEvent event) {
    if (isRemove(event)) {
      afterTokensEdit();
    }
  }

  @Override
  public void onKeyPressedLowPriority(Cell cell, KeyEvent event) {
    if (isRemove(event)) {
      afterTokensEdit();
    }
  }

  @Override
  public void onCut(Cell cell, CopyCutEvent event) {
    if (event.getResult() != null
        && event.getResult().isSupported(ContentKinds.SINGLE_LINE_TEXT)
        && !TextContentHelper.getText(event.getResult()).isEmpty()) {
      afterTokensEdit();
    }
  }

  @Override
  public void onPaste(Cell cell, PasteEvent event) {
    if (event.getContent().isSupported(ContentKinds.SINGLE_LINE_TEXT)
        && !TextContentHelper.getText(event.getContent()).isEmpty()) {
      afterTokensEdit();
    }
  }

  @Override
  public void onCellTraitEvent(Cell cell, CellTraitEventSpec<?> spec, Event event) {
    if (spec == Cells.AFTER_COMPLETED) {
      afterTokenCompleted();
    }
  }

  private boolean isRemove(KeyEvent event) {
    return event.getKey() == Key.BACKSPACE || event.getKey() == Key.DELETE;
  }

  private void afterTokensEdit() {
    myPostProcessor.afterTokensEdit(mySync.tokens(), mySync.property().get());
  }

  private void afterTokenCompleted() {
    myPostProcessor.afterTokenCompleted(mySync.tokens(), mySync.property().get());
  }
}
