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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import fr.ens.transcriptome.oligo.util.UnSynchronizedBufferedWriter;

public class Util {

  public static final BufferedReader createBufferedReader(final File file)
      throws FileNotFoundException {

    if (file == null)
      return null;

    final FileInputStream inFile = new FileInputStream(file);
    final FileChannel inChannel = inFile.getChannel();

    return new BufferedReader(new InputStreamReader(Channels
        .newInputStream(inChannel)));

  }

  public static final UnSynchronizedBufferedWriter createBufferedWriter(
      final File file) throws FileNotFoundException {

    if (file == null)
      return null;

    final FileOutputStream outFile = new FileOutputStream(file);
    final FileChannel outChannel = outFile.getChannel();

    return new UnSynchronizedBufferedWriter(new OutputStreamWriter(Channels
        .newOutputStream(outChannel), Charset.forName("ISO-8859-1")));

  }
}
