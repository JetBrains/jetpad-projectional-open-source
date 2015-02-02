/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.demo.concept.mapper;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.indent.IndentCell;

import static jetbrains.jetpad.cell.util.CellFactory.*;

class ConceptDeclarationCell extends IndentCell {
  final TextCell name;
  final Cell members;
  final TextCell isAbstractText;

  ConceptDeclarationCell() {
    to(this,
      keyword("Concept"), space(), name = new TextCell(), placeHolder(name, "<no name>"),
      newLine(),
      space(),
      indent(true,
        newLine(),
        keyword("Properties"),
        indent(true,
          newLine(),
          label("isAbstract"), space(), isAbstractText = new TextCell(), placeHolder(isAbstractText, "<no abstract>")
        )),
      newLine(),
      space(),
      indent(true,
        newLine(),
        keyword("Members"),
        indent(true,
          newLine(),
          members = indent()
        )
      )
    );
  }
}