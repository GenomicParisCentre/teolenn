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

package fr.ens.transcriptome.teolenn.measurement.filter;

import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

/**
 * This class define a filter on a range of float values.
 * @author Laurent Jourdren
 */
public class FloatRangeFilter implements MeasurementFilter {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  /** Measurement filter name. */
  public static final String MEASUREMENT_FILTER_NAME = "floatrange";

  private String field;
  private int index = -1;

  private float min;
  private float max;

  /**
   * Get the name of the filter.
   * @return the name of the module
   */
  public String getName() {

    return MEASUREMENT_FILTER_NAME;
  }

  /**
   * Get the description of the filter.
   * @return the description of the filter
   */
  public String getDescription() {

    return "Filter floats measurements";
  }
  
  /**
   * Filter a SequenceMeasurements.
   * @param sm SequenceMeasurements to test
   * @return true if the test allow to keep SequenceMeasurements values
   */
  public boolean accept(final SequenceMeasurements sm) {

    if (sm == null)
      return false;

    if (index == -1) {
      index = sm.getIndexMeasurment(this.field.toLowerCase());

      // Throw an exception if the measure is unknown
      if (index == -1) {
        logger.severe("Unknown measurement: " + this.field);
        throw new RuntimeException("In floatrange, unknown measurement: "
            + this.field);
      }
    }

    final Object[] values = sm.getArrayMeasurementValues();
    if (values == null)
      return false;

    final float f = (Float) values[index];

    return this.min <= f && f <= this.max;
  }

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {

    if ("measurement".equals(key) && value != null)
      this.field = value;
    else if ("min".equals(key) && value != null)
      this.min = Float.parseFloat(value);
    else if ("max".equals(key) && value != null)
      this.max = Float.parseFloat(value);

  }

  /**
   * Run the initialization phase of the parameter.
   * @throws TeolennException if an error occurs while the initialization phase
   */
  public void init() throws TeolennException {

    if (this.field == null)
      throw new TeolennException(
          "Invalid parameter: The Field value is unknown for "
              + MEASUREMENT_FILTER_NAME + " filter.");

    if (min > max) {

      final float tmp = this.min;

      this.min = this.max;
      this.max = tmp;
    }

  }

  //
  // Constructor
  //

  /**
   * Public constructor. Needed for instanciation using
   * MeasurementFilterRegistery.
   */
  public FloatRangeFilter() {
  }

  /**
   * Public constructor.
   * @param field Field to use
   * @param min minimal value to keep
   * @param max maximal value to keep
   */
  public FloatRangeFilter(final String field, final float min, final float max) {

    if (min < max) {

      this.min = min;
      this.max = max;
    } else {

      this.min = max;
      this.max = min;
    }

    this.field = field;
  }

}
