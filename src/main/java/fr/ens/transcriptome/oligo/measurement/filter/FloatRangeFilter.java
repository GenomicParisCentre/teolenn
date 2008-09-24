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
 * This class define a filter on a range of float values.
 * @author Laurent Jourdren
 */
public class FloatRangeFilter implements SequenceMeasurementFilter {

  private String field;
  private int index = -1;

  private float min;
  private float max;

  /**
   * Filter a SequenceMeasurements.
   * @param sm SequenceMeasurements to test
   * @return true if the test allow to keep SequenceMeasurements values
   */
  public boolean accept(final SequenceMeasurements sm) {

    if (sm == null)
      return false;

    if (index == -1)
      index = sm.getIndexMeasurment(this.field);
    // findIndex(sm);

    final Object[] values = sm.getArrayMeasurementValues();
    if (values == null)
      return false;

    final float f = (Float) values[index];

    return this.min <= f && f <= this.max;
  }

  //
  // Constructor
  //
  
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
