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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import fr.ens.transcriptome.oligo.measurement.Measurement;
import fr.ens.transcriptome.oligo.util.FileUtils;

/**
 * This class define a reader for statistics of sequences measurements.
 * @author Laurent Jourdren
 */
public class SequenceMeasurementsStatReader {

  private BufferedReader br;
  private SequenceMeasurements sm;

  private static final Pattern tabPattern = Pattern.compile("\t");

  /**
   * Read statistics.
   * @return the SequenceMeasurements arguments of the constructor
   * @throws IOException if an error occurs while reading data
   */
  public SequenceMeasurements read() throws IOException {

    final SequenceMeasurements sm = this.sm;

    Measurement[] arrayMs = null;

    String line;
    boolean first = true;

    while ((line = br.readLine()) != null) {

      String[] fields = tabPattern.split(line);

      if (first) {
        final List<Measurement> listMs = new ArrayList<Measurement>();
        for (int i = 1; i < fields.length; i++)
          listMs.add(sm.getMeasurement(fields[i]));

        arrayMs = listMs.toArray(new Measurement[0]);
        first = false;
      } else {

        final String property = fields[0];

        for (int i = 1; i < fields.length; i++) {
          final String value = fields[i].trim();
          final Measurement m = arrayMs[i - 1];

          if (m != null && !"".equals(value))
            m.setProperty(property, value);
        }

      }

    }

    br.close();

    return sm;
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   * @param file File to read
   * @param sm a SequenceMeasurement from data used to generate statistics
   */
  public SequenceMeasurementsStatReader(final File file,
      final SequenceMeasurements sm) throws IOException {

    if (file == null)
      throw new NullPointerException("File is null");

    if (sm == null)
      throw new NullPointerException("SequenceMeasurements is null");

    this.br = FileUtils.createBufferedReader(file);
    this.sm = sm;
  }

}
