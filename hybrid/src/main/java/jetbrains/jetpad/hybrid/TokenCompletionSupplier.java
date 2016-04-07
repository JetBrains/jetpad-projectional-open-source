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

import com.google.common.base.Function;
import jetbrains.jetpad.completion.ByBoundsCompletionItem;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.hybrid.parser.CommentToken;
import jetbrains.jetpad.hybrid.parser.Token;

public abstract class TokenCompletionSupplier extends CompletionSupplier {

  private final Function<Token, Runnable> myTokenHandler;
  private final String myCommentPrefix;

  protected TokenCompletionSupplier(CommentSpec commentSpec, Function<Token, Runnable> tokenHandler) {
    myCommentPrefix = commentSpec.getCommentPrefix();
    myTokenHandler = tokenHandler;
  }

  protected final CompletionItem getCommentCompletionItem() {
    return new ByBoundsCompletionItem(myCommentPrefix) {
      @Override
      public Runnable complete(String text) {
        return myTokenHandler.apply(new CommentToken(myCommentPrefix, getBody(text)));
      }
    };
  }

}
