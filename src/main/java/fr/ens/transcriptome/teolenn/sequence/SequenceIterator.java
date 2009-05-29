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

package fr.ens.transcriptome.teolenn.sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.util.FileUtils;

/**
 * This class define a iterator over sequence stored in a fasta file.
 * @author Laurent Jourdren
 */
public class SequenceIterator extends Sequence {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  private File inputFile;
  private final StringBuilder sequence = new StringBuilder();
  private final BufferedReader br;
  private String nextSequenceName;

  /**
   * Test if the file contains another sequence
   * @return true if the file contains another sequence
   */
  public boolean hasNext() {

    return nextSequenceName != null;
  }

  /**
   * Get the next sequence name and set the sequence fields.
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

          // setSequence(result.toUpperCase());
          setSequence(result);

          return result;
        }

        sequence.append(line.trim());
      }
      br.close();
      setName(this.nextSequenceName);
      this.nextSequenceName = null;

    } catch (IOException e) {

      System.err.println("Error while reading fasta file ("
          + inputFile + "): " + e.getMessage());
      logger.severe("Error while reading fasta file ("
          + inputFile + "): " + e.getMessage());
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
   * Public constructor.
   * @param inputFile Fasta file to read
   * @throws IOException if an error occurs while reading the file
   */
  public SequenceIterator(final File inputFile) throws IOException {

    this.inputFile = inputFile;
    this.br = FileUtils.createBufferedReader(inputFile);

    try {
      this.nextSequenceName = extractSequenceName(br.readLine());
    } catch (IOException e) {
      System.err.println("Error while reading first line of the fasta file ("
          + inputFile + "): " + e.getMessage());
      throw new IOException(
          "Error while reading first line of the fasta file ("
              + inputFile + "): " + e.getMessage());
    }
  }

}
