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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class contains utilty methods.
 * @author Laurent Jourdren
 */
public class SystemUtils {

  /**
   * Get the name of the host.
   * @return The name of the host
   */
  public static String getHostName() {

    try {
      InetAddress addr = InetAddress.getLocalHost();

      // Get hostname
      return addr.getHostName();
    } catch (UnknownHostException e) {

      return null;
    }
  }

  /**
   * Get IP of the host
   * @return The IP of the host in textual form
   */
  public static String getIPAddr() {

    try {
      InetAddress addr = InetAddress.getLocalHost();

      // Get IP
      return addr.getHostAddress();
    } catch (UnknownHostException e) {

      return null;
    }
  }

  /**
   * Test if the system is Mac OS X.
   * @return true if the system is Mac OS X
   */
  public static boolean isMacOsX() {
    return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
  }

  /**
   * Test if the system is Unix.
   * @return true if the operating systeme is Windows.
   */
  public static boolean isLinux() {

    return System.getProperty("os.name").toLowerCase().startsWith("linux");
  }

}
