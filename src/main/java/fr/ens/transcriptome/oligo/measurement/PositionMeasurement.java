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

import fr.ens.transcriptome.oligo.Sequence;

/**
 * This class define a measurement that returns
 * @author Laurent Jourdren
 */
public final class PositionMeasurement extends SimpleMeasurement {

  private int windowSize = -1;
  private int windowBestPosition;
  private boolean first = true;

  /**
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return an object as result
   */
  public Object calcMesurement(final Sequence sequence) {

    if (this.windowSize == -1)
      throw new RuntimeException("Window size is undefined.");

    String seqName = sequence.getName();

    final int startPos = seqName.indexOf(":subseq(");
    final int endPos1 = seqName.indexOf(",", startPos);

    final int seqStart =
        Integer.parseInt(seqName.substring(startPos + 8, endPos1));

    if (this.first) {

      final int endPos2 = seqName.indexOf(")", startPos);
      final int lenPos =
          Integer.parseInt(seqName.substring(endPos1 + 1, endPos2));

      this.windowBestPosition =
          (int) Math.ceil(((this.windowSize - lenPos) / 2.0f));

      this.first = false;
    }

    final int windowStart =
        (int) (Math.floor((float) seqStart / (float) this.windowSize)
            * this.windowSize - 1);
    final int oligo2Window = seqStart - windowStart;

    final float result =
        1 - (Math.abs(this.windowBestPosition - oligo2Window) / ((float) this.windowSize - (float) this.windowBestPosition));

    return result;
  }

  /**
   * Get the name of the measurement.
   * @return the name of the measurement
   */
  public String getName() {

    return "Position";
  }

  /**
   * Get the description of the measurement.
   * @return the description of the measurement
   */
  public String getDescription() {

    return "Get a position score for the sequence";
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

    return Float.parseFloat(s);
  }

  /**
   * Get the score for the measurement.
   * @param value value
   * @return the score
   */
  public float getScore(final Object value) {

    return (Float) value;
  }

  /**
   * Clear the results and the current statistics.
   */
  public void clear() {

    this.first = false;
  }

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {

    if ("_windowsize".equals(key))
      this.windowSize = Integer.parseInt(value);
  }

  /**
   * Run the initialization phase of the parameter.
   */
  public void init() {
  }

  //
  // Constructors
  //

  /**
   * Public constructor.
   */
  public PositionMeasurement() {
  }

  /**
   * Public constructor.
   * @param windowSize The size of the window
   */
  public PositionMeasurement(final int windowSize) {

    this.windowSize = windowSize;
  }

}
