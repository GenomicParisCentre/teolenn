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

package fr.ens.transcriptome.teolenn.selector;


/**
 * This class define a measurement that returns
 * @author Stéphane Le Crom
 * @author Laurent Jourdren
 */
public final class PositionMeasurement extends SimpleSelectorMeasurement {

  /** Measurement name. */
  public static final String MEASUREMENT_NAME = "Position";

  private boolean start1;
  private int windowLength = -1;
  private int windowBestPosition;
  private boolean first = true;

  private int oligoLength;

  /**
   * Calc the measurement of a sequence.
   * @param chromosome the chromosome of sequence to use for the measurement
   * @param startPos the start position of sequence to use for the measurement
   * @return an object as result
   */
  public Object calcMesurement(final String chromosome, final int startPos) {

    if (this.windowLength == -1)
      throw new RuntimeException("Window size is undefined.");

    if (this.first) {

      this.windowBestPosition =
          (int) Math.ceil(((this.windowLength - this.oligoLength) / 2.0f));

      this.first = false;
    }

    // The first pos for the sequence for computation is always 0
    final int internalStartPos;
    if (this.start1)
      internalStartPos = startPos - 1;
    else
      internalStartPos = startPos;

    final int windowStart =
        (int) (Math.floor((float) internalStartPos / (float) this.windowLength)
            * this.windowLength - 1);

    final int oligo2Window = internalStartPos - windowStart;

    final float result =
        1 - (Math.abs(this.windowBestPosition - oligo2Window) / ((float) this.windowLength - (float) this.windowBestPosition));

    return result;
  }

  /**
   * Get the name of the measurement.
   * @return the name of the measurement
   */
  public String getName() {

    return MEASUREMENT_NAME;
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
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {
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
   * @param start1 true if the position of the first base of the sequence is 1
   * @param oligoLength The length of the oligos
   * @param windowSize The size of the window
   */
  public PositionMeasurement(final boolean start1, final int oligoLength,
      final int windowSize) {

    this.start1 = start1;
    this.oligoLength = oligoLength;
    this.windowLength = windowSize;
  }
}
