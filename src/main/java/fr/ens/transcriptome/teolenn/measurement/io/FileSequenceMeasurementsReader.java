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

package fr.ens.transcriptome.teolenn.measurement.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import fr.ens.transcriptome.teolenn.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.MeasurementRegistery;
import fr.ens.transcriptome.teolenn.util.FileUtils;

/**
 * This class in implements a reader for SequenceMeasurement based on simple
 * text files.
 * @author Laurent Jourdren
 */
public final class FileSequenceMeasurementsReader implements
    SequenceMeasurementsReader {

  private BufferedReader br;
  private String infoCurrentFile;
  private Measurement[] ms;

  private static final Pattern tabPattern = Pattern.compile("\t");

  private void readHeader() throws IOException {

    final String line = br.readLine();

    if (line == null)
      throw new IOException("File is empty" + this.infoCurrentFile == null
          ? "" : this.infoCurrentFile);

    final String[] mNames = tabPattern.split(line);
    this.ms = new Measurement[mNames.length - 1];

    for (int i = 1; i < mNames.length; i++) {
      Measurement m = MeasurementRegistery.getMeasurement(mNames[i]);
      if (m == null)
        throw new RuntimeException("Unknown measurement: " + mNames[i]);
      ms[i - 1] = m;
    }

  }

  /**
   * Get the next sequence measurement.
   * @return the next sequence measurement
   * @throws IOException if an error occurs while reading measurements
   */
  public SequenceMeasurements next() throws IOException {

    return next(null);
  }

  /**
   * Get the next sequence measurement. This version of the next method allow to
   * reuse previous sequence measurement object to save time and memory.
   * @return the next sequence measurement
   * @throws IOException if an error occurs while reading measurements
   */
  public SequenceMeasurements next(final SequenceMeasurements sm)
      throws IOException {

    final SequenceMeasurements result;

    if (sm == null) {

      result = new SequenceMeasurements();
      for (int i = 0; i < ms.length; i++)
        result.addMesurement(ms[i]);
      result.setArrayMeasurementValues(new Object[ms.length]);

    } else
      result = sm;

    final String line = br.readLine();

    if (line == null)
      return null;

    final String[] tokens = tabPattern.split(line);

    result.setId(Integer.parseInt(tokens[0]));
    final Object[] values = result.getArrayMeasurementValues();

    for (int i = 0; i < this.ms.length; i++)
      values[i] = ms[i].parse(tokens[i + 1]);

    return result;
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   * @param file Sequence measurement file to parse
   */
  public FileSequenceMeasurementsReader(final File file) throws IOException {

    if (file == null)
      throw new NullPointerException("File is null");

    this.infoCurrentFile = file.getName();
    this.br = FileUtils.createBufferedReader(file);

    readHeader();
  }

}
