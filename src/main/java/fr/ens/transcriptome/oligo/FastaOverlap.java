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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FastaOverlap {

  private static final byte EOL = 10;
  private static final byte MIN_UPPER_CASE_ALPHA = 65;
  private static final byte MAX_UPPER_CASE_ALPHA = 90;
  private static final byte MIN_LOWER_CASE_ALPHA = 97;
  private static final byte MAX_LOWER_CASE_ALPHA = 122;
  private static final byte GREATER_THAN = 62;

  private static final int FASTA_MAX_LEN = 70;
  private static final byte[] MSG_SUBSEQ = " :subseq(".getBytes();

  public static void fastaOverlap(final File inputFile, final File outputDir,
      final String suffix, final int size) throws IOException {

    FileInputStream inFile = new FileInputStream(inputFile);
    FileChannel inChannel = inFile.getChannel();

    MappedByteBuffer map =
        inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inputFile.length());

    boolean header = false;
    final StringBuilder sb = new StringBuilder();
    final ByteBuffer bb = ByteBuffer.allocate(size);
    int countSeq = 0;
    int countSubseq = 0;
    int bufferFree = size;
    byte[] headerData = null;
    String seqName = null;

    FileChannel outChannel = null;

    for (long i = 0; i < inputFile.length(); i++) {

      final byte b = map.get();

      if (b == GREATER_THAN)
        header = true;

      if (header) {

        if (outChannel != null) {
          outChannel.close();
          outChannel = null;
        }

        if (b == EOL) {
          header = false;
          final String headerString = sb.toString().trim();
          sb.setLength(0);
          headerData = headerString.getBytes();
          seqName = headerString.substring(1).replace(' ', '_');

          countSeq++;
          countSubseq = 0;
          bufferFree = size;

          outChannel = createFile(outputDir, seqName, suffix, 1);
          bb.clear();

        }

        sb.append((char) b);

      } else if ((MIN_UPPER_CASE_ALPHA <= b && b <= MAX_UPPER_CASE_ALPHA)
          || (MIN_LOWER_CASE_ALPHA <= b && b <= MAX_LOWER_CASE_ALPHA)) {

        if (bufferFree != 0) {
          bb.put(b);
          bufferFree--;
        } else {

          bb.position(1);
          // ByteBuffer bbTmp = bb.slice().duplicate();
          ByteBuffer bbTmp = bb.slice();
          bb.position(0);
          bb.put(bbTmp);
          bb.put(b);

          writeSubSeq(outChannel, bb, headerData, ++countSubseq, countSubseq
              + size);

        }

      }

    }
    outChannel.close();

  }

  private static FileChannel createFile(final File outputDir,
      final String prefix, final String suffix, final int countSubseq)
      throws FileNotFoundException {

    final StringBuilder sb = new StringBuilder();

    sb.append(prefix);
    sb.append("-");
    sb.append(countSubseq);
    sb.append(suffix);

    final FileOutputStream outFile =
        new FileOutputStream(new File(outputDir, sb.toString()));

    return outFile.getChannel();
  }

  private static void writeSubSeq(FileChannel outChannel, final ByteBuffer bb,
      final byte[] header, final long subSeqStart, final long subSeqEnd)
      throws IOException {

    // Write header

    outChannel.write(ByteBuffer.wrap(header));
    outChannel.write(ByteBuffer.wrap(MSG_SUBSEQ));
    outChannel.write(ByteBuffer.wrap(Long.toString(subSeqStart).getBytes()));
    outChannel.write(ByteBuffer.wrap(new byte[] {(byte) ','}));
    outChannel.write(ByteBuffer.wrap(Long.toString(subSeqEnd).getBytes()));
    outChannel.write(ByteBuffer.wrap(new byte[] {(byte) ')', (byte) '\n'}));

    // Write data

    bb.flip();
    final int max = bb.limit();

    int count = 0;
    byte[] writeBuf = new byte[FASTA_MAX_LEN];

    while (count < max) {

      if (count > 0)
        outChannel.write(ByteBuffer.wrap(new byte[] {(byte) '\n'}));

      final int nextCount = count + FASTA_MAX_LEN;
      final int len = nextCount > max ? nextCount - max : FASTA_MAX_LEN;

      bb.get(writeBuf, 0, len);
      outChannel.write(ByteBuffer.wrap(writeBuf, 0, len));

      count = nextCount;
    }

    outChannel.write(ByteBuffer.wrap(new byte[] {(byte) '\n'}));

  }

  public static void main(final String[] args) throws IOException {

    File inputFile =
        new File("/home/jourdren/tmp/testseq/t2/trichoderma.fasta");
    File outputDir = new File("/home/jourdren/tmp/testseq/t2");

    fastaOverlap(inputFile, outputDir, ".fa2", 260);
  }

}
