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

package fr.ens.transcriptome.teolenn.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.util.FileUtils;
import fr.ens.transcriptome.teolenn.util.UnSynchronizedBufferedWriter;

public class ChromosomeNameResource {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  /** The name of the resource. */
  public static final String RESOURCE_NAME = "chromosomes";

  /** The name of the chromosome list file. */
  private static final String CHROMOSOME_LIST_FILENAME = "chromosomes_list.txt";

  private static char[] FORBIDEN_CHARACTERS =
      {'!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '.', '/',
          ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '`', '{',
          '|', '}', '~'};

  private List<String> list = new ArrayList<String>();
  private Map<String, Integer> lengths = new HashMap<String, Integer>();

  private File listFile;

  private static final class ChromosomeNameComparator implements
      Comparator<String> {

    public int compare(String a, String b) {

      if (a == null && b == null)
        return 0;

      if (a == null)
        return -1;
      if (b == null)
        return 1;

      if (a.equals(b))
        return 0;

      int lastA = getLastDigitIndex(a);

      int lastB = getLastDigitIndex(b);

      if (lastA != lastB)
        return a.compareTo(b);

      final String prefixA = a.substring(0, lastA);
      final String prefixB = b.substring(0, lastB);

      if (!prefixA.equals(prefixB))
        return prefixA.compareTo(prefixB);

      final String suffixA = a.substring(lastA, a.length());
      final String suffixB = b.substring(lastB, b.length());

      int nA = suffixA.length() == 0 ? 0 : Integer.parseInt(suffixA);
      int nB = suffixB.length() == 0 ? 0 : Integer.parseInt(suffixB);

      return nA - nB;
    }

    private int getLastDigitIndex(String a) {

      int i = a.length();

      while (i > 0) {

        if (!Character.isDigit(a.charAt(i - 1)))
          return i;
        i--;
      }

      return 0;
    }

  };

  /**
   * Static method to get the singleton of the resource
   * @return an ORFResource object
   * @throws IOException if an error occurs while reading the orfs file
   */
  public static ChromosomeNameResource getRessource() throws TeolennException {

    final Resources rs = Resources.getResources();

    if (rs.isResource(RESOURCE_NAME))
      return (ChromosomeNameResource) rs.getResource(RESOURCE_NAME);

    throw new TeolennException("Chromosomes names resource not initialized.");
  }

  /**
   * Static method to get the singleton of the resource
   * @param oligoDirectory The path to the oligo directory
   * @return an ORFResource object
   * @throws IOException if an error occurs while reading the orfs file
   */
  public static ChromosomeNameResource getRessource(File oligoDirectory) {

    final Resources rs = Resources.getResources();

    if (rs.isResource(RESOURCE_NAME))
      return (ChromosomeNameResource) rs.getResource(RESOURCE_NAME);

    final ChromosomeNameResource result = new ChromosomeNameResource();

    // Get the oligo directory
    result.listFile = new File(oligoDirectory, CHROMOSOME_LIST_FILENAME);

    rs.setResource(RESOURCE_NAME, result);

    return result;
  }

  /**
   * Add chromosomes names to the list.
   * @param chromosomeName Name of the chromosome to add
   * @throws TeolennException if one of the chromosome names is invalid
   */
  public void addChromosomesNames(final Map<String, Integer> chromosomeNames)
      throws TeolennException {

    for (Map.Entry<String, Integer> e : chromosomeNames.entrySet()) {

      final String nameTrimmed = e.getKey().trim();

      this.list.add(nameTrimmed);
      this.lengths.put(nameTrimmed, e.getValue());
    }

    logger.info("Chromosomes: " + this.list);

    final String verifyError = verify();
    if (verifyError != null) {
      logger.severe(verifyError);
      throw new TeolennException(verifyError);
    }

    sortNames();
    save();
  }

  /**
   * Sort the chromosomes names.
   */
  private void sortNames() {

    Collections.sort(this.list, new ChromosomeNameComparator());
  }

  /**
   * Get the list of the chromosomes names.
   * @return an unmodifiable list of chromosomes
   */
  public List<String> getChromosomesNames() {

    return Collections.unmodifiableList(this.list);
  }

  /**
   * Get the chromosome length.
   * @param name Name of the chromosome
   * @return the length of the chromosome
   */
  public int getChromosomeLength(final String name) {

    if (this.lengths.containsKey(name))
      return this.lengths.get(name);

    return 0;
  }

  /**
   * Save the list of chromosomes.
   */
  private void save() {

    try {
      UnSynchronizedBufferedWriter bw =
          FileUtils.createBufferedWriter(this.listFile);

      StringBuilder sb = new StringBuilder();

      for (String s : this.list) {
        sb.append(s);
        sb.append('\t');
        sb.append(getChromosomeLength(s));
        sb.append('\n');
      }

      bw.write(sb.toString());
      bw.close();

    } catch (FileNotFoundException e) {

      logger.warning("Unable to create the file for chromosome names list.");
      return;
    } catch (IOException e) {

      logger.warning("Unable to create the file for chromosome names list.");
      return;
    }
  }

  /**
   * Verify if the chromosomes names are valid.
   * @return a String with the error message if needed
   */
  private String verify() throws TeolennException {

    for (String chr : this.list) {

      if (chr.indexOf(' ') != -1)
        return "Invalid chromosome name. Not space is allowed: " + chr;

      for (int i = 0; i < FORBIDEN_CHARACTERS.length; i++)
        if (chr.indexOf(FORBIDEN_CHARACTERS[i]) != -1)
          return "Invalid chromosome name. The name contains one or more invalid character: "
              + chr;

    }

    return null;
  }

  /**
   * Load the list of the chromosomes.
   * @throws TeolennException Throw an exception if the file can not be read
   */
  public void load() throws TeolennException {

    this.list.clear();

    try {
      BufferedReader br = FileUtils.createBufferedReader(this.listFile);

      String line = null;

      while ((line = br.readLine()) != null) {

        final String trimmedLine = line.trim();
        final int pos = trimmedLine.indexOf('\t');
        final String chr = trimmedLine.substring(0, pos - 1).trim();
        final int len = Integer.parseInt(trimmedLine.substring(pos + 1));

        this.list.add(chr);
        this.lengths.put(chr, len);
      }

      br.close();

      sortNames();

    } catch (FileNotFoundException e) {
      throw new TeolennException(
          "Unable to load the list of chromosomes names.");

    } catch (IOException e) {
      throw new TeolennException(
          "Unable to load the list of chromosomes names.");
    }

    final String verifyError = verify();
    if (verifyError != null) {
      logger.severe(verifyError);
      throw new TeolennException(verifyError);
    }

  }

}
