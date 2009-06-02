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

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ens.transcriptome.teolenn.TeolennException;
import fr.ens.transcriptome.teolenn.measurement.resource.ORFResource;
import fr.ens.transcriptome.teolenn.sequence.Sequence;

/**
 * This class test if a sequence in an ORF.
 * @author Laurent Jourdren
 */
public class ORFsFilter implements SequenceFilter {

  /** Sequence filter name. */
  public static final String SEQUENCE_FILTER_NAME = "orfs";

  private ORFResource ressource;
  private Properties ressourceProperties = new Properties();

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

    return "Filter ORFs";
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
    final int start = Integer.parseInt(m.group(2));
    final int len = Integer.parseInt(m.group(3));

    return this.ressource.getORF(chr, start, len) != null;
  }

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {

    if (key == null || value == null)
      return;

    this.ressourceProperties.setProperty(key.toLowerCase(), value);
  }

  /**
   * Run the initialization phase of the parameter.
   * @throws TeolennException if an error occurs while initialize the filter
   */
  public void init() throws TeolennException {

    try {
      this.ressource = ORFResource.getRessource(this.ressourceProperties);
    } catch (IOException e) {

      throw new TeolennException(e);
    }

  }

}
