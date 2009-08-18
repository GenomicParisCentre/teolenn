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

package fr.ens.transcriptome.teolenn.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.util.FileUtils;

/**
 * This class define a resource that get the of a sequene if exists.
 * @author Laurent Jourdren
 */
public class ORFResource {

  /** The name of the resource. */
  public static final String RESOURCE_NAME = "orf";

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  // Regex to retrieve chromosome, startPos and len of a sequence from its name
  private static final Pattern seqNamePattern =
      Pattern.compile("^(.+)\t(\\d+)\t(\\d+)$");

  private Map<String, Set<ORF>> orfs = new HashMap<String, Set<ORF>>();
  private final Set<ORF> orfsToRemove = new HashSet<ORF>();
  private Set<ORF> lastOrfsChr = null;
  private Set<ORF> currentChrORFs = null;

  private String currentSequenceName;
  private ORF currentORF;

  private String lastChromosome;
  private int lastOligoStart;

  /**
   * Static method to get the singleton of the ressource
   * @param properties Properties used to configure the resource
   * @return an ORFResource object
   * @throws IOException if an error occurs while reading the orfs file
   */
  public static ORFResource getRessource(final Properties properties)
      throws IOException {

    final MeasurementResources rs = MeasurementResources.getResources();

    if (rs.isResource(RESOURCE_NAME))
      return (ORFResource) rs.getResource(RESOURCE_NAME);

    if (!properties.containsKey("orfsfile"))
      throw new IOException("No orfsFile");

    // Get the orfs file
    final File orfsFile = new File(properties.getProperty("orfsfile"));

    // Compute the offset to apply on orfs positions
    final int oligoStart1 =
        Boolean.parseBoolean(properties.getProperty("_start1")) ? 1 : 0;

    final int orfStart1;
    if (properties.containsKey("start1"))
      orfStart1 =
          Boolean.parseBoolean(properties.getProperty("start1")) ? 1 : 0;
    else
      orfStart1 = 0;

    int startOffset = 0;

    if (oligoStart1 == orfStart1)
      startOffset = 0;
    else
      startOffset = oligoStart1 - orfStart1;

    final ORFResource result = new ORFResource(orfsFile, startOffset);

    rs.setResource(RESOURCE_NAME, result);

    return result;
  }

  /**
   * This class define an ORF.
   * @author Laurent Jourdren
   */
  public static final class ORF implements Comparable<ORF> {

    public String name;
    public String chromosome;
    public int start;
    public int end;
    public boolean wattsonStrand;

    /**
     * Test if a sequence is in the ORF
     * @param start start position of the ORF
     * @param end end position of the ORF
     * @return true if the sequence is in the ORF
     */
    public final boolean isOligoInsideORF(final int start, final int end) {

      return start >= this.start && end <= this.end;
    }

    public final int compareTo(final ORF orf) {

      return this.start - orf.start;
    }

    /**
     * Overide toString()
     * @return a String with the start and end position of the ORF
     */
    public String toString() {

      return name
          + " " + chromosome + " [" + start + "," + end + "]"
          + (wattsonStrand ? 'W' : 'C');
    }

    public boolean equals(Object o) {

      if (o == this)
        return true;

      if (o == null || !(o instanceof ORF))
        return false;

      final ORF orf = (ORF) o;

      return this.name.equals(orf.name)
          && this.start == orf.start && this.end == orf.end
          && this.wattsonStrand == orf.wattsonStrand;
    }

    //
    // Constructor
    //

    /**
     * Public constructor.
     * @param start Start position of the ORF
     * @param end End position of the ORF
     * @param name Name of the ORF
     * @param wattsonStrand true if the ORF is in the Wattson strand
     */
    public ORF(final String chromosome, final int start, final int end,
        final String name, final boolean wattsonStrand) {

      this.chromosome = chromosome;
      this.start = start;
      this.end = end;
      this.name = name;
      this.wattsonStrand = wattsonStrand;
    }

  }

  /**
   * Get the associated ORF to a sequence.
   * @param chromosome Chromosome
   * @param oligoStart the start position of the sequence
   * @param oligoLength the length of the sequence
   * @return the associated ORF if exists or null
   */
  public ORF getORF(final String chromosome, final int oligoStart,
      final int oligoLength) {

    if (this.lastChromosome == chromosome && lastOligoStart == oligoStart)
      return this.currentORF;

    this.lastChromosome = chromosome;
    this.lastOligoStart = oligoStart;
    
    return getORF(chromosome + "\t" + oligoStart + "\t" + oligoLength);
  }

  private final ORF getORF(final String sequenceKey) {

    if (sequenceKey == null)
      return null;

    if (sequenceKey.equals(this.currentSequenceName))
      return this.currentORF;

    final ORF orf = nextORF(sequenceKey);

    this.currentSequenceName = sequenceKey;
    this.currentORF = orf;

    return orf;
  }

  private final ORF nextORF(final String sequenceKey) {

    if (sequenceKey == null)
      return null;

    final Matcher m = seqNamePattern.matcher(sequenceKey);

    if (!m.matches())
      throw new RuntimeException("Unable to parse sequence name: "
          + sequenceKey);

    final String chr = m.group(1);
    final int start = Integer.parseInt(m.group(2));
    final int len = Integer.parseInt(m.group(3));
    final int end = start + len;

    final Set<ORF> chrORFs = this.orfs.get(chr);

    // Remove no more used ORFs
    if (chrORFs != this.lastOrfsChr) {
      this.lastOrfsChr = chrORFs;
      this.currentChrORFs = new TreeSet<ORF>(chrORFs);
    } else
      for (ORF o : this.orfsToRemove)
        this.currentChrORFs.remove(o);

    this.orfsToRemove.clear();

    for (ORF o : this.currentChrORFs) {

      if (end > o.end) {
        orfsToRemove.add(o);
        continue;
      }

      if (o.isOligoInsideORF(start, end))
        return o;
    }

    return null;
  }

  //
  // Constructor
  //

  /**
   * Run the initialization phase of the parameter.
   * @param orfsFile File with orfs to read
   * @param startOffset the offset to apply to have the same coordinate that the
   *          oligonucleotides sequences
   * @throws IOException if an error occurs while reading data
   */
  private ORFResource(final File orfsFile, final int startOffset)
      throws IOException {

    final BufferedReader br = FileUtils.createBufferedReader(orfsFile);

    final Pattern p = Pattern.compile("\t");

    String line = null;

    while ((line = br.readLine()) != null) {

      // Handle comments
      if (line.startsWith("#"))
        continue;

      final String[] fields = p.split(line);
      final String orfName = fields[0];
      final String chr = fields[1];
      final boolean wattsonStrand = "W".equals(fields[4].toUpperCase());

      final int start = Integer.parseInt(fields[2]) + startOffset;
      final int end = Integer.parseInt(fields[3]) + startOffset;

      final Set<ORF> chrORFs;

      if (!this.orfs.containsKey(chr)) {
        chrORFs = new TreeSet<ORF>();
        this.orfs.put(chr, chrORFs);
      } else
        chrORFs = this.orfs.get(chr);

      chrORFs.add(new ORF(chr, start, end, orfName, wattsonStrand));
    }

    int count = 0;
    for (String k : this.orfs.keySet())
      count += this.orfs.get(k).size();

    logger.fine("Orfs readed: " + count);
  }

}
