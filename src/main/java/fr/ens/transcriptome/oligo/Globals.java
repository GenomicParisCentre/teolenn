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

package fr.ens.transcriptome.oligo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Globals constants of the application.
 * @author Laurent Jourdren
 */
public class Globals {

  private static Properties manifestProperties;
  private static final String MANIFEST_PROPERTIES_FILE = "/manifest.txt";

  /** The name of the application. */
  public static final String APP_NAME = "Alloligos";

  /** The name of the application. */
  public static final String APP_NAME_LOWER_CASE = APP_NAME.toLowerCase();

  /** The version of the application. */
  public static final String APP_VERSION = getVersion();

  /** The built number of the application. */
  public static final String APP_BUILD_NUMBER = getBuiltNumber();

  /** The build date of the application. */
  public static final String APP_BUILD_DATE = getBuiltDate();

  /** The log level of the application. */
  public static final Level LOG_LEVEL = Level.OFF;

  private static final String WEBSITE_URL_DEFAULT =
      "http://transcriptome.ens.fr/alloligos";

  /** Corsen Website url. */
  public static final String WEBSITE_URL = getWebSiteURL();

  private static String getVersion() {

    String s = getManifestProperty("Specification-Version");

    return s != null ? s : "UNKNOWN VERSION";
  }

  private static String getBuiltNumber() {

    String s = getManifestProperty("Implementation-Version");

    return s != null ? s : "UNKNOWN BUILT";
  }

  private static String getBuiltDate() {

    final String unknown = "UNKNOWN DATE";

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

  private static void readManifest() {

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
