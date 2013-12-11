/*
 * Copyright 2012-2013 JetBrains s.r.o
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

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import jetbrains.jetpad.event.ContentKinds;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.cell.action.CellAction;
import jetbrains.jetpad.projectional.cell.completion.CompletionItem;
import jetbrains.jetpad.projectional.cell.completion.CompletionParameters;
import jetbrains.jetpad.projectional.cell.completion.SimpleCompletionItem;
import jetbrains.jetpad.projectional.cell.text.TextEditing;
import jetbrains.jetpad.projectional.cell.util.CellLists;
import jetbrains.jetpad.projectional.cell.util.ValueEditors;
import jetbrains.jetpad.projectional.demo.concept.model.*;
import jetbrains.jetpad.projectional.cell.Cell;
import jetbrains.jetpad.projectional.cell.support.*;
import jetbrains.jetpad.projectional.util.Validators;

import java.util.ArrayList;
import java.util.List;

class ConceptDeclarationMapper extends Mapper<ConceptDeclaration, ConceptDeclarationCell> {
  ConceptDeclarationMapper(ConceptDeclaration source) {
    super(source, new ConceptDeclarationCell());

    getTarget().name.addTrait(TextEditing.validTextEditing(Validators.identifier()));
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);

    conf.add(Synchronizers.forProperties(getSource().name, getTarget().name.text()));
    conf.add(Synchronizers.forProperties(getSource().isAbstract, ValueEditors.booleanProperty(getTarget().isAbstractText)));

    ProjectionalRoleSynchronizer<Object, ConceptMember> membersSync = ProjectionalSynchronizers.<Object, ConceptMember>forRole(
      this,
      getSource().members, getTarget().members,
      CellLists.newLineSeparated(getTarget().members.children()),
      new MapperFactory<ConceptMember, Cell>() {
      @Override
      public Mapper<? extends ConceptMember, ? extends Cell> createMapper(ConceptMember source) {
        if (source instanceof EmptyMember) {
          return new EmptyMemberMapper((EmptyMember) source);
        }

        if (source instanceof ChildMember) {
          return new NamedMemberMapper((NamedMember) source, "Child");
        }

        if (source instanceof PropertyMember) {
          return new NamedMemberMapper((NamedMember) source, "Property");
        }

        if (source instanceof ReferenceMember) {
          return new NamedMemberMapper((NamedMember) source, "Reference");
        }

        return null;
      }
    });
    membersSync.setItemFactory(new EmptyMemberSupplier());
    membersSync.setCompletion(new ConceptMemberCompletion());
    membersSync.setClipboardParameters(ContentKinds.<ConceptMember>create("member"),
    new Function<ConceptMember, ConceptMember>() {
      @Override
      public ConceptMember apply(ConceptMember input) {
        return input.copy();
      }
    });
    conf.add(membersSync);
  }

  static class EmptyMemberSupplier implements Supplier<ConceptMember> {
    @Override
    public ConceptMember get() {
      return new EmptyMember();
    }
  }

  static class ConceptMemberCompletion implements RoleCompletion<Object, ConceptMember> {
    @Override
    public List<CompletionItem> createRoleCompletion(CompletionParameters cp, Mapper<?, ? extends Cell> mapper, Object contextNode, final Role<ConceptMember> target) {
      List<CompletionItem> result = new ArrayList<CompletionItem>();
      result.add(new SimpleCompletionItem("Property") {
        @Override
        public CellAction complete(String text) {
          return target.set(new PropertyMember());
        }
      });
      result.add(new SimpleCompletionItem("Reference") {
        @Override
        public CellAction complete(String text) {
          return target.set(new ReferenceMember());
        }
      });
      result.add(new SimpleCompletionItem("Child") {
        @Override
        public CellAction complete(String text) {
          return target.set(new ChildMember());
        }
      });
      result.add(new SimpleCompletionItem("Empty") {
        @Override
        public CellAction complete(String text) {
          return target.set(new EmptyMember());
        }
      });
      return result;
    }
  }

}