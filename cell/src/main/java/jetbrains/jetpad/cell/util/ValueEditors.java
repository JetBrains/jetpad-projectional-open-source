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
package jetbrains.jetpad.cell.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import jetbrains.jetpad.cell.action.Runnables;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.*;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.values.Color;

import java.util.ArrayList;
import java.util.List;

import static jetbrains.jetpad.model.property.Properties.validatedProperty;

public class ValueEditors {

  public static <EnumT extends Enum<EnumT>> Property<EnumT> enumProperty(final TextCell textView, final Class<EnumT> cls) {
    return enumProperty(textView, cls, Color.BLACK);
  }


  public static <EnumT extends Enum<EnumT>> Property<EnumT> enumProperty(final TextCell textView, final Class<EnumT> cls, final Color color) {
    class MyEnumValidator implements Predicate<String> {
      @Override
      public boolean apply(String input) {
        if (input == null) return true;
        try {
          Enum.valueOf(cls, input);
          return true;
        } catch (IllegalArgumentException e) {
          return false;
        }
      }
    }

    textView.addTrait(new BaseCellTrait() {
      @Override
      protected CellTrait[] getBaseTraits(Cell cell) {
        return new CellTrait[] { TextEditing.validTextEditing(new MyEnumValidator(), color) };
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return new CompletionSupplier() {
            @Override
            public List<CompletionItem> get(CompletionParameters cp) {
              List<CompletionItem> result = new ArrayList<CompletionItem>();
              for (final EnumT v : cls.getEnumConstants()) {
                result.add(new SimpleCompletionItem(v.toString()) {
                  @Override
                  public Runnable complete(String text) {
                    textView.text().set(v.toString());
                    return CellActions.toEnd(textView);
                  }
                });
              }
              return result;
            }
          };
        }

        return super.get(cell, spec);
      }
    });

    return Properties.transform(validatedProperty(textView.text(), new MyEnumValidator()), new Function<String, EnumT>() {
        @Override
        public EnumT apply(String s) {
          return s == null ? null : Enum.valueOf(cls, s);
        }
      }, new Function<EnumT, String>() {
        @Override
        public String apply(EnumT enumT) {
          return enumT == null ? null : "" + enumT;
        }
      }
    );
  }

  public static Property<Integer> intProperty(TextCell textView) {
    textView.addTrait(TextEditing.validTextEditing(Validators.integer(), Color.BLUE));
    Property<String> validated = validatedProperty(textView.text(), Validators.integer());

    return Properties.transform(validated, new Function<String, Integer>() {
      @Override
      public Integer apply(String s) {
        return s == null ? null : Integer.parseInt(s);
      }
    }, new Function<Integer, String>() {
      @Override
      public String apply(Integer integer) {
        return integer == null ? null : "" + integer;
      }
    });
  }

  public static Property<Boolean> booleanProperty(TextCell textView) {
    return booleanProperty(textView, true);
  }

  public static Property<Boolean> booleanProperty(final TextCell textView, final boolean completion) {
    final Color color = textView.textColor().get();
    textView.addTrait(new BaseCellTrait() {
      @Override
      protected CellTrait[] getBaseTraits(Cell cell) {
        return new CellTrait[] { TextEditing.validTextEditing(Validators.bool(), color) };
      }

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == Completion.COMPLETION) {
          return new CompletionSupplier() {
            @Override
            public List<CompletionItem> get(CompletionParameters cp) {
              List<CompletionItem> result = new ArrayList<CompletionItem>();
              result.add(new SimpleCompletionItem("true") {
                @Override
                public Runnable complete(String text) {
                  textView.text().set("true");
                  return Runnables.EMPTY;
                }
              });
              result.add(new SimpleCompletionItem("false") {
                @Override
                public Runnable complete(String text) {
                  textView.text().set("false");
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

    return Properties.transform(textView.text(), new Function<String, Boolean>() {
      @Override
      public Boolean apply(String s) {
        return s == null ? null : Boolean.parseBoolean(s);
      }
    }, new Function<Boolean, String>() {
      @Override
      public String apply(Boolean bool) {
        return "" + bool;
      }
    });
  }
}