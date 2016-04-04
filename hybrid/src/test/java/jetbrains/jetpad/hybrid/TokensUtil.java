package jetbrains.jetpad.hybrid;

import jetbrains.jetpad.hybrid.parser.ErrorToken;
import jetbrains.jetpad.hybrid.parser.IntValueToken;
import jetbrains.jetpad.hybrid.parser.Token;
import jetbrains.jetpad.hybrid.parser.ValueToken;
import jetbrains.jetpad.hybrid.testapp.mapper.ValueExprTextGen;
import jetbrains.jetpad.hybrid.testapp.mapper.ValueExprCloner;
import jetbrains.jetpad.hybrid.testapp.model.*;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class TokensUtil {
  private TokensUtil() {
  }

  static Token integer(int val) {
    return new IntValueToken(val);
  }

  static Token singleQtd(String body) {
    return new ValueToken(new StringExpr("'", body), new ValueExprCloner(), new ValueExprTextGen());
  }

  static Token doubleQtd(String body) {
    return new ValueToken(new StringExpr("\"", body), new ValueExprCloner(), new ValueExprTextGen());
  }

  static Token tripleQtd(String body) {
    return new ValueToken(new StringExpr("'''", body), new ValueExprCloner(), new ValueExprTextGen());
  }

  static Token complex() {
    return new ValueToken(new ComplexValueExpr(), new ValueExprCloner());
  }

  static Token pos() {
    return new ValueToken(new PosValueExpr(), new ValueExprCloner());
  }

  static Token value() {
    return new ValueToken(new ValueExpr(), new ValueExprCloner());
  }

  static Token async() {
    return new ValueToken(new AsyncValueExpr(), new ValueExprCloner());
  }
  static Token error(String text) {
    return new ErrorToken(text);
  }

  static void assertTokensEqual(List<Token> expected, List<Token> actual) {
    assertEquals(expected + " " + actual, expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      Token expectedToken = expected.get(i);
      Token actualToken = actual.get(i);
      if (expectedToken instanceof ValueToken) {
        assertTrue(actualToken instanceof ValueToken);
        assertValueTokensEqual((ValueToken) expectedToken, (ValueToken) actualToken);
      } else if (expectedToken instanceof ErrorToken) {
        assertTrue(actualToken instanceof ErrorToken);
        assertEquals(expectedToken.toString(), actualToken.toString());
      } else {
        assertEquals(expectedToken, actualToken);
      }
    }
  }

  private static void assertValueTokensEqual(ValueToken expected, ValueToken actual) {
    Object expectedValue = expected.value();
    Object actualValue = actual.value();
    if (expectedValue instanceof ValueExpr) {
      assertTrue(actualValue instanceof ValueExpr);
      assertEquals(((ValueExpr) expectedValue).val.get(), ((ValueExpr) actualValue).val.get());
    } else if (expectedValue instanceof ComplexValueExpr) {
      assertTrue(actualValue instanceof ComplexValueExpr);
    } else if (expectedValue instanceof PosValueExpr) {
      assertTrue(actualValue instanceof PosValueExpr);
    } else if (expectedValue instanceof StringExpr) {
      assertTrue(actualValue instanceof StringExpr);
      assertEquals(actualValue.toString(), expectedValue.toString());
    } else {
      assertEquals(expectedValue, actualValue);
    }
  }
}
