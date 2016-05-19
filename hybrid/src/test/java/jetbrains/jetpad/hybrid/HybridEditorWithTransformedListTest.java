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
package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.edt.TestEventDispatchThread;
import jetbrains.jetpad.cell.CellContainerEdtUtil;
import jetbrains.jetpad.cell.EditingTestCase;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.hybrid.testapp.mapper.ExprListMapper;
import jetbrains.jetpad.hybrid.testapp.mapper.Tokens;
import jetbrains.jetpad.hybrid.testapp.model.EmptyExpr;
import jetbrains.jetpad.hybrid.testapp.model.ExprList;
import jetbrains.jetpad.hybrid.testapp.model.IdExpr;
import jetbrains.jetpad.hybrid.testapp.model.NumberExpr;
import jetbrains.jetpad.hybrid.testapp.model.PlusExpr;
import jetbrains.jetpad.projectional.util.RootController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HybridEditorWithTransformedListTest extends EditingTestCase {
  private ExprList exprList = new ExprList();
  private Registration registration;
  private ExprListMapper rootMapper = new ExprListMapper(exprList);
  private final TestEventDispatchThread edt = new TestEventDispatchThread();

  @Before
  public void init() {
    exprList.expr.add(new EmptyExpr());
    exprList.expr.add(new EmptyExpr());
    registration = RootController.install(myCellContainer);
    rootMapper.attachRoot();
    myCellContainer.root.children().add(rootMapper.getTarget());
    CellActions.toFirstFocusable(rootMapper.getTarget()).run();
    CellContainerEdtUtil.resetEdt(myCellContainer, edt);
  }

  @After
  public void dispose() {
    rootMapper.detachRoot();
    registration.remove();
  }

  @Override
  protected void type(String s) {
    super.type(s);
    edt.executeUpdates(TextEditing.AFTER_TYPE_DELAY);
  }

  @Test
  public void typeCorrect() {
    type("id");
    down();
    type("1+2");
    assertTrue(exprList.expr.get(0) instanceof IdExpr);
    assertTrue(exprList.expr.get(1) instanceof PlusExpr);
  }

  @Test
  public void typeIncorrect() {
    type("id+");
    down();
    type("+");
    assertTrue(exprList.expr.get(0) instanceof IdExpr);
    assertEquals(asList(Tokens.ID, Tokens.PLUS), rootMapper.getItemsSynchronizers().get(0).tokens());
    assertTrue(exprList.expr.get(1) instanceof EmptyExpr);
    assertEquals(Collections.singletonList(Tokens.PLUS), rootMapper.getItemsSynchronizers().get(1).tokens());
  }

  @Test
  public void clearItemToEmpty() {
    type("0");
    down();
    type("1");
    backspace();
    assertEquals(Integer.valueOf(0), ((NumberExpr) exprList.expr.get(0)).value.get());
    assertTrue(exprList.expr.get(1) instanceof EmptyExpr);
  }

  @Test
  public void addItem() {
    type("0");
    down();
    type("1");
    enter();
    type("2");
    assertEquals(3, exprList.expr.size());
    for (int i = 0; i < 3; i++) {
      assertEquals(Integer.valueOf(i), ((NumberExpr) exprList.expr.get(i)).value.get());
    }
  }

  @Test
  public void removeItemWithDel() {
    type("0");
    down();
    type("1");
    up();
    del();
    assertEquals(1, exprList.expr.size());
    assertEquals(Integer.valueOf(0), ((NumberExpr) exprList.expr.get(0)).value.get());
  }

  @Test
  public void removeItemWithBackspace() {
    type("0");
    down();
    type("1");
    left();
    backspace();
    assertEquals(1, exprList.expr.size());
    assertEquals(Integer.valueOf(1), ((NumberExpr) exprList.expr.get(0)).value.get());
  }

  @Test
  public void removeTwoItems() {
    type("0");
    down();
    type("1");
    enter();
    type("2");
    left();
    press(Key.UP, ModifierKey.SHIFT);
    press(Key.UP, ModifierKey.SHIFT);
    del();
    assertEquals(1, exprList.expr.size());
    assertEquals(Integer.valueOf(2), ((NumberExpr) exprList.expr.get(0)).value.get());
  }
}