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
package jetbrains.jetpad.event;

public class CopyCutEvent extends Event {
  private boolean myCut;
  private ClipboardContent myResult;

  public CopyCutEvent(boolean cut) {
    myCut = cut;
  }

  public boolean isCut() {
    return myCut;
  }

  public ClipboardContent getResult() {
    return myResult;
  }

  public void consume(ClipboardContent content) {
    consume();
    myResult = content;
  }
}