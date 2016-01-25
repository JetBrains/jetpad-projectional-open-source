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
package jetbrains.jetpad.projectional.demo.concept;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.demo.concept.mapper.ConceptMappers;
import jetbrains.jetpad.projectional.demo.concept.model.ConceptDeclaration;
import jetbrains.jetpad.projectional.demo.concept.model.PropertyMember;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.projectional.util.RootController;

public class ConceptDemo {
  public static CellContainer create() {
    ConceptDeclaration mainModel = createModel();

    Mapper<ConceptDeclaration, ? extends Cell> mapper = ConceptMappers.create(mainModel);
    mapper.attachRoot();

    CellContainer cellContainer = new CellContainer();
    cellContainer.root.children().add(mapper.getTarget());
    RootController.install(cellContainer);

    return cellContainer;
  }

  private static ConceptDeclaration createModel() {
    ConceptDeclaration result = new ConceptDeclaration();
    result.name.set("ConceptDeclaration");

    PropertyMember nameProp = new PropertyMember();
    nameProp.name.set("name");
    result.members.add(nameProp);

    PropertyMember isAbstractProp = new PropertyMember();
    isAbstractProp.name.set("isAbstract");
    result.members.add(isAbstractProp);

    return result;
  }
}