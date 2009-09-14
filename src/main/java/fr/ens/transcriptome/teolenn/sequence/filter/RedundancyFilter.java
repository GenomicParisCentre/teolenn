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

package fr.ens.transcriptome.teolenn.sequence.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

    final int startOffset = this.startOffset;
    String l = null;

    while ((l = br.readLine()) != null) {

      final String line = l;

      final int subSeqPos = indexOf(line, ':', 0);
      final int commaPos = indexOf(line, ',', subSeqPos);
      final int posTabSeq = indexOf(line, '\t', commaPos);
      final int posTabUnknown1 = indexOf(line, '\t', posTabSeq);
      final int posTabNbMatches = indexOf(line, '\t', posTabUnknown1);
      final int posTabUnknown2 = indexOf(line, '\t', posTabNbMatches);
      final int posTabLen = indexOf(line, '\t', posTabUnknown2);
      final int posTabStrand = indexOf(line, '\t', posTabLen);
      final int posTabChr = indexOf(line, '\t', posTabStrand);
      final int posTabMatchStart = indexOf(line, '\t', posTabChr);
      final int posTabMatchType = indexOf(line, '\t', posTabMatchStart);
      final int posTabNucleotideModifs = indexOf(line, '\t', posTabMatchType);

      if (posTabMatchType == -1)
        continue;

      final String chr = line.substring(0, subSeqPos);
      final int oligoPos =
          Integer.parseInt(line.substring(subSeqPos + 8, commaPos));
      final int nbMatches =
          Integer.parseInt(line.substring(posTabNbMatches + 1, posTabUnknown2));
      final int matchLen =
          Integer.parseInt(line.substring(posTabLen + 1, posTabStrand));
      final char matchStrand = line.charAt(posTabStrand + 1);
      final String matchChr = line.substring(posTabChr + 1, posTabMatchStart);
      final int matchStart =
          Integer.parseInt(line
              .substring(posTabMatchStart + 1, posTabMatchType))
              + startOffset;
      final int matchType =
          posTabNucleotideModifs == -1 ? Integer.parseInt(line
              .substring(posTabMatchType + 1)) : Integer.parseInt(line
              .substring(posTabMatchType + 1, posTabNucleotideModifs));

      // add result in memory only if oligo match at the right position
      if (nbMatches == 1
          && matchType == 0 && matchStrand == '+' && chr.equals(matchChr)
          && oligoPos == matchStart)
        this.currentChrResult.put(oligoPos, matchLen);

    }

  }

  private static final int indexOf(final String ch, final char character,
      final int startPos) {

    if (startPos == -1)
      return -1;

    int p = startPos;
    final int len = ch.length();

    while (++p < len)
      if (ch.codePointAt(p) == character)
        return p;

    return -1;
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
