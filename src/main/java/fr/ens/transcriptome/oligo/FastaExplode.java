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

public class FastaExplode {

  public static void fastaExplode(final File inputFile, final File outputDir,
      final String prefix, final String suffix) throws IOException {

    BufferedReader br = Util.createBufferedReader(inputFile);

    int count = 0;
    Writer os = null;
    boolean first = true;
    String line;

    while ((line = br.readLine()) != null) {

      if (first || line.startsWith(">")) {
        if (os != null)
          os.close();
        os = getOutputStream(outputDir, prefix, ++count, suffix);
        first = false;
      }

      os.write(line);
      os.write("\n");
    }

    os.close();
    br.close();
  }

  private static Writer getOutputStream(final File outputDir,
      final String prefix, final int count, final String suffix)
      throws FileNotFoundException {

    File f = new File(outputDir, prefix + "_" + count + suffix);

    return Util.createBufferedWriter(f);
  }

}
