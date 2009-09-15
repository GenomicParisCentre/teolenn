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

package fr.ens.transcriptome.teolenn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Globals constants of the application.
 * @author Laurent Jourdren
 */
public class Globals {

  private static Properties manifestProperties;
  private static final String MANIFEST_PROPERTIES_FILE = "/manifest.txt";

  /** The name of the application. */
  public static final String APP_NAME = "Teolenn";

  /** The name of the application. */
  public static final String APP_NAME_LOWER_CASE = APP_NAME.toLowerCase();

  /** The version of the application. */
  public static final String APP_VERSION = getVersion();

  /** The built number of the application. */
  public static final String APP_BUILD_NUMBER = getBuiltNumber();

  /** The build date of the application. */
  public static final String APP_BUILD_DATE = getBuiltDate();

  /** The log level of the application. */
  public static final Level LOG_LEVEL = Level.INFO; // Level.OFF;

  /** Set the debug mode. */
  public static final boolean DEBUG = false;

  /** Use serialization format for measurement file. */
  public static final boolean MEASUREMENT_FILE_SERIALIZED = true;

  /** Format of the log. */
  public static final Formatter LOG_FORMATTER = new Formatter() {
    public String format(final LogRecord record) {
      return record.getLevel() + "\t" + record.getMessage() + "\n";
    }
  };

  private static final String WEBSITE_URL_DEFAULT =
      "http://transcriptome.ens.fr/teolenn";

  /** Teolenn Website url. */
  public static final String WEBSITE_URL = getWebSiteURL();

  private static final String COPYRIGHT_DATE = "2008-2009";

  /** Licence text. */
  public static final String LICENSE_TXT =
      "This program is developed under the GNU General Public License version 2 or later and CeCILL.";

  /** About string, plain text version. */
  public static final String ABOUT_TXT =
      Globals.APP_NAME
          + " version "
          + Globals.APP_VERSION
          + " ("
          + Globals.APP_BUILD_NUMBER
          + ")"
          + " is a software to compute design of oligonucleotides for microarrays.\n"
          + "This version has been built on " + APP_BUILD_DATE + ".\n\n"
          + "Authors:\n" + "  Laurent Jourdren <jourdren@biologie.ens.fr>\n"
          + "  Stéphane Le Crom <lecrom@biologie.ens.fr>\n" + "Contacts:\n"
          + "  Mail: " + APP_NAME_LOWER_CASE + "@biologie.ens.fr\n"
          + "  Google group: http://groups.google.com/group/teolenn\n"
          + "Copyright " + COPYRIGHT_DATE
          + " École Normale Supérieure microarray platform.\n" + LICENSE_TXT
          + "\n";

  /** Default standard output state. */
  public static final boolean STD_OUTPUT_DEFAULT = false;

  /** Design file version. */
  public static final double DESIGN_FILE_VERSION = 1.1;

  private static String getVersion() {

    String s = getManifestProperty("Specification-Version");

    return s != null ? s : "UNKNOWN_VERSION";
  }

  private static String getBuiltNumber() {

    String s = getManifestProperty("Implementation-Version");

    return s != null ? s : "UNKNOWN_BUILT";
  }

  private static String getBuiltDate() {

    final String unknown = "UNKNOWN_DATE";

    String s = getManifestProperty("Built-Date");

    return s != null ? s : unknown;
  }

  private static String getWebSiteURL() {

    String s = getManifestProperty("url");

    return s != null ? s : WEBSITE_URL_DEFAULT;
  }

  private static String getManifestProperty(final String propertyKey) {

    if (propertyKey == null)
      return null;

    readManifest();

    return manifestProperties.getProperty(propertyKey);
  }

  private static synchronized void readManifest() {

    if (manifestProperties != null)
      return;

    try {
      manifestProperties = new Properties();

      InputStream is =
          Globals.class.getResourceAsStream(MANIFEST_PROPERTIES_FILE);

      if (is == null)
        return;

      manifestProperties.load(is);
    } catch (IOException e) {
    }
  }

  //
  // Constructor
  //

  /**
   * Private constructor.
   */
  private Globals() {
  }

}
