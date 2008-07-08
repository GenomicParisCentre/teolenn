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

package fr.ens.transcriptome.oligo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ens.transcriptome.oligo.measurement.Measurement;

public class SequenceMeasurements {
  // measurement
  private int id;
  private Sequence sequence;
  private List<Measurement> measurements = new ArrayList<Measurement>();
  private Map<String, Integer> measurementsIndex =
      new HashMap<String, Integer>();
  // private List<Object> measurementValues = new ArrayList<Object>();
  private Object[] measurementValues;

  private Measurement[] arrayMeasurements;

  private Map<Measurement, Float> weights = new HashMap<Measurement, Float>();

  //
  // Getters
  //

  public int getId() {
    return id;
  }

  public Sequence getSequence() {
    return sequence;
  }

  //
  // Setters
  //

  public void setId(int id) {
    this.id = id;
  }

  public void setSequence(Sequence sequence) {
    this.sequence = sequence;
  }

  //
  // Other methods
  //

  public int size() {

    return this.measurements.size();
  }

  public void addMesurement(final Measurement m) {

    addMesurement(m, 1.0f);
  }

  public void addMesurement(final Measurement m, final float weight) {

    if (m == null)
      return;

    this.measurements.add(m);
    measurementsIndex.put(m.getName(), measurementsIndex.size());
    this.arrayMeasurements = null;

    setWeight(m, weight);
  }

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

  public void addMesurementsToStats() {

    if (this.arrayMeasurements == null)
      this.arrayMeasurements = this.measurements.toArray(new Measurement[0]);

    for (int i = 0; i < arrayMeasurements.length; i++)
      this.arrayMeasurements[i].addLastMeasurementToStats();

  }

  public Object[] getArrayMeasurementValues() {

    return this.measurementValues;
  }

  public String[] getArrayMesurementNames() {

    String[] result = new String[size()];

    for (int i = 0; i < result.length; i++)
      result[i] = this.measurements.get(i).getName();

    return result;
  }

  public int getIndexMeasurment(final String name) {

    if (name == null)
      return -1;

    return this.measurementsIndex.get(name);
  }

  public Measurement getMeasurement(final String name) {

    final int index = getIndexMeasurment(name);

    return index < 0 ? null : this.measurements.get(index);
  }

  public void setWeight(Measurement m, float weight) {

    this.weights.put(m, weight);
  }

  void setArrayMeasurementValues(final Object[] values) {

    this.measurementValues = values;
  }

  public float getScore() {

    float result = 0;

    int count = 0;

    final Object[] values = getArrayMeasurementValues();

    for (Measurement m : this.measurements)
      result += m.getScore(values[count++]) * this.weights.get(m);

    return result;
  }

  public List<Measurement> getMeasurements() {

    return new ArrayList<Measurement>(this.measurements);
  }

}
