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
import java.io.Writer;

import fr.ens.transcriptome.oligo.util.FileUtils;

public class FastaExplode {

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
