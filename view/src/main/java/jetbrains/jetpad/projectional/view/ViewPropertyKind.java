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
package jetbrains.jetpad.projectional.view;

public enum ViewPropertyKind {
  NONE() {
    @Override
    void invalidate(View view) {
    }
  },

  REPAINT() {
    @Override
    void invalidate(View view) {
      view.repaint();
    }
  },

  RELAYOUT() {
    @Override
    void invalidate(View view) {
      view.invalidate();
    }
  },


  RELAYOUT_AND_REPAINT() {
    @Override
    void invalidate(View view) {
      view.repaint();
      view.invalidate();
    }
  },

  RELAYOUT_PARENT() {
    @Override
    void invalidate(View view) {
      if (view.parent().get() == null) return;
      view.parent().get().invalidate();
    }
  };

  abstract void invalidate(View view);
}