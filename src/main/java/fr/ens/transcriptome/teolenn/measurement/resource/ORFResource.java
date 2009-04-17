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

package fr.ens.transcriptome.teolenn.measurement.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import fr.ens.transcriptome.teolenn.sequence.Sequence;

public class ORFResource {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  // Regex to retrieve chromosome, startPos and len of a sequence from its name
  private static final Pattern seqNamePattern =
      Pattern.compile("^(.*):subseq\\((\\d+),(\\d+)\\)$");

  private Map<String, Set<ORF>> orfs = new HashMap<String, Set<ORF>>();
  private final Set<ORF> orfsToRemove = new HashSet<ORF>();
  private Set<ORF> lastOrfsChr = null;

  private String currentSequenceName;
  private ORF currentORF;

  public static ORFResource getRessource(final Properties properties)
      throws IOException {

    final MeasurementResources rs = MeasurementResources.getResources();

    if (rs.isResource("orf"))
      return (ORFResource) rs.getResource("orf");

    if (!properties.containsKey("orfsfile"))
      throw new IOException("No orfsFile");

    final File orfsFile = new File(properties.getProperty("orfsfile"));
    final boolean start1;

    if (properties.containsKey("start1"))
      start1 = Boolean.parseBoolean(properties.getProperty("start1"));
    else
      start1 = false;

    final ORFResource result = new ORFResource(orfsFile, start1);

    rs.setResource("orf", result);

    return result;
  }

  public static final class ORF implements Comparable<ORF> {

    public String name;
    public int start;
    public int end;
    public boolean wattsonStrand;

    public final boolean isOligoInsideORF(final int start, final int end) {

      return start >= this.start && end <= this.end;
    }

    public final int compareTo(final ORF orf) {

      return this.start - orf.start;
    }

    public ORF(final int start, final int end, final String name) {

      this.start = start;
      this.end = end;
      this.name = name;
    }

    public String toString() {

      return this.start + "," + this.end;
    }
  }

  public ORF getORf(final Sequence sequence) {

    if (sequence == null)
      return null;

    final String seqName = sequence.getName();

    if (seqName == null)
      return null;

    if (seqName.equals(this.currentSequenceName))
      return this.currentORF;

    final ORF orf = nextORF(sequence);

    this.currentSequenceName = seqName;
    this.currentORF = orf;

    return orf;
  }

  private ORF nextORF(final Sequence sequence) {

    final String sequenceName = sequence.getName();

    if (sequenceName == null)
      return null;

    final Matcher m = seqNamePattern.matcher(sequenceName);

    if (!m.matches())
      throw new RuntimeException("Unable to parse sequence name: "
          + sequenceName);

    final String chr = m.group(1);
    final int start = Integer.parseInt(m.group(2));
    final int len = Integer.parseInt(m.group(3));
    final int end = start + len;

    final Set<ORF> chrORFs = this.orfs.get(chr);

    // Remove no more used ORFs
    if (chrORFs != this.lastOrfsChr)
      this.lastOrfsChr = chrORFs;
    else
      for (ORF o : this.orfsToRemove)
        chrORFs.remove(o);

    this.orfsToRemove.clear();

    for (ORF o : chrORFs) {

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
   * @param start1 true if the first position on sequence is 1
   * @throws IOException if an error occurs while reading data
   */
  private ORFResource(final File orfsFile, final boolean start1)
      throws IOException {

    final BufferedReader br =
        new BufferedReader(new InputStreamReader(new FileInputStream(orfsFile)));

    final Pattern p = Pattern.compile("\t");

    String line = null;

    while ((line = br.readLine()) != null) {

      // Handle comments
      if (line.startsWith("#"))
        continue;

      final String[] fields = p.split(line);
      final String orfName = fields[0];
      final String chr = fields[1];
      final int start = Integer.parseInt(fields[2]) + (start1 ? -1 : 0);
      final int end = Integer.parseInt(fields[3]) + (start1 ? -1 : 0);

      final Set<ORF> chrORFs;

      if (!this.orfs.containsKey(chr)) {
        chrORFs = new TreeSet<ORF>();
        this.orfs.put(chr, chrORFs);
      } else
        chrORFs = this.orfs.get(chr);

      chrORFs.add(new ORF(start, end, orfName));
    }

    int count = 0;
    for (String k : this.orfs.keySet())
      count += this.orfs.get(k).size();

    logger.info("Orfs readed: " + count);
  }

}
