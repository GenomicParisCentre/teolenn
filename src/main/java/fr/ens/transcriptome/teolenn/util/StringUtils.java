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

package fr.ens.transcriptome.teolenn.util;

import java.io.File;

public final class StringUtils {

  /**
   * Get the basename of the filename.
   * @param filename The filename
   * @return the basename of the file
   */
  public static String basename(final String filename) {

    if (filename == null)
      return null;

    final File f = new File(filename);
    final String shortName = f.getName();

    final int pos = shortName.indexOf('.');

    if (pos == -1)
      return filename;

    return filename
        .substring(0, filename.length() - (shortName.length() - pos));
  }

  /**
   * Get the extension of a filename.
   * @param filename The filename
   * @return the exstension of the filename
   */
  public static String extension(String filename) {

    if (filename == null)
      return null;

    final File f = new File(filename);
    final String shortName = f.getName();

    final int pos = shortName.indexOf('.');

    if (pos == -1)
      return "";

    return filename.substring(filename.length() - (shortName.length() - pos),
        filename.length());
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

  /**
   * Split a string. \t is the separator character.
   * @param s the String to split
   * @param array The result array.
   * @return the array with the new values
   */
  public static final String[] fastSplit(final String s, final String[] array) {

    if (array == null || s == null)
      return null;

    int lastPos = 0;
    final int len = array.length - 1;

    for (int i = 0; i < len; i++) {

      final int pos = s.indexOf("\t", lastPos);

      if (pos == -1)
        throw new ArrayIndexOutOfBoundsException();
      array[i] = s.substring(lastPos, pos);
      lastPos = pos + 1;
    }
    array[len] = s.substring(lastPos, s.length());

    return array;
  }

}
