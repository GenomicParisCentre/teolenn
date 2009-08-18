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

package fr.ens.transcriptome.teolenn;

public class DesignConstants {

  public static final String OLIGO_SUFFIX = ".oligo";
  public static final String OLIGO_MASKED_SUFFIX = ".masked";
  public static final String OLIGO_FILTERED_SUFFIX = ".oligo.filtered";
  public static final String OLIGO_MASKED_FILTERED_SUFFIX = ".masked.filtered";
  public static final String OLIGO_SUBDIR = "oligos";
  public static final String TEMP_SUBDIR = "tmp";
  static final String OLIGO_MEASUREMENTS_FILE = "oligo.mes";
  static final String OLIGO_MEASUREMENTS_STATS_FILE = "oligo.stats";
  static final String OLIGO_MEASUREMENTS_FILTERED_FILE = "filtered.mes";
  static final String OLIGO_MEASUREMENTS_FILTERED_STATS_FILE =
  "filtered.stats";
  static final String SELECTED_FILE = "select.mes";
  public static final String GENOME_FILE_PARAMETER_NAME = "_genomefile";
  public static final String GENOME_MASKED_FILE_PARAMETER_NAME =
  "_genomemaskedfile";
  public static final String OUTPUT_DIR_PARAMETER_NAME = "_outputdir";
  public static final String OUTPUT_DEFAULT_FILE_PARAMETER_NAME = "_outputdefaultfile";
  public static final String OLIGO_DIR_PARAMETER_NAME = "_oligodir";
  public static final String TEMP_DIR_PARAMETER_NAME = "_tempdir";
  public static final String OLIGO_LENGTH_PARAMETER_NAME = "_oligolength";
  public static final String START_1_PARAMETER_NAME = "_start1";
  public static final String EXTENSION_FILTER_PARAMETER_NAME =
  "_extensionfilter";
  public static final String CURRENT_OLIGO_FILE_PARAMETER_NAME =
  "currentOligoFile";
  public static final String MEASUREMENT_FILE_PARAMETER_NAME = "_oriMesFile";
  public static final String FILTERED_MEASUREMENT_FILE_PARAMETER_NAME =
  "_filteredMesFile";
  public static final String STATS_FILE_PARAMETER_NAME = "_statsFile";
  public static final String SELECTED_FILE_PARAMETER_NAME = "_selectedFile";
  static final int OLIGO_LEN_DEFAULT = 60;

}
