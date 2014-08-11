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
package jetbrains.jetpad.hybrid;

import com.google.common.base.Function;
import jetbrains.jetpad.hybrid.parser.Parser;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.prettyprint.PrettyPrinter;
import jetbrains.jetpad.completion.CompletionSupplier;

public interface HybridPositionSpec<SourceT> {
  Parser<SourceT> getParser();
  PrettyPrinter<? super SourceT> getPrettyPrinter();

  PairFinder getPairFinder();

  CompletionSupplier getTokenCompletion(Function<Token, Runnable> tokenHandler);
  CompletionSupplier getAdditionalCompletion(CompletionContext ctx, Completer completer);
}