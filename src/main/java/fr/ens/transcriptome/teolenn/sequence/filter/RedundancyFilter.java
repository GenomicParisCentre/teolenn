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

package fr.ens.transcriptome.teolenn.sequence.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ens.transcriptome.teolenn.DesignConstants;
import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.Settings;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.sequence.Sequence;
import fr.ens.transcriptome.teolenn.util.BinariesInstaller;
import fr.ens.transcriptome.teolenn.util.FileUtils;
import fr.ens.transcriptome.teolenn.util.ProcessUtils;
import fr.ens.transcriptome.teolenn.util.StringUtils;

/**
 * This class test the redundancy of oligos using SOAP.
 * @author Stéphane Le Crom
 * @author Laurent Jourdren
 */
public class RedundancyFilter implements SequenceFilter {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  /** Sequence filter name. */
  public static final String SEQUENCE_FILTER_NAME = "redundancy";

  private static final String SOAP_ARGS = " -s 12 -v 5 -r 1 -w 1000 -p ";

  // Parameters
  private File referenceFile;
  private File[] oligosFiles;

  private File oligosDir;
  private File tempDir;
  private BufferedReader br;
  private String currentChr;
  private int startOffset;
  private String extensionFilter;

  // Soap results for current chromosome
  private final Map<Integer, Integer> currentChrResult =
      new HashMap<Integer, Integer>();

  // Regex to retrieve chromosome, startPos and len of a sequence from its name
  private static final Pattern seqNamePattern =
      Pattern.compile("^(.*):subseq\\((\\d+),(\\d+)\\)$");

  /**
   * Get the name of the filter.
   * @return the name of the module
   */
  public String getName() {

    return SEQUENCE_FILTER_NAME;
  }

  /**
   * Get the description of the filter.
   * @return the description of the filter
   */
  public String getDescription() {

    return "Filter the redundancy of oligos using SOAP";
  }

  /**
   * Tests whether or not the specified sequence should be accepted.
   * @param sequence Sequence to test
   * @return true if and only if the specified sequence should be accepted
   */
  public boolean accept(final Sequence sequence) {

    final String sequenceName = sequence.getName();

    if (sequenceName == null)
      return false;

    final Matcher m = seqNamePattern.matcher(sequenceName);

    if (!m.matches())
      throw new RuntimeException("Unable to parse sequence name: "
          + sequenceName);

    final String chr = m.group(1);
    final int startPos = Integer.parseInt(m.group(2));
    final int len = Integer.parseInt(m.group(3));

    try {

      // If the sequence to test is not on the current chromosome, load results
      // for the sequence chromosome
      if (!chr.equals(this.currentChr)) {
        parseResultFile(chr);
        this.currentChr = chr;
      }

      // Get the length of the match of the soap result
      final Integer result = this.currentChrResult.get(startPos);

      // Return true if the result exists and if its length is equals to the
      // oligo length
      return result != null && result == len;

    } catch (IOException e) {
      throw new RuntimeException("Error while reading Soap results: "
          + e.getMessage());
    }
  }

  /**
   * Parse a ".sop" file and store its data in memory.
   * @param chrSequence chromosome to parse
   * @throws IOException if an error occurs while reading soap results
   */
  private final void parseResultFile(final String chrSequence)
      throws IOException {

    if (this.br != null)
      this.br.close();

    this.br =
        FileUtils.createBufferedReader(new File(this.tempDir, chrSequence
            .replace(' ', '_')
            + ".sop"));

    // Clear current results
    this.currentChrResult.clear();

    Scanner s = new Scanner(br);
    s.useDelimiter("\t|\n");

    while (true)
      try {
        final String oligoName = s.next();

        final int subSeqPos = oligoName.indexOf(":subseq(");
        final int commaPos = oligoName.indexOf(",", subSeqPos);

        final String chr = oligoName.substring(0, subSeqPos);
        final int oligoPos =
            Integer.parseInt(oligoName.substring(subSeqPos + 8, commaPos));

        s.next();
        s.next();

        final int nbMatches = s.nextInt();
        s.next();
        final int matchLen = s.nextInt();
        final String matchStrand = s.next();
        final String matchChr = s.next();
        final int matchStart = s.nextInt() + this.startOffset;
        final int matchType = s.nextInt();

        // add result in memory only if oligo match at the right position
        if (nbMatches == 1
            && matchType == 0 && "+".equals(matchStrand)
            && chr.equals(matchChr) && oligoPos == matchStart)
          this.currentChrResult.put(oligoPos, matchLen);

        s.nextLine();
      } catch (NoSuchElementException e) {

        break;
      }

  }

  /**
   * Create the parameter file for soap execution
   * @param parameterFile the output parameter file
   * @param oligosFiles oligos files to add to parameter file
   * @throws IOException if an error occurs while writing the file
   */
  private void createParameterFile(final File parameterFile,
      final File[] oligosFiles) throws IOException {

    FileWriter fw = new FileWriter(parameterFile);

    for (int i = 0; i < oligosFiles.length; i++) {

      fw.append("-a ");
      final String oligoFile = oligosFiles[i].getAbsolutePath();
      fw.append(oligoFile);
      fw.append(" -o ");
      fw.append(this.tempDir.getAbsolutePath());
      fw.append(File.separatorChar);
      fw.append(StringUtils.basename(oligosFiles[i].getName()) + ".sop");
      fw.append(SOAP_ARGS + Settings.getMaxThreads());
      fw.append("\n");

    }

    fw.close();
  }

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {

    if (DesignConstants.START_1_PARAMETER_NAME.equals(key)) {

      final boolean start1 = Boolean.parseBoolean(value);
      if (start1)
        this.startOffset = 0;
      else
        this.startOffset = -1;
    } else if (DesignConstants.GENOME_FILE_PARAMETER_NAME.equals(key))
      this.referenceFile = new File(value);
    else if (DesignConstants.TEMP_DIR_PARAMETER_NAME.equals(key))
      this.tempDir = new File(value);
    else if (DesignConstants.OLIGO_DIR_PARAMETER_NAME.equals(key))
      this.oligosDir = new File(value);
    else if (DesignConstants.EXTENSION_FILTER_PARAMETER_NAME.equals(key))
      this.extensionFilter = value;

  }

  /**
   * Run the initialization phase of the parameter.
   * @throws TeolennException
   */
  public void init() throws TeolennException {

    if (this.extensionFilter == null || "".equals(extensionFilter))
      throw new TeolennException("No extension filter set.");

    this.oligosFiles = this.oligosDir.listFiles(new FilenameFilter() {

      public boolean accept(File dir, String name) {

        return name.endsWith(extensionFilter);
      }
    });

    try {

      // Install soap if needed
      if (Settings.getSoapPath() == null)
        Settings.setSoapPath(BinariesInstaller.install("soap"));

      // Create the parameter file
      File paramFile = File.createTempFile("soap-", ".param");
      createParameterFile(paramFile, this.oligosFiles);

      // Define the commande line
      final String cmd =
          Settings.getSoapPath()
              + " -d " + this.referenceFile.getAbsolutePath() + " "
              + paramFile.getAbsolutePath();

      // Execute Soap
      ProcessUtils.exec(cmd, Settings.isStandardOutputForExecutable());

      // remove the parameter file
      if (!Globals.DEBUG && !paramFile.delete())
        logger.warning("Can't remove redundancy parameter file: "
            + paramFile.getAbsolutePath());
    } catch (IOException e) {

      throw new TeolennException("Error while initialize "
          + SEQUENCE_FILTER_NAME + " sequence filter: " + e.getMessage());
    }

  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   */
  public RedundancyFilter() {
  }

}
