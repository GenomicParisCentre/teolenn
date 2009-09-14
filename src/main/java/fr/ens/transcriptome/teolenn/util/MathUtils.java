/*
 *                  Teolenn development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU General Public License version 2 or later. This
 * should be distributed with the code. If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/licenses/gpl-2.0.txt
 *
 * Copyright for this code is held jointly by the microarray platform
 * of the École Normale Supérieure and the individual authors.
 * These should be listed in @author doc comments.
 *
 * For more information on the Teolenn project and its aims,
 * or to join the Teolenn Google group, visit the home page
 * at:
 *
 *      http://www.transcriptome.ens.fr/teolenn
 *
 */

package fr.ens.transcriptome.teolenn.util;

import java.util.Collection;

import org.apache.commons.math.stat.descriptive.rank.Median;

/**
 * Math utility class
 * @author Laurent Jourdren
 */
public final class MathUtils {

  private static final int BASE_10 = 10;
  private static final int BASE_2 = 2;

  /**
   * Calculates the standard deviation of an array of numbers. see
   * http://davidmlane.com/hyperstat/A16252.html
   * @param data Numbers to compute the standard deviation of. Array must
   *            contain two or more numbers.
   * @return standard deviation estimate of population ( to get estimate of
   *         sample, use n instead of n-1 in last line )
   */
  public static double sdFast(final double[] data) {

    if (data == null)
      return Double.NaN;

    // sd is sqrt of sum of (values-mean) squared divided by n - 1
    // Calculate the mean
    double mean = 0;
    final int n = data.length;
    if (n < 2)
      return Double.NaN;
    for (int i = 0; i < n; i++) {
      mean += data[i];
    }
    mean /= n;
    // calculate the sum of squares
    double sum = 0;
    for (int i = 0; i < n; i++) {
      final double v = data[i] - mean;
      sum += v * v;
    }
    return java.lang.Math.sqrt(sum / (n - 1));
  }

  /**
   * Return the log 10 of a double.
   * @param d a double
   * @return the log 10 of a double
   */
  public static double log10(final double d) {
    return java.lang.Math.log(d) / java.lang.Math.log(BASE_10);
  }

  /**
   * Return the log 2 of a double.
   * @param d a double
   * @return the log 2 of a double
   */
  public static double log2(final double d) {
    return Math.log(d) / Math.log(BASE_2);
  }

  /**
   * Remove NaN from an array
   * @param data Array to use
   * @return an new array without NaN values
   */
  public static double[] removeNaN(final double[] data) {

    if (data == null)
      return null;

    int count = 0;

    for (int i = 0; i < data.length; i++)
      if (Double.isNaN(data[i]))
        count++;

    if (count == 0)
      return data;

    final double[] result = new double[count];
    count = 0;

    for (int i = 0; i < result.length; i++)
      if (!Double.isNaN(data[i]))
        result[count++] = data[i];

    return result;
  }

  /**
   * Calc the median of an array of double
   * @param data Data
   * @return the median or NaN if the data is null
   */
  public static double median(final double[] data) {

    return median(data, true);
  }

  /**
   * Calc the median of an array of double
   * @param data Data
   * @param noNaN true if NaN value must be removed from the computation
   * @return the median or NaN if the data is null
   */
  public static double median(final double[] data, final boolean noNaN) {

    if (data == null)
      return Double.NaN;

    Median median = new Median();
    return median.evaluate(noNaN ? removeNaN(data) : data);
  }

  /**
   * Calc the mean of an array of double
   * @param data Data
   * @param noNaN true if NaN value must be removed from the computation
   * @return the mean or NaN if the data is null
   */
  public static double mean(final double[] data, final boolean noNaN) {

    return mean(noNaN ? removeNaN(data) : data);
  }

  /**
   * Calc the mean of an array of double
   * @param data Data
   * @return the mean or NaN if the data is null
   */
  public static double mean(final double[] data) {

    if (data == null)
      return Double.NaN;

    int count = 0;
    float sum = 0;

    for (int i = 0; i < data.length; i++) {

      count++;
      sum += data[i];
    }

    return sum / count;
  }

  /**
   * Convert a collection of doubles to an array of doubles.
   * @param col collection to convert
   * @return a new array of double
   */
  public static double[] toArray(final Collection<Float> col) {

    if (col == null)
      return null;

    final double[] result = new double[col.size()];

    int i = 0;
    for (Float val : col)
      result[i++] = val;

    return result;
  }

  /**
   * Create an histogram from an array of doubles.
   * @param data data
   * @param min minimum value
   * @param max maximum value
   * @param bins number of bins
   * @return an array with the values of the histogram. The last value of the
   *         array contains the numbers outside the range
   */
  public static double[] histogram(final double[] data, final double min,
      final double max, final int bins) {

    if (data == null || bins <= 0)
      return null;

    final double step = (max - min) / bins;

    int array[] = new int[bins + 1];
    int count = 0;

    for (int i = 0; i < data.length; i++) {

      final double v = data[i];

      if (v < min || v >= max)
        array[bins]++;
      else
        array[(int) ((v - min) / step)]++;

      count++;
    }

    final double[] result = new double[array.length];

    for (int i = 0; i < array.length; i++)
      result[i] = (double) array[i] / (double) count;

    return result;
  }

  //
  // Constructor
  //

  /**
   * Private constructor.
   */
  private MathUtils() {
  }

}
