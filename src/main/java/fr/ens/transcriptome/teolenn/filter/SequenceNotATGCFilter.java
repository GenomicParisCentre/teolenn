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

package fr.ens.transcriptome.teolenn.filter;

import fr.ens.transcriptome.teolenn.Sequence;

/**
 * This class define a filter to remove all sequence that contains other
 * character than 'A','a','T','t','G','g','C','c'.
 * @author Laurent Jourdren
 */
public class SequenceNotATGCFilter implements SequenceFilter {

  /**
   * Tests whether or not the specified sequence should be accepted.
   * @param sequence Sequence to test
   * @return true if and only if the specified sequence should be accepted
   */
  public boolean accept(final Sequence sequence) {

    final String s = sequence.getSequence();
    final int len = s.length();

    for (int i = 0; i < len; i++)
      switch (s.charAt(i)) {

      case 'A':
      case 'a':
      case 'T':
      case 't':
      case 'G':
      case 'g':
      case 'C':
      case 'c':
        break;

      default:
        return false;
      }

    return true;
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
  // Constructor
  //

  /**
   * Public constructor.
   */
  public SequenceNotATGCFilter() {
  }

}
