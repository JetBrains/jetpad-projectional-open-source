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
package jetbrains.jetpad.projectional.demo.nanoLang.mapper;

import com.google.common.base.Predicate;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.projectional.demo.nanoLang.model.LambdaExpression;

class LambdaExpressionMapper extends Mapper<LambdaExpression, LambdaExpressionCell> {
  LambdaExpressionMapper(LambdaExpression source) {
    super(source, new LambdaExpressionCell());
    
    getTarget().varName.addTrait(TextEditing.validTextEditing(new Predicate<String>() {
      @Override
      public boolean apply(String s) {
        return s.length() == 1 && s.charAt(0) != ' ';
      }
    }));
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);
    
    conf.add(Synchronizers.forProperties(getSource().argumentName, getTarget().varName.text()));
    conf.add(NanoLangSynchronizers.expressionSynchronizer(this, getSource().body, getTarget().bodyCell));
  }
}