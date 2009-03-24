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

package fr.ens.transcriptome.teolenn.util;

public final class StringUtils {

  /**
   * Get the basename of the filename.
   * @param filename The filename
   * @return the basename of the file
   */
  public static String basename(final String filename) {

    if (filename == null)
      return null;

    int pos = filename.lastIndexOf('.');

    return filename.substring(0, pos);
  }

  /**
   * Get the extension of a filename.
   * @param filename The filename
   * @return the exstension of the filename
   */
  public static String extension(String filename) {

    if (filename == null)
      return null;

    int pos = filename.lastIndexOf('.');

    return filename.substring(pos, filename.length());
  }

  /**
   * Remove non alpha char at the end of String.
   * @param s String to handle
   * @return the string without the last non end of string
   */
  public static final String removeNonAlphaAtEndOfString(final String s) {

    if (s == null)
      return null;

    int len = s.length();
    if (len == 0)
      return s;

    char c = s.charAt(len - 1);
    if (!Character.isLetter(c))
      return s.substring(0, len - 1);

    return s;
  }

  /**
   * Convert a number of milliseconds into a human reading string.
   * @param time time in ms
   * @return a the time in ms
   */
  public static final String toTimeHumanReadable(final long time) {

    long min = time / (60 * 1000);
    long minRest = time % (60 * 1000);
    long sec = minRest / 1000;

    long mili = minRest % 1000;

    return String.format("%02d:%02d.%03d", min, sec, mili);
  }

}
