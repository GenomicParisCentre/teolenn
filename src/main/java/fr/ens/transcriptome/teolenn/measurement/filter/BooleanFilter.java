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

package fr.ens.transcriptome.teolenn.measurement.filter;

import java.security.InvalidParameterException;

import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

/**
 * This class define a filter on boolean values.
 * @author Laurent Jourdren
 */
public class BooleanFilter implements MeasurementFilter {

  /** Measurement filter name. */
  public static final String MEASUREMENT_FILTER_NAME = "booleanfilter";

  private String field;
  private int index = -1;

  private boolean acceptValue;

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

    return "Filter boolean measurements";
  }

  /**
   * Filter a SequenceMeasurements.
   * @param sm SequenceMeasurements to test
   * @return true if the test allow to keep SequenceMeasurements values
   */
  public boolean accept(final SequenceMeasurements sm) {

    if (index == -1)
      this.index = sm.getIndexMeasurment(this.field);

    final Object[] values = sm.getArrayMeasurementValues();
    if (values == null)
      return false;

    final boolean b = (Boolean) values[index];

    return b == this.acceptValue;
  }

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {

    if ("measurement".equals(key) && value != null)
      this.field = value;
    if ("acceptValue".equals(key) && value != null)
      this.acceptValue = Boolean.parseBoolean(value);

  }

  /**
   * Run the initialization phase of the parameter.
   * @throws TeolennException if an error occurs while the initialization phase
   */
  public void init() throws TeolennException {

    if (this.field == null)
      throw new InvalidParameterException("The field value is unknown for "
          + MEASUREMENT_FILTER_NAME + " filter.");
  }

  //
  // Constructor
  //

  /**
   * Public constructor. Needed for instancition using
   * MeasurementFilterRegistery.
   */
  public BooleanFilter() {
  }

  /**
   * Public constructor
   * @param field Field to use
   * @param acceptValue value to keep
   */
  public BooleanFilter(final String field, final boolean acceptValue) {

    if (field == null)
      throw new NullPointerException("field value is  null");

    this.field = field;
    this.acceptValue = acceptValue;
  }

}
