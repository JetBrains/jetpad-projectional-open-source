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
package jetbrains.jetpad.cell.toDom;

import com.google.gwt.resources.client.CssResource;

public interface CellToDomCss extends CssResource {
  String cell();
  String vertical();
  String rootContainer();
  String horizontal();
  String indented();
  String hidden();
  String outlined();
  String selected();
  String paired();
  String domCell();
  String hasShadow();

  String lineHighlight();
  String content();

  String fitContentWidth();

  String decorations();

  String hasWarning();
  String hasError();

  String currentHighlightColor();
  String redUnderline();
  String yellowUnderline();
  String blueUnderline();
  String link();

  String popup();

  String tooltip();
  String tooltipBottom();
  String tooltipTop();
}