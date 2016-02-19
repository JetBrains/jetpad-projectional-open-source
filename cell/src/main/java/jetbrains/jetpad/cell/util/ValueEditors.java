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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import jetbrains.jetpad.base.Enums;
import jetbrains.jetpad.base.Runnables;
import jetbrains.jetpad.base.Validators;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.Completion;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.trait.DerivedCellTrait;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.values.Color;

import java.util.ArrayList;
import java.util.List;

import static jetbrains.jetpad.model.property.Properties.validatedProperty;

public class ValueEditors {

  public static <EnumT extends Enum<EnumT>> Property<EnumT> enumProperty(final TextCell textCell, final Class<EnumT> cls) {
    return enumProperty(textCell, cls, Color.BLACK);
  }

  public static <EnumT extends Enum<EnumT>> Property<EnumT> enumProperty(final TextCell textCell, final Class<EnumT> cls, final Color color) {
    class MyEnumValidator implements Predicate<String> {
      @Override
      public boolean apply(String input) {
        if (Strings.isNullOrEmpty(input)) return true;
        try {
          Enums.valueOf(cls, input);
          return true;
        } catch (IllegalArgumentException e) {
          return false;
        }
      }
    }

    textCell.addTrait(new DerivedCellTrait() {
      @Override
      protected CellTrait getBase(Cell cell) {
        return TextEditing.validTextEditing(new MyEnumValidator(), color);
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return new CompletionSupplier() {
            @Override
            public List<CompletionItem> get(CompletionParameters cp) {
              List<CompletionItem> result = new ArrayList<>();
              for (final EnumT v : cls.getEnumConstants()) {
                result.add(new SimpleCompletionItem(v.toString()) {
                  @Override
                  public Runnable complete(String text) {
                    textCell.text().set(v.toString());
                    return CellActions.toEnd(textCell);
                  }
                });
              }
              return result;
            }
          };
        }

        if (spec == TextEditing.CLEAR_ON_DELETE) {
          return true;
        }

        return super.get(cell, spec);
      }
    });

    return Properties.map(validatedProperty(textCell.text(), new MyEnumValidator()), new Function<String, EnumT>() {
        @Override
        public EnumT apply(String s) {
          return Strings.isNullOrEmpty(s) ? null : Enums.valueOf(cls, s);
        }
      }, new Function<EnumT, String>() {
        @Override
        public String apply(EnumT enumT) {
          return enumT == null ? null : "" + enumT;
        }
      }
    );
  }

  public static Property<Integer> intProperty(TextCell textCell) {
    textCell.addTrait(TextEditing.validTextEditing(Validators.unsignedInteger(), Color.BLUE));
    Property<String> validated = validatedProperty(textCell.text(), Validators.unsignedInteger());

    return Properties.map(validated, new Function<String, Integer>() {
        @Override
        public Integer apply(String s) {
          return s == null ? null : Integer.parseInt(s);
        }
      }, new Function<Integer, String>() {
        @Override
        public String apply(Integer integer) {
          return integer == null ? null : "" + integer;
        }
      }
    );
  }

  public static Property<Boolean> booleanProperty(TextCell textView) {
    return booleanProperty(textView, true);
  }

  public static Property<Boolean> booleanProperty(final TextCell textCell, final boolean completion) {
    final Color color = textCell.textColor().get();
    textCell.addTrait(new DerivedCellTrait() {
      @Override
      protected CellTrait getBase(Cell cell) {
        return TextEditing.validTextEditing(Validators.bool(), color);
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return new CompletionSupplier() {
            @Override
            public List<CompletionItem> get(CompletionParameters cp) {
              List<CompletionItem> result = new ArrayList<>();
              result.add(new SimpleCompletionItem("true") {
                @Override
                public Runnable complete(String text) {
                  textCell.text().set("true");
                  return Runnables.EMPTY;
                }
              });
              result.add(new SimpleCompletionItem("false") {
                @Override
                public Runnable complete(String text) {
                  textCell.text().set("false");
                  return Runnables.EMPTY;
                }
              });

              return result;
            }
          };
        }

        return super.get(cell, spec);
      }
    });

    return Properties.map(textCell.text(), new Function<String, Boolean>() {
        @Override
        public Boolean apply(String s) {
          return s == null ? null : Boolean.parseBoolean(s);
        }
      }, new Function<Boolean, String>() {
        @Override
        public String apply(Boolean bool) {
          return "" + bool;
        }
      }
    );
  }
}