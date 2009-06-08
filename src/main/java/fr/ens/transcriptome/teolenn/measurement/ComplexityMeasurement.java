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

package fr.ens.transcriptome.teolenn.measurement;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import fr.ens.transcriptome.teolenn.DesignConstants;
import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.sequence.Sequence;
import fr.ens.transcriptome.teolenn.sequence.SequenceIterator;
import fr.ens.transcriptome.teolenn.util.StringUtils;

/**
 * This class define the complexity measurement.
 * @author Stéphane Le Crom
 * @author Laurent Jourdren
 */
public class ComplexityMeasurement extends FloatMeasurement {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  /** Measurement name. */
  public static final String MEASUREMENT_NAME = "Complexity";

  /* The list that contain the bases count as masked */
  private final static char[] MASK_BASES =
      {'a', 'c', 'g', 't', 'n', 'N', 'x', 'X'};

  private SequenceIterator si;
  private static final Pattern subseqPattern = Pattern.compile("subseq");

  /**
   * Count the number of a specified char in a string
   * @param s String to test
   * @param c Char to count
   * @return the number of specified char in a string
   */
  private static final int countChar(final String s, final char c) {

    if (s == null)
      return 0;

    final int len = s.length();
    int count = 0;

    for (int i = 0; i < len; i++)
      if (s.charAt(i) == c)
        count++;

    return count;
  }

  /**
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return a float value
   */
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

    int maskNumber = 0;

    // Count the number of masked bases
    for (int i = 0; i < MASK_BASES.length; i++)
      maskNumber += countChar(s, MASK_BASES[i]);

    // Calculate a complexity score
    float result = 1.0f - ((float) maskNumber / (float) s.length());

    return result;
  }

  /**
   * Test if two sequences have the same chromosome and position.
   * @param seqA Sequence A to test
   * @param seqB Sequence B to test
   * @return true if the two sequences have the same chromosome and position
   */
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

  /**
   * Get the description of the measurement.
   * @return the description of the measurement
   */
  public String getDescription() {

    return "Complexity measurement";
  }

  /**
   * Get the name of the measurement.
   * @return the name of the measurement
   */
  public String getName() {

    return MEASUREMENT_NAME;
  }

  /**
   * Set a property of the measurement.
   * @param key key of the property to set
   * @param value value of the property to set
   */
  public void setProperty(final String key, final String value) {

    if (key == null || value == null)
      return;

    // Get the current oligo file
    if (DesignConstants.CURRENT_OLIGO_FILE_PARAMETER_NAME.equals(key)) {

      final String ext =
          DesignConstants.OLIGO_FILTERED_SUFFIX.equals(StringUtils.extension(value))
              ? DesignConstants.OLIGO_MASKED_FILTERED_SUFFIX
              : DesignConstants.OLIGO_MASKED_SUFFIX;

      final File f = new File(StringUtils.basename(value) + ext);
      logger.fine("Open sequence file: "
          + f.getName() + " in " + MEASUREMENT_NAME + " measurement.");
      if (!f.exists())
        throw new RuntimeException(
            "Unable to open oligo masked  sequence file: " + f.getName());

      try {

        // Set the oligo masked file to read
        this.si = new SequenceIterator(f);

      } catch (IOException e) {

        throw new RuntimeException(
            "Unable to open oligo masked  sequence file: " + f.getName());
      }

    }

    super.setProperty(key, value);
  }

  /**
   * Get the score for the measurement.
   * @param value value
   * @return the score
   */
  public float getScore(final Object value) {

    return (Float) value;
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   */
  public ComplexityMeasurement() {

    super(0, 1);
  }

}
