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

import fr.ens.transcriptome.oligo.MeltingTemp;
import fr.ens.transcriptome.oligo.Sequence;

public class TmMeasurement extends FloatMeasurement {

  public float calcFloatMeasurement(Sequence sequence) {

    return MeltingTemp.tmstalucDNA(sequence.getSequence(), 50, 50);
  }

  public String getDescription() {

    return "Calc Tm of sequence";
  }

  public String getName() {

    return "Tm";
  }

  //
  // Constructor
  //

  public TmMeasurement() {

    super(0, 100);
  }

}
