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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class SequenceIterator extends Sequence {

  private final StringBuilder sequence = new StringBuilder();
  private final BufferedReader br;
  private String nextSequenceName;

  public boolean hasNext() {

    return nextSequenceName != null;
  }

  /**
   * Get the next sequence name.
   * @return the next sequence name
   */
  public String next() {

    String line;
    try {
      while ((line = br.readLine()) != null) {

        if (line.startsWith(">")) {

          setName(this.nextSequenceName);
          this.nextSequenceName = extractSequenceName(line);
          final String result = sequence.toString();
          this.sequence.setLength(0);

          setSequence(result.toUpperCase());
          return result;
        }

        sequence.append(line.trim());
      }
      br.close();
      this.nextSequenceName = null;

    } catch (IOException e) {

      System.err.println(e.getMessage());
      return null;
    }

    final String result = sequence.toString();

    setSequence(result);

    return result;
  }

  private static final String extractSequenceName(final String line) {

    if (line == null)
      return null;

    return line.trim().substring(1);
  }

  //
  // Constructor
  //

  /**
   * Public constructor
   * @param inputFile
   * @throws IOException
   */

  public SequenceIterator(final File inputFile) throws IOException {

    this.br = Util.createBufferedReader(inputFile);

    this.nextSequenceName = extractSequenceName(br.readLine());
  }

}
