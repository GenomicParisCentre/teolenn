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

package fr.ens.transcriptome.oligo.measurement.filter;

import fr.ens.transcriptome.oligo.SequenceMeasurements;

/**
 * This class define a filter on boolean values.
 * @author Laurent Jourdren
 */
public class BooleanFilter implements SequenceMeasurementFilter {

  private String field;
  private int index = -1;

  private boolean acceptValue;

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

  //
  // Constructor
  //

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