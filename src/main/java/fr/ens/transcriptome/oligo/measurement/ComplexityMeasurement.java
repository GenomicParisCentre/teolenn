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

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import fr.ens.transcriptome.oligo.Sequence;
import fr.ens.transcriptome.oligo.SequenceIterator;
import fr.ens.transcriptome.oligo.util.StringUtils;

public class ComplexityMeasurement extends FloatMeasurement {

  private SequenceIterator si;
  private static final Pattern subseqPattern = Pattern.compile("subseq");

  private static final int countChar(String s, char c) {

    if (s == null)
      return 0;

    final int len = s.length();
    int count = 0;

    for (int i = 0; i < len; i++)
      if (s.charAt(i) == c)
        count++;

    return count;
  }

  public float calcFloatMeasurement(final Sequence sequence) {

    if (!si.hasNext())
      throw new RuntimeException(
          "Invalid oligo masked  sequence file: no more sequences");

    si.next();

    if (!isSameSequencePosition(sequence, si))
      throw new RuntimeException(
          "Invalid oligo masked  sequence file, sequence not found ("
              + sequence.getName() + ")");

    final String s = si.getSequence();

    // The list that contain the bases count as masked
    final char[] maskBase = {'a', 'c', 'g', 't', 'n', 'N', 'x', 'X'};

    int maskNumber = 0;

    // Count the number of masked bases
    for (int i = 0; i < maskBase.length; i++)
      maskNumber += countChar(s, maskBase[i]);

    // Calculate a complexity score
    float result = 1.0f - ((float) maskNumber / (float) s.length());

    return result;
  }

  private boolean isSameSequencePosition(final Sequence seqA,
      final Sequence seqB) {

    final String nameA = seqA.getName();
    final String nameB = seqB.getName();

    if (nameA == null || nameB == null)
      return false;

    final String[] splitA = subseqPattern.split(nameA);
    final String[] splitB = subseqPattern.split(nameB);

    return splitA[splitA.length - 1].equals(splitB[splitB.length - 1]);
  }

  public String getDescription() {

    return "Complexity measurement";
  }

  public String getName() {

    return "Complexity";
  }

  @Override
  public void setProperty(final String key, final String value) {

    if (key == null || value == null)
      return;

    if ("currentOligoFile".equals(key)) {

      try {

        File f = new File(StringUtils.basename(value) + ".filtered.masked");

        this.si = new SequenceIterator(f);

      } catch (IOException e) {

        throw new RuntimeException(
            "Unable to open oligo masked  sequence file: " + value);
      }

    }

    super.setProperty(key, value);
  }

  //
  // Constructor
  //

  public ComplexityMeasurement() {

    super(0, 1);
  }

}
