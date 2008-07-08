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

import java.io.File;
import java.io.IOException;
import java.io.Writer;

public class SequenceWriter {

  private static final int FASTA_MAX_LEN = 70;
  private Writer writer;
  private final StringBuffer sb = new StringBuffer();

  public void write(final Sequence sequence) throws IOException {

    this.sb.append(">");
    this.sb.append(sequence.getName());
    this.sb.append("\n");

    final String s = sequence.getSequence();

    int pos = 0;
    final int len = s.length();

    while (pos < len) {

      final int newPos = pos + FASTA_MAX_LEN;

      this.sb.append(s.substring(pos, newPos > len ? len : newPos));
      this.sb.append("\n");

      pos = newPos;
    }

    this.writer.write(sb.toString());
    this.sb.setLength(0);
  }

  public void close() throws IOException {

    this.writer.close();
  }

  //
  // Constructor
  //

  public SequenceWriter(File outputFile) throws IOException {

    this.writer =  Util.createBufferedWriter(outputFile);
  }

}
