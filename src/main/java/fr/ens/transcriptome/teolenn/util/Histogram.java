/*
 *                      Nividic development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the microarray platform
 * of the École Normale Supérieure and the individual authors.
 * These should be listed in @author doc comments.
 *
 * For more information on the Nividic project and its aims,
 * or to join the Nividic mailing list, visit the home page
 * at:
 *
 *      http://www.transcriptome.ens.fr/nividic
 *
 */

package fr.ens.transcriptome.teolenn.util;

public class Histogram {

  private double min;
  private double max;
  private int bins;
  private double step;
  private int count;
  private int[] array;

  /**
   * Public constructor.
   * @param min minimum value
   * @param max maximum value
   * @param bins number of bins
   */
  public Histogram(final double min, final double max, final int bins) {

    if (bins <= 0)
      throw new RuntimeException("Invalid parameter: bins=" + bins);

    this.min = Math.min(min, max);
    this.max = Math.max(min, max);
    this.step = (max - min) / bins;

    this.array = new int[bins + 1];
  }

  /**
   * Add a value to the histogram.
   * @param value Value to add
   */
  public void addValue(final double value) {

    if (value < this.min || value >= this.max)
      this.array[bins]++;
    else
      this.array[(int) ((value - this.min) / this.step)]++;

    this.count++;
  }

  /**
   * Get the histogram of the values.
   * @return an array of double
   */
  public double[] getHistogram() {

    final double[] result = new double[array.length];

    for (int i = 0; i < array.length; i++)
      result[i] = (double) array[i] / (double) count;

    return result;
  }

  /**
   * Clear data.
   */
  public void clear() {

    this.count = 0;
    for (int i = 0; i < this.array.length; i++)
      this.array[i] = 0;

  }

}
