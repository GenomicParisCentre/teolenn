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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fr.ens.transcriptome.oligo.Sequence;
import fr.ens.transcriptome.oligo.util.Histogram;
import fr.ens.transcriptome.oligo.util.MathUtils;

public abstract class FloatMeasurement implements Measurement {

  private static final int MAX_STATS_VALUES = 20000;

  private float lastValue = Float.NaN;
  private double[] values = new double[MAX_STATS_VALUES];
  private int nValues;
  private double median, mean, deviation;
  private int n;

  private List<Float> listMedian;
  private List<Float> listMean;
  private List<Float> listDeviation;
  private Histogram histo;

  protected abstract float calcFloatMeasurement(final Sequence sequence);

  public Object calcMesurement(final Sequence sequence) {

    final Float result = calcFloatMeasurement(sequence);

    this.lastValue = result.floatValue();

    return result;
  }

  public void addLastMeasurementToStats() {

    this.values[nValues++] = this.lastValue;

    if (nValues == MAX_STATS_VALUES)
      subStats();

    this.histo.addValue(this.lastValue);
    n++;
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
    if (this.listDeviation == null)
      this.listDeviation = new ArrayList<Float>();

    this.listMean.add(mean);
    this.listMedian.add(median);
    this.listDeviation.add(sd);
    nValues = 0;
  }

  private float getMean(List<Float> data) {

    final double[] array = MathUtils.toArray(data);

    return (float) MathUtils.mean(array);
  }

  public abstract String getDescription();

  public abstract String getName();

  public Object getType() {

    return Float.class;
  }

  public Object parse(final String s) {

    if (s == null)
      return null;

    final Float result = Float.parseFloat(s);
    this.lastValue = result.floatValue();

    return result;
  }

  public float getScore(final Object value) {

    final double tm = ((Float) value).doubleValue();

    return (float) (1.0 - Math.abs((this.median - tm) / this.deviation));
  }

  public Properties computeStatistics() {

    if (this.listMean == null || this.listMean.size() == 0)
      subStats();

    this.median = getMean(this.listMedian);
    this.mean = getMean(this.listMean);
    this.deviation = getMean(this.listDeviation);

    final Properties result = new Properties();
    result.setProperty("median", Double.toString(this.median));
    result.setProperty("mean", Double.toString(this.mean));
    result.setProperty("deviation", Double.toString(this.deviation));
    result.setProperty("n", Integer.toString(this.n));

    double[] histo = this.histo.getHistogram();
    if (histo != null)
      for (int i = 0; i < histo.length; i++)
        result.setProperty("" + (i * 10) + "-" + ((i + 1) * 10), Double
            .toString(histo[i]));

    return result;
  }

  public void clear() {

    if (values != null)
      nValues = 0;

    if (this.listMean != null)
      this.listMean.clear();
    if (this.listMedian != null)
      this.listMean.clear();
    if (this.listDeviation != null)
      this.listDeviation.clear();

    n = 0;
  }

  public void setProperty(final String key, final String value) {

    if (key == null || value == null)
      return;

    if ("median".equals(key.trim().toLowerCase()))
      this.median = Double.parseDouble(value.trim());
    else if ("mean".equals(key.trim().toLowerCase()))
      this.mean = Double.parseDouble(value.trim());
    else if ("deviation".equals(key.trim().toLowerCase()))
      this.deviation = Double.parseDouble(value.trim());
  }

  //
  // Constructor
  //

  public FloatMeasurement(final double minValueHisto, final double maxValueHisto) {

    this.histo = new Histogram(minValueHisto, maxValueHisto, 10);
  }

}
