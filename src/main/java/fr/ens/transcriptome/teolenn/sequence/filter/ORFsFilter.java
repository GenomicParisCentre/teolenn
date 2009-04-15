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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.sequence.Sequence;

/**
 * This class test if a sequence in an ORF.
 * @author Laurent Jourdren
 */
public class ORFsFilter implements SequenceFilter {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  // Regex to retrieve chromosome, startPos and len of a sequence from its name
  private static final Pattern seqNamePattern =
      Pattern.compile("^(.*):subseq\\((\\d+),(\\d+)\\)$");

  private File orfsFile;
  private Map<String, Set<ORF>> orfs = new HashMap<String, Set<ORF>>();
  private final Set<ORF> orfsToRemove = new HashSet<ORF>();
  private Set<ORF> lastOrfsChr = null;

  public static final class ORF implements Comparable<ORF> {

    public int start;
    public int end;

    public final boolean isOligoInsideORF(final int start, final int end) {

      return start >= this.start && end <= this.end;
    }

    public final int compareTo(final ORF orf) {

      return this.start - orf.start;
    }

    public ORF(final int start, final int end) {

      this.start = start;
      this.end = end;
    }

    public String toString() {

      return this.start + "," + this.end;

    }

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
        return true;
    }

    return false;
  }

  /**
   * Set a parameter for the filter.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  public void setInitParameter(final String key, final String value) {

    if ("orfsfile".equals(key))
      this.orfsFile = new File(value);
  }

  /**
   * Run the initialization phase of the parameter.
   * @throws IOException
   */
  public void init() throws IOException {

    final BufferedReader br =
        new BufferedReader(new InputStreamReader(new FileInputStream(
            this.orfsFile)));

    final Pattern p = Pattern.compile("\t");

    String line = null;

    while ((line = br.readLine()) != null) {

      // Handle comments
      if (line.startsWith("#"))
        continue;

      final String[] fields = p.split(line);
      final String chr = fields[1];
      final int start = Integer.parseInt(fields[2]);
      final int end = Integer.parseInt(fields[3]);

      final Set<ORF> chrORFs;

      if (!this.orfs.containsKey(chr)) {
        chrORFs = new TreeSet<ORF>();
        this.orfs.put(chr, chrORFs);
      } else
        chrORFs = this.orfs.get(chr);

      chrORFs.add(new ORF(start, end));
    }

    int count = 0;
    for (String k : this.orfs.keySet())
      count += this.orfs.get(k).size();

    logger.info("Orfs readed: " + count);

  }

  public static void main(String[] args) {

    ORF o = new ORF(100, 200);

    for (int i = 60; i < 240; i += 20)
      System.out.println(i + ":" + o.isOligoInsideORF(i, i + 60));

  }

}
