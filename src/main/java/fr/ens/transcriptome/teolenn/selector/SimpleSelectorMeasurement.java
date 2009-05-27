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

package fr.ens.transcriptome.teolenn.selector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ens.transcriptome.teolenn.measurement.SimpleMeasurement;
import fr.ens.transcriptome.teolenn.sequence.Sequence;

public abstract class SimpleSelectorMeasurement extends SimpleMeasurement
    implements SelectorMeasurement {

  private static final Pattern seqNamePattern =
    Pattern.compile("^(.*):subseq\\((\\d+),(\\d+)\\)$");
  
  /**
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return an object as result
   */
  final public Object calcMesurement(final Sequence sequence) {
    
    final String sequenceName = sequence.getName();

    final Matcher m = seqNamePattern.matcher(sequenceName);

    if (!m.matches())
      throw new RuntimeException("Unable to parse sequence name: "
          + sequenceName);

    final String chr = m.group(1);
    final int startPos = Integer.parseInt(m.group(2));
    
    return calcMesurement(chr, startPos);
  }
  
}
