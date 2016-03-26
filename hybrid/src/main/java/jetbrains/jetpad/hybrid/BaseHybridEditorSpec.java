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

import jetbrains.jetpad.hybrid.parser.Token;

import java.util.List;

public abstract class BaseHybridEditorSpec<SourceT> implements HybridEditorSpec<SourceT> {

  protected BaseHybridEditorSpec() {
  }

  @Override
  public CommentSpec getCommentSpec() {
    return CommentSpec.EMPTY;
  }

  protected final SourceT parse(List<Token> input) {
    return getParser().parse(HybridEditorSpecUtil.getParsingContextFactory(this).getParsingContext(input));
  }

}
