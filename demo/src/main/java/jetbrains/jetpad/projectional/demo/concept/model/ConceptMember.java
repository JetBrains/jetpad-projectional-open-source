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
package jetbrains.jetpad.projectional.demo.concept.model;

import jetbrains.jetpad.model.children.SimpleComposite;

public abstract class ConceptMember extends SimpleComposite<ConceptDeclaration, ConceptMember> {

  public ConceptMember copy() {
    if (this instanceof EmptyMember) {
      return new EmptyMember();
    }

    if (this instanceof NamedMember) {
      NamedMember result;
      if (this instanceof PropertyMember) {
        result = new PropertyMember();
      } else if (this instanceof ChildMember) {
        result = new ChildMember();
      } else if (this instanceof ReferenceMember) {
        result = new ReferenceMember();
      } else {
        throw new IllegalStateException();
      }

      result.name.set(((NamedMember) this).name.get());
      return result;
    }

    return null;
  }

}