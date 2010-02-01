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

package fr.ens.transcriptome.teolenn.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.util.FileUtils;

public class SequenceCore {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

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
   * @param interval interval of sizes
   * @param start1 true if the first position on sequence is 1
   * @throws IOException if an error occurs while executing fastaoverlap
   */
  public static final Map<String, Integer> fastaOverlap(final File inputFile,
      final File outputDir, final String extension, final int windowSize,
      final int interval, final boolean start1) throws IOException {

    final int minSize = windowSize - interval <= 0 ? 1 : windowSize - interval;
    final int maxSize = windowSize + interval;

    final Map<String, Integer> result = new HashMap<String, Integer>();

    BufferedReader br = null;
    try {
      br = FileUtils.createBufferedReader(inputFile);
    } catch (FileNotFoundException e) {
      logger.severe("Unable to read file: " + e.getMessage());
      throw e;
    }

    boolean first = true;
    String line;

    StringBuilder sbHeader = new StringBuilder();
    String outputFilename = null;
    String headerOutput = null;
    Writer os = null;

    int countInternal = 0;
    int count = 0;

    final StringBuilder subSeq = new StringBuilder(maxSize);

    final int firstPosition = start1 ? 1 : 0;

    int offset = firstPosition;

    while ((line = br.readLine()) != null) {

      String lineTrimed = line.trim();

      if (line.startsWith(">")) {

        if (!first) {

          writeAllSubSeq(subSeq, os, offset, minSize, maxSize, headerOutput,
              true);
          result.put(outputFilename, count);

          count = 0;
          offset = firstPosition;
          countInternal = subSeq.length();
        } else
          first = false;

        outputFilename = lineTrimed.substring(1).replace(' ', '_');
        try {
          os = getOutputStream(outputDir, outputFilename, extension);
        } catch (FileNotFoundException e) {

          logger.severe("Unable to create file: " + e.getMessage());
          throw e;
        }

        sbHeader.append(lineTrimed);
        sbHeader.append(":subseq(");
        headerOutput = sbHeader.toString();
        sbHeader.setLength(0);
      } else {

        final int lineLength = lineTrimed.length();

        subSeq.append(line);
        countInternal += lineLength;
        count += lineLength;

        if (countInternal > maxSize) {

          offset +=
              writeAllSubSeq(subSeq, os, offset, minSize, maxSize,
                  headerOutput, false);

          countInternal = 0;
        }

      }
    }

    writeAllSubSeq(subSeq, os, offset, minSize, maxSize, headerOutput, true);
    result.put(outputFilename, count);
    br.close();

    return result;
  }

  private static final int writeAllSubSeq(final StringBuilder sb,
      final Writer os, final int offset, final int minSize, final int maxSize,
      final String header, final boolean flush) throws IOException {

    final int endFor = sb.length() - maxSize + 1;
    int index = 0;

    final StringBuilder output = outputStringBuilder;

    for (index = 0; index < endFor; index++) {

      // final int end = index + maxSize;

      // final String sequence = sb.substring(index, end);

      for (int size = minSize; size <= maxSize; size++) {

        final int end = index + size;
        final String sequence = sb.substring(index, end);

        writeSeq(output, sequence, header, offset + index, size);
      }

      if (output.length() > WRITE_BUFFER_LEN) {
        os.write(output.toString());
        output.setLength(0);
      }

    }

    if (flush) {

      final int endFor2 = sb.length() - minSize + 1;

      int i = 0;
      for (; index < endFor2; index++) {

        for (int size = minSize; size < maxSize - i && index < endFor2; size++) {

          final int end = index + size;
          final String sequence = sb.substring(index, end);

          writeSeq(output, sequence, header, offset + index, size);
        }
        i++;
      }

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

    return FileUtils.createBufferedWriter(f);
  }

  public static void fastaExplode(final File inputFile, final File outputDir,
      final String prefix, final String suffix, final boolean convertXN,
      final boolean compress) throws IOException {

    BufferedReader br = FileUtils.createBufferedReader(inputFile);

    Writer os = null;
    boolean first = true;
    String line;

    while ((line = br.readLine()) != null) {

      if (first || line.startsWith(">")) {
        if (os != null)
          os.close();

        final String seqName = line.substring(1, line.length()).trim();

        os = getOutputStream(outputDir, prefix, seqName, suffix, compress);
        first = false;
      } else if (convertXN)
        line = line.replace('X', 'N');

      os.write(line);
      os.write("\n");
    }

    os.close();
    br.close();
  }

  private static Writer getOutputStream(final File outputDir,
      final String prefix, final String seqName, final String suffix,
      final boolean compress) throws IOException {

    File f = new File(outputDir, prefix + seqName + suffix);

    return compress ? FileUtils.createBufferedGZipWriter(f) : FileUtils
        .createBufferedWriter(f);
  }

}
