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
package jetbrains.jetpad.cell.completion;

import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionMenuModel;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.mapper.*;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.WritableProperty;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.values.Color;

class CompletionMenu {
  static Cell createView(CompletionMenuModel model, Handler<CompletionItem> completer) {
    CompletionMenuModelMapper mapper = new CompletionMenuModelMapper(model, completer);
    mapper.attachRoot();
    return mapper.getTarget();
  }

  private static class CompletionMenuModelMapper extends Mapper<CompletionMenuModel, ScrollCell> {
    private VerticalCell myVerticalCell;
    private Handler<CompletionItem> myCompleter;

    private CompletionMenuModelMapper(CompletionMenuModel source, Handler<CompletionItem> completer) {
      super(source, new ScrollCell());

      myCompleter = completer;

      myVerticalCell = new VerticalCell();
      getTarget().children().add(myVerticalCell);

      myVerticalCell.background().set(Color.VERY_LIGHT_GRAY);
      getTarget().maxDimension().set(new Vector(600, 200));
      getTarget().scroll().set(true);
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);

      conf.add(Synchronizers.forObservableRole(
        this,
        getSource().visibleItems,
        myVerticalCell.children(),
        new MapperFactory<CompletionItem, Cell>() {
          @Override
          public Mapper<? extends CompletionItem, ? extends Cell> createMapper(CompletionItem source) {
            return new CompletionItemMapper(source);
          }
        }));
    }
  }

  private static class CompletionItemMapper extends Mapper<CompletionItem, HorizontalCell> {
    private TextCell myText;

    private CompletionItemMapper(CompletionItem source) {
      super(source, new HorizontalCell());
      getTarget().children().add(myText = new TextCell());
      getTarget().addTrait(new BaseCellTrait() {
        @Override
        public void onMousePressed(Cell cell, MouseEvent event) {
          CompletionMenuModelMapper parentMapper = (CompletionMenuModelMapper) getParent();
          if (parentMapper.getSource().selectedItem.get() == getSource()) {
            parentMapper.myCompleter.handle(getSource());
          } else {
            parentMapper.getSource().selectedItem.set(getSource());
          }
          event.consume();
        }
      });
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);
      final ReadableProperty<String> text = ((CompletionMenuModelMapper) getParent()).getSource().text;

      conf.add(Synchronizers.forProperty(Properties.ifProp(new DerivedProperty<Boolean>(text) {
        @Override
        public Boolean get() {
          return getSource().isMatch(text.get());
        }
      }, Color.BLUE, Color.BLACK), myText.textColor()));

      conf.add(Synchronizers.forProperty(new DerivedProperty<String>() {
        @Override
        public String get() {
          return getSource().visibleText(text.get());
        }

        @Override
        public String getPropExpr() {
          return "visibleText(" + getSource() + ")";
        }
      }, myText.text()));

      final ReadableProperty<CompletionItem> selectedItem = ((CompletionMenuModelMapper) getParent()).getSource().selectedItem;

      conf.add(Synchronizers.forProperty(
        Properties.same(selectedItem, getSource()),
        new WritableProperty<Boolean>() {
          @Override
          public void set(Boolean value) {
            if (value == null) {
              value = Boolean.FALSE;
            }
            getTarget().background().set(value ? Color.LIGHT_CYAN : null);
            if (value && getTarget().isAttached()) {
              getTarget().scrollTo();
            }
          }
        }));
    }
  }
}