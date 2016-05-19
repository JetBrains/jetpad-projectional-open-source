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
package jetbrains.jetpad.cell.util;

import jetbrains.jetpad.base.Disposable;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.edt.EventDispatchThread;

public class DebouncedCommand implements Disposable, Runnable {
  private final EventDispatchThread myEdt;
  private final Runnable myCommand;
  private final int myDelay;
  private Registration myRegistration = null;

  public DebouncedCommand(int delay, EventDispatchThread edt, final Runnable command) {
    myEdt = edt;
    myDelay = delay;
    myCommand = new Runnable() {
      @Override
      public void run() {
        removeRegistration();
        command.run();
      }
    };
  }

  public void run() {
    removeRegistration();
    myRegistration = myEdt.schedule(myDelay, myCommand);
  }

  @Override
  public void dispose() {
    removeRegistration();
  }

  private void removeRegistration() {
    if (myRegistration != null) {
      myRegistration.remove();
    }
  }
}
