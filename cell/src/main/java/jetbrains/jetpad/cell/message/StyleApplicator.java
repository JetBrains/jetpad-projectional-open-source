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
package jetbrains.jetpad.cell.message;

import jetbrains.jetpad.base.Disposable;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;

import java.util.HashMap;
import java.util.Map;

class StyleApplicator {
  private final MessageStyler myStyler;
  private Map<Cell, StyleRegistrations> myRegistrations = null;

  StyleApplicator(MessageStyler styler) {
    myStyler = styler == null ? new MessageStyler() : styler;
  }

  void applyInfo(Cell cell, boolean info) {
    if (info) {
      get(cell).myInfo = myStyler.doApplyInfo(cell);
    } else {
      StyleRegistrations registrations = getIfExists(cell);
      if (registrations == null) return;
      registrations.myInfo.remove();
      registrations.myInfo = null;
      releaseIfEmpty(cell, registrations);
    }
  }

  void applyBroken(Cell cell, boolean broken) {
    if (broken) {
      get(cell).myBroken = myStyler.doApplyBroken(cell);
    } else {
      StyleRegistrations registrations = getIfExists(cell);
      if (registrations == null) return;
      registrations.myBroken.remove();
      registrations.myBroken = null;
      releaseIfEmpty(cell, registrations);
    }
  }

  void applyError(Cell cell, boolean error) {
    if (error) {
      get(cell).myError = myStyler.doApplyError(cell);
    } else {
      StyleRegistrations registrations = getIfExists(cell);
      if (registrations == null) return;
      registrations.myError.remove();
      registrations.myError = null;
      releaseIfEmpty(cell, registrations);
    }
  }

  void applyWarning(Cell cell, boolean warning) {
    if (warning) {
      get(cell).myWarning = myStyler.doApplyWarning(cell);
    } else {
      StyleRegistrations registrations = getIfExists(cell);
      if (registrations == null) return;
      registrations.myWarning.remove();
      registrations.myWarning = null;
      releaseIfEmpty(cell, registrations);
    }
  }

  private StyleRegistrations get(Cell cell) {
    if (myRegistrations == null) {
      myRegistrations = new HashMap<>();
    }
    if (!myRegistrations.containsKey(cell)) {
      myRegistrations.put(cell, new StyleRegistrations());
    }
    return myRegistrations.get(cell);
  }

  private StyleRegistrations getIfExists(Cell cell) {
    return myRegistrations == null ? null : myRegistrations.get(cell);
  }

  private void releaseIfEmpty(Cell cell, StyleRegistrations registrations) {
    if (!registrations.isEmpty()) return;
    myRegistrations.remove(cell);
    if (myRegistrations.isEmpty()) {
      myRegistrations = null;
    }
  }

  void detach(Cell cell) {
    if (myRegistrations == null) return;
    StyleRegistrations registrations = myRegistrations.remove(cell);
    if (registrations == null) return;
    if (myRegistrations.isEmpty()) {
      myRegistrations = null;
    }
    registrations.dispose();
  }

  private static class StyleRegistrations implements Disposable {
    private Registration myInfo = null;
    private Registration myBroken = null;
    private Registration myError = null;
    private Registration myWarning = null;

    boolean isEmpty() {
      return myInfo == null && myBroken == null && myError == null && myWarning == null;
    }

    @Override
    public void dispose() {
      if (myInfo != null) {
        myInfo.remove();
      }
      if (myBroken != null) {
        myBroken.remove();
      }
      if (myError != null) {
        myError.remove();
      }
      if (myWarning != null) {
        myWarning.remove();
      }
    }
  }
}