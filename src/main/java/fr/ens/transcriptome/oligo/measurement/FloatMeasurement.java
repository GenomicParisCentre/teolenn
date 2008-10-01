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

package fr.ens.transcriptome.oligo.measurement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fr.ens.transcriptome.oligo.Sequence;
import fr.ens.transcriptome.oligo.util.Histogram;
import fr.ens.transcriptome.oligo.util.MathUtils;

/**
 * This abstract class define a float measurement. It contains all the code
 * (e.g. the statistics) shared by this type of measurement.
 * @author Laurent Jourdren
 */
public abstract class FloatMeasurement implements Measurement {

  private static final int MAX_STATS_VALUES = 20000;

  private float lastValue = Float.NaN;
  private double[] values = new double[MAX_STATS_VALUES];
  private int nValues;
  private double median, mean, stdDev, reference, deviation;
  private int n;

  private List<Float> listMedian;
  private List<Float> listMean;
  private List<Float> listStdDev;
  private float min = Float.NaN;
  private float max = Float.NaN;
  private Histogram histo;

  private boolean firstGetScore = true;

  /**
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return a float value
   */
  protected abstract float calcFloatMeasurement(final Sequence sequence);

  /**
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return an object as result
   */
  public Object calcMesurement(final Sequence sequence) {

    final Float result = calcFloatMeasurement(sequence);

    this.lastValue = result.floatValue();

    return result;
  }

  /**
   * Add last measurements value to the statistics.
   */
  public void addLastMeasurementToStats() {

    this.values[nValues++] = this.lastValue;

    if (nValues == MAX_STATS_VALUES)
      subStats();

    this.histo.addValue(this.lastValue);
    n++;
    this.min =
        Float.isNaN(this.min) ? this.lastValue : Math.min(this.lastValue,
            this.min);
    this.max =
        Float.isNaN(this.max) ? this.lastValue : Math.max(this.lastValue,
            this.max);
  }

  private void subStats() {

    final double[] array;

    if (this.nValues == MAX_STATS_VALUES)
      array = this.values;
    else {

      array = new double[nValues];
      System.arraycopy(this.values, 0, array, 0, nValues);
    }

    float median = (float) MathUtils.median(array);
    float mean = (float) MathUtils.mean(array);
    float sd = (float) MathUtils.sdFast(array);

    if (this.listMedian == null)
      this.listMedian = new ArrayList<Float>();
    if (this.listMean == null)
      this.listMean = new ArrayList<Float>();
    if (this.listStdDev == null)
      this.listStdDev = new ArrayList<Float>();

    this.listMean.add(mean);
    this.listMedian.add(median);
    this.listStdDev.add(sd);
    nValues = 0;
  }

  private float getMean(List<Float> data) {

    final double[] array = MathUtils.toArray(data);

    return (float) MathUtils.mean(array);
  }

  /**
   * Get the type of the result of calcMeasurement.
   * @return the type of the measurement
   */
  public Object getType() {

    return Float.class;
  }

  /**
   * Parse a string to an object return as calcMeasurement.
   * @param s String to parse
   * @return an object
   */
  public Object parse(final String s) {

    if (s == null)
      return null;

    final Float result = Float.parseFloat(s);
    this.lastValue = result.floatValue();

    return result;
  }

  private void beforeFirstGetScore() {

    if (Double.isNaN(this.deviation))
      this.deviation = this.stdDev;
    if (Double.isNaN(this.reference))
      this.reference = this.median;

    this.firstGetScore = false;
  }

  /**
   * Get the score for the measurement.
   * @param value value
   * @return the score
   */
  public float getScore(final Object value) {

    if (this.firstGetScore)
      beforeFirstGetScore();

    final double val = ((Float) value).doubleValue();

    return (float) (1.0 - Math.abs((this.reference - val) / this.deviation));
  }

  /**
   * Compute statistics of the measurement.
   * @return a Properties object with all statistics
   */
  public Properties computeStatistics() {

    if (this.listMean == null || this.listMean.size() == 0)
      subStats();

    this.median = getMean(this.listMedian);
    this.mean = getMean(this.listMean);
    this.stdDev = getMean(this.listStdDev);

    final Properties result = new Properties();
    result.setProperty("median", Double.toString(this.median));
    result.setProperty("mean", Double.toString(this.mean));
    result.setProperty("stddev", Double.toString(this.stdDev));
    result.setProperty("n", Integer.toString(this.n));
    result.setProperty("min", Float.toString(this.min));
    result.setProperty("max", Float.toString(this.max));

    double[] histo = this.histo.getHistogram();
    if (histo != null)
      for (int i = 0; i < histo.length; i++)
        result.setProperty("" + (i * 10) + "-" + ((i + 1) * 10), Double
            .toString(histo[i]));

    return result;
  }

  /**
   * Clear the results and the current statistics.
   */
  public void clear() {

    if (values != null)
      nValues = 0;

    if (this.listMean != null)
      this.listMean.clear();
    if (this.listMedian != null)
      this.listMean.clear();
    if (this.listStdDev != null)
      this.listStdDev.clear();

    this.min = Float.NaN;
    this.max = Float.NaN;

    this.mean = Float.NaN;
    this.median = Float.NaN;
    this.stdDev = Float.NaN;
    this.reference = Float.NaN;
    this.deviation = Float.NaN;

    n = 0;

    this.firstGetScore = true;
  }

  /**
   * Set a property of the measurement.
   * @param key key of the property to set
   * @param value value of the property to set
   */
  public void setProperty(final String key, final String value) {

    if (key == null || value == null)
      return;

    if ("median".equals(key.trim().toLowerCase()))
      this.median = Double.parseDouble(value.trim());
    else if ("mean".equals(key.trim().toLowerCase()))
      this.mean = Double.parseDouble(value.trim());
    else if ("stdDev".equals(key.trim().toLowerCase()))
      this.stdDev = Double.parseDouble(value.trim());
    else if ("reference".equals(key.trim().toLowerCase()))
      this.reference = Double.parseDouble(value.trim());
    else if ("deviation".equals(key.trim().toLowerCase()))
      this.deviation = Double.parseDouble(value.trim());
  }

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {
  }

  /**
   * Run the initialization phase of the parameter.
   * @throws IOException if an error occurs while the initialization phase
   */
  public void init() throws IOException {
  }

  /**
   * Reset the values of the histogram.
   * @param minValueHisto
   * @param maxValueHisto
   */
  protected void resetHistogram(final double minValueHisto,
      final double maxValueHisto) {

    this.histo = new Histogram(minValueHisto, maxValueHisto, 10);
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   * @param minValueHisto minimal value of the histogram
   * @param maxValueHisto maximal value of the histogram
   */
  public FloatMeasurement(final double minValueHisto, final double maxValueHisto) {

    this.histo = new Histogram(minValueHisto, maxValueHisto, 10);
  }

}
