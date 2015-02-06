/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.event;

import java.util.Objects;

public abstract class Either<L, R> {

  public static <L, R> Either<L, R> left(L left) {
    return new Left<>(left);
  }

  public static <L, R> Either<L, R> right(R right) {
    return new Right<>(right);
  }

  public abstract boolean isLeft();

  public boolean isRight() {
    return !isLeft();
  }

  public abstract L getLeft();
  public abstract R getRight();

  private static class Left<L, R> extends Either<L, R> {
    private L myLeft;

    private Left(L left) {
      myLeft = left;
    }

    @Override
    public boolean isLeft() {
      return true;
    }

    @Override
    public L getLeft() {
      return myLeft;
    }

    @Override
    public R getRight() {
      throw new UnsupportedOperationException();
    }


    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Left)) return false;
      return Objects.equals(myLeft, ((Left) obj).myLeft);
    }

    @Override
    public int hashCode() {
      if (myLeft == null) return 3;
      return 239 * myLeft.hashCode();
    }

    @Override
    public String toString() {
      return "L(" + myLeft + ")";
    }
  }

  private static class Right<L, R> extends Either<L, R> {
    private R myRight;

    private Right(R right) {
      myRight = right;
    }

    @Override
    public boolean isLeft() {
      return false;
    }

    @Override
    public L getLeft() {
      throw new UnsupportedOperationException();
    }

    @Override
    public R getRight() {
      return myRight;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Right)) return false;
      return Objects.equals(myRight, ((Right) obj).myRight);
    }

    @Override
    public int hashCode() {
      if (myRight == null) return 1;
      return 31 * myRight.hashCode();
    }

    @Override
    public String toString() {
      return "R(" + myRight + ")";
    }
  }
}
