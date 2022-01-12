package hu.minhiriathaen.oqcp.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

public class NumberComparator<T extends Number> implements Comparator<T> {

  @Override
  public int compare(final Number number, final Number otherNumber) {
    if (isSpecial(number) || isSpecial(otherNumber)) {
      return Double.compare(number.doubleValue(), otherNumber.doubleValue());
    } else {
      return toBigDecimal(number).compareTo(toBigDecimal(otherNumber));
    }
  }

  private static boolean isSpecial(final Number number) {
    final boolean specialDouble =
        number instanceof Double
            && (Double.isNaN((Double) number) || Double.isInfinite((Double) number));
    final boolean specialFloat =
        number instanceof Float
            && (Float.isNaN((Float) number) || Float.isInfinite((Float) number));
    return specialDouble || specialFloat;
  }

  private static BigDecimal toBigDecimal(final Number number) {
    if (number instanceof BigDecimal) {
      return (BigDecimal) number;
    }
    if (number instanceof BigInteger) {
      return new BigDecimal((BigInteger) number);
    }
    if (number instanceof Byte
        || number instanceof Short
        || number instanceof Integer
        || number instanceof Long) {
      return new BigDecimal(number.longValue());
    }
    if (number instanceof Float || number instanceof Double) {
      return BigDecimal.valueOf(number.doubleValue());
    }

    try {
      return new BigDecimal(number.toString());
    } catch (final NumberFormatException e) {
      throw new RuntimeException(
          "The given number (\""
              + number
              + "\" of class "
              + number.getClass().getName()
              + ") does not have a parsable string representation",
          e);
    }
  }
}
