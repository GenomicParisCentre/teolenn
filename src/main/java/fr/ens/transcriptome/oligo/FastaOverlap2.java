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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import fr.ens.transcriptome.oligo.util.FileUtils;

public class FastaOverlap2 {

  private static final int FASTA_MAX_LEN = 70;
  private static final int WRITE_BUFFER_LEN = 1000000;
  private static final StringBuilder outputStringBuilder =
      new StringBuilder(WRITE_BUFFER_LEN + 50000);

  /**
   * Execute a fasta overlap
   * @param inputFile input file
   * @param outputDir output directory
   * @param extension Name of the extension
   * @param windowSize size of the window
   * @throws IOException if an error occurs while executing fastaoverlap
   */
  public static final void fastaOverlap(final File inputFile,
      final File outputDir, final String extension, final int windowSize)
      throws IOException {

    BufferedReader br = FileUtils.createBufferedReader(inputFile);

    boolean first = true;
    String line;

    StringBuilder sbHeader = new StringBuilder();
    String outputFilename = null;
    String headerOutput = null;
    Writer os = null;

    int countInternal = 0;

    final StringBuilder subSeq = new StringBuilder(windowSize);

    int offset = 0;

    while ((line = br.readLine()) != null) {

      String lineTrimed = line.trim();

      if (line.startsWith(">")) {

        if (!first) {

          writeAllSubSeq(subSeq, os, offset, windowSize, headerOutput, true);
          //os.close();
          offset = 0;
          countInternal = subSeq.length();
        } else
          first = false;

        outputFilename = lineTrimed.substring(1).replace(' ', '_');
        os = getOutputStream(outputDir, outputFilename, extension);

        sbHeader.append(lineTrimed);
        sbHeader.append(":subseq(");
        headerOutput = sbHeader.toString();
        sbHeader.setLength(0);
      } else {

        final int lineLength = lineTrimed.length();

        subSeq.append(line);
        countInternal += lineLength;

        if (countInternal > windowSize) {

          offset +=
              writeAllSubSeq(subSeq, os, offset, windowSize, headerOutput,
                  false);

          countInternal = 0;
        }

      }
    }

    writeAllSubSeq(subSeq, os, offset, windowSize, headerOutput, true);

    br.close();
    //os.close();

  }

  private static final int writeAllSubSeq(final StringBuilder sb,
      final Writer os, final int offset, final int size,
      final String header, final boolean flush) throws IOException {

    final int endFor = sb.length() - size + 1;
    int index = 0;

    final StringBuilder output = outputStringBuilder;

    for (index = 0; index < endFor; index++) {

      final int end = index + size;

      final String sequence = sb.substring(index, end).toString();

      writeSeq(output, sequence, header, offset + index, size);

      if (output.length() > WRITE_BUFFER_LEN) {
        os.write(output.toString());
        output.setLength(0);
      }

    }

    if (flush) {

      os.write(output.toString());
      output.setLength(0);
      os.close();
      // sb.delete(0, index+size);
      sb.setLength(0);

    } else
      sb.delete(0, index);

    return index;
  }

  private static final void writeSeq(final StringBuilder sb, String sequence,
      final String header, final int start, final int size) throws IOException {

    sb.append(header);
    sb.append(start);
    sb.append(",");
    sb.append(size);
    sb.append(")\n");

    final int seqLen = sequence.length();

    for (int i = 0; i < seqLen; i += FASTA_MAX_LEN) {

      final int endPos = i + FASTA_MAX_LEN;
      sb.append(sequence.subSequence(i, endPos > seqLen ? seqLen : endPos));
      sb.append("\n");
    }

  }

  private static final Writer getOutputStream(final File outputDir,
      final String prefix, final String suffix) throws FileNotFoundException {

    File f = new File(outputDir, prefix + suffix);

    return  FileUtils.createBufferedWriter(f);
  }

  public static void main(final String[] args) throws IOException {

    File inputFile =
        new File("/home/jourdren/tmp/testseq/t2/trichoderma.fasta");
    // File inputFile = new File("/home/jourdren/tmp/testseq/t2/end.fasta");

    File outputDir = new File("/home/jourdren/tmp/testseq/t2");

    fastaOverlap(inputFile, outputDir, ".fa2", 60);
  }

}
