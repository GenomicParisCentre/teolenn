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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This class define a settings class.
 * @author Laurent Jourdren
 */
public class Settings {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);
  private static Properties properties = new Properties();

  private static final String GT_PATH =
      "/export/home2/users/sgdb/jourdren/ArrayDesignTmp/src/genometools-1.0.2/bin/gt";

  private static final String SOAP_PATH =
      "/export/home2/users/sgdb/jourdren/soap-align/soap";
      //"/export/home2/users/sgdb/lecrom/soap_soft/soap";

  static {

    setGenomeToolsPath(GT_PATH);
    setSoapPath(SOAP_PATH);
  }

  /**
   * Get the path of the Soap application.
   * @return the path of the Soap application
   */
  public static String getSoapPath() {

    return properties.getProperty("soap.path");
  }

  /**
   * Set the path of the soap application.
   * @param soapPath The path of the soap application
   */
  public static void setSoapPath(final String soapPath) {

    properties.setProperty("soap.path", soapPath);
  }

  /**
   * Get the genome tools application path.
   * @return the genome tools application path
   */
  public static String getGenomeToolsPath() {

    return properties.getProperty("gt.path");
  }

  /**
   * Set the genome tools application path.
   * @param gtPath the genome tools application path
   */
  public static void setGenomeToolsPath(final String gtPath) {

    properties.setProperty("gt.path", gtPath);
  }

  /**
   * Get the configuration file path.
   * @return the configuration file path
   */
  public static String getConfigurationFilePath() {

    final String os = System.getProperty("os.name");
    final String home = System.getProperty("user.home");

    if (os.toLowerCase().startsWith("windows"))
      return home
          + File.separator + "Application Data" + File.separator
          + Globals.APP_NAME_LOWER_CASE + ".conf";

    return home + File.separator + "." + Globals.APP_NAME_LOWER_CASE;
  }

  /**
   * Save CorsenSwing options
   * @throws IOException if an error occurs while writing results
   */
  public static void saveSettings() throws IOException {

    saveSettings(new File(getConfigurationFilePath()));
  }

  /**
   * Save Corsen options
   * @param file File to save.
   * @throws IOException if an error occurs while writing settings
   */
  public static void saveSettings(final File file) throws IOException {

    FileOutputStream fos = new FileOutputStream(file);

    properties.store(fos, " "
        + Globals.APP_NAME + " version " + Globals.APP_VERSION
        + " configuration file");
    fos.close();
  }

  /**
   * Load CorsenSwing options
   * @throws IOException if an error occurs while reading settings
   */
  public static void loadSettings() throws IOException {

    loadSettings(new File(getConfigurationFilePath()));
  }

  /**
   * Load CorsenSwing options
   * @param file file to save
   * @throws IOException if an error occurs while reading the file
   */
  public static void loadSettings(final File file) throws IOException {

    logger.info("Load configuration file: " + file.getAbsolutePath());
    FileInputStream fis = new FileInputStream(file);

    properties.load(fis);
    fis.close();
  }

}
