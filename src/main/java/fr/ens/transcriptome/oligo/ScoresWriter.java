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

import fr.ens.transcriptome.oligo.util.FileUtils;

public final class ScoresWriter {

  public static ScoresWriter singleton;

  private Writer writer;

  public Writer getWriter() {

    return writer;
  }

  public static ScoresWriter getSingleton() {

    return singleton;
  }

  public ScoresWriter(File file) throws IOException {

    if (file == null)
      throw new NullPointerException("File is null");

    this.writer = FileUtils.createBufferedWriter(file);
    singleton = this;
  }

}
