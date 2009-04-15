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
 * or to join the Teolenn mailing list, visit the home page
 * at:
 *
 *      http://www.transcriptome.ens.fr/teolenn
 *
 */

package fr.ens.transcriptome.teolenn.sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ens.transcriptome.teolenn.measurement.Measurement;

/**
 * This class define a sequence measurement.
 * @author Laurent Jourdren
 */
public class SequenceMeasurements {

  private int id;
  private Sequence sequence;
  private final List<Measurement> measurements = new ArrayList<Measurement>();
  private final Map<String, Integer> measurementsIndex =
      new HashMap<String, Integer>();
  private Object[] measurementValues;
  private Measurement[] arrayMeasurements;
  private Map<Measurement, Float> weights = new HashMap<Measurement, Float>();

  //
  // Getters
  //

  /**
   * Get the id of the measurement.
   * @return the id of the measurement
   */
  public int getId() {
    return id;
  }

  /**
   * Get the sequence related to the measurement
   * @return the sequence related to the measurement
   */
  public Sequence getSequence() {
    return sequence;
  }

  //
  // Setters
  //

  /**
   * Set the id of the SequenceMeasurement
   * @param id id to set
   */
  public void setId(final int id) {
    this.id = id;
  }

  /**
   * Set the sequence.
   * @param sequence The sequence to set
   */
  public void setSequence(final Sequence sequence) {
    this.sequence = sequence;
  }

  //
  // Other methods
  //

  /**
   * Get the number of measurements
   * @return ths number of measurements
   */
  public int size() {

    return this.measurements.size();
  }

  /**
   * Add a measurement.
   * @param m Measurement to add
   */
  public void addMesurement(final Measurement m) {

    addMesurement(m, 1.0f);
  }

  /**
   * Add a measurement.
   * @param m Measurement to add
   * @param weight Weight of the measurement in the final score
   */
  public void addMesurement(final Measurement m, final float weight) {

    if (m == null)
      return;

    this.measurements.add(m);
    measurementsIndex.put(m.getName().toLowerCase(), measurementsIndex.size());
    this.arrayMeasurements = null;

    setWeight(m, weight);
  }

  /**
   * Calc all the measurements for the sequence.
   */
  public void calcMesurements() {

    if (this.arrayMeasurements == null) {
      this.arrayMeasurements = this.measurements.toArray(new Measurement[0]);
      this.measurementValues = new Object[this.arrayMeasurements.length];
    }

    for (int i = 0; i < arrayMeasurements.length; i++) {
      this.measurementValues[i] =
          this.arrayMeasurements[i].calcMesurement(this.sequence);
    }

  }

  /**
   * Add the current measurements to the statistics of the measurements.
   */
  public void addMesurementsToStats() {

    if (this.arrayMeasurements == null)
      this.arrayMeasurements = this.measurements.toArray(new Measurement[0]);

    for (int i = 0; i < arrayMeasurements.length; i++)
      this.arrayMeasurements[i].addLastMeasurementToStats();

  }

  /**
   * Get an array with the measurements values.
   * @return an array of objects with the measurements values
   */
  public Object[] getArrayMeasurementValues() {

    return this.measurementValues;
  }

  /**
   * Get the measurements names.
   * @return an array with the measurements name
   */
  public String[] getArrayMesurementNames() {

    String[] result = new String[size()];

    for (int i = 0; i < result.length; i++)
      result[i] = this.measurements.get(i).getName();

    return result;
  }

  /**
   * Get the index of a measurement.
   * @param name name of the measurement to search
   * @return the index of the measurement in the SequenceMeasurement
   */
  public int getIndexMeasurment(final String name) {

    if (name == null)
      return -1;

    final Integer result = this.measurementsIndex.get(name.toLowerCase());

    if (result == null)
      return -1;

    return result;
  }

  /**
   * Get a measurement object from its name.
   * @param name The name of the measurement to search
   * @return the index of the measurement in the SequenceMeasurement
   */
  public Measurement getMeasurement(final String name) {

    final int index = getIndexMeasurment(name);

    return index < 0 ? null : this.measurements.get(index);
  }

  /**
   * Get the weight of a measurement.
   * @param m Measurement to query
   * @return the weight of a measurement or NaN if the measurement doesn't
   *         exists.
   */
  public float getWeight(final Measurement m) {

    final Float result = this.weights.get(m);

    if (result == null)
      return Float.NaN;

    return result;
  }

  /**
   * Set the weight of a measurement.
   * @param m Measurement
   * @param weight weight to set
   */
  public void setWeight(final Measurement m, final float weight) {

    this.weights.put(m, weight);
  }

  /**
   * Set the array of measurement values
   * @param values Values to set
   */
  public void setArrayMeasurementValues(final Object[] values) {

    this.measurementValues = values;
  }

  /**
   * Compute the final score of the sequence.
   * @return the final score of the sequence
   */
  public float getScore() {

    float result = 0;

    int count = 0;

    final Object[] values = getArrayMeasurementValues();

    for (Measurement m : this.measurements) {

      final Object value = values[count++];
      final float score = m.getScore(value);
      final float scoreWithWeigth = score * this.weights.get(m);

      result += scoreWithWeigth;
    }

    return result;
  }

  /**
   * Get a list of the measurements
   * @return a list of measurement
   */
  public List<Measurement> getMeasurements() {

    return Collections.unmodifiableList(this.measurements);
  }

  /**
   * Test if the sum of the weights of the measurements equals to 1.
   * @return true if the sum of the weights of the measurements equals to 1
   */
  public boolean isSumOfWeightEquals1() {

    float sum = 0;

    for (Measurement m : this.measurements)
      sum += this.weights.get(m);

    return sum == 1.0f;
  }

}
