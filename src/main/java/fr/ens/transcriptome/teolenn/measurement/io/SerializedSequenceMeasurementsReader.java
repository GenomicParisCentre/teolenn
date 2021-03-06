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

package fr.ens.transcriptome.teolenn.measurement.io;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.measurement.ChromosomeMeasurement;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.MeasurementRegistery;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.util.FileUtils;

/**
 * This class in implements a reader for SequenceMeasurement based on object
 * serialization.
 * @author Laurent Jourdren
 */
public class SerializedSequenceMeasurementsReader implements
    SequenceMeasurementsReader {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);
  private static final String SERIALIZED_FORMAT_VERSION = "TEOLENN_MES_2";

  private ObjectInputStream in;
  private Measurement[] ms;
  private int[] types;
  private final Map<Integer, String> indexChromosomeNames =
      new HashMap<Integer, String>();
  private int typeDataChromosomeName;
  private String currentChr;
  private int chrNameIndex = -1;
  private int indexChr = -1;

  /**
   * Read the header of the file.
   * @throws IOException if an error occurs while reading the header of the file
   */
  private void readHeader() throws IOException {

    try {

      // Read cookie
      final String cookie = this.in.readUTF();
      if (!SERIALIZED_FORMAT_VERSION.equals(cookie)) {
        logger.severe("Invalid measurement version file");
        throw new IOException("Invalid measurement version file");
      }

      // Read "Id" column name
      this.in.readUTF();

      // Read the names of the used measurements
      final String[] mNames = (String[]) this.in.readObject();

      this.ms = new Measurement[mNames.length];

      for (int i = 0; i < mNames.length; i++) {
        Measurement m = MeasurementRegistery.getMeasurement(mNames[i]);
        if (m == null)
          throw new RuntimeException("Unknown measurement: " + mNames[i]);
        this.ms[i] = m;
      }

      // Find the types of the measurements
      this.types = new int[this.ms.length];
      for (int i = 0; i < this.ms.length; i++) {

        final Object objType = this.ms[i].getType();

        if (Float.class == objType)
          this.types[i] = 1;
        else if (Integer.class == objType)
          this.types[i] = 3;
        else if (String.class == objType)
          this.types[i] = 5;
        else {
          logger.severe("Unknown datatype: " + objType);
          throw new IOException("Unknown datatype: " + objType);
        }
      }

      // Get the data type of chromosome names
      this.typeDataChromosomeName = this.in.readInt();

      // Get the chromosomes names
      final String[] chrNames = (String[]) this.in.readObject();
      for (int i = 0; i < chrNames.length; i++)
        this.indexChromosomeNames.put(i, chrNames[i]);

    } catch (ClassNotFoundException e) {
    }

    this.currentChr = null;
    this.chrNameIndex = -1;
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

      this.indexChr =
          result.getIndexMeasurment(ChromosomeMeasurement.MEASUREMENT_NAME);

    } else
      result = sm;

    // Read the id
    try {
      result.setId(in.readInt());
    } catch (EOFException e) {

      return null;
    }

    // Read the measurement values
    final Object[] values = result.getArrayMeasurementValues();
    final int len = this.ms.length;

    for (int i = 0; i < len; i++) {

      if (i == this.indexChr) {

        final int val;

        switch (this.typeDataChromosomeName) {

        case 2:
          val = in.readByte();
          break;
        case 4:
          val = in.readShort();
          break;
        case 3:
          val = in.readInt();
          break;
        default:
          val = -10;
          break;
        }

        if (val == chrNameIndex)
          values[i] = this.currentChr;
        else {
          chrNameIndex = val;
          this.currentChr = this.indexChromosomeNames.get(val);
          values[i] = this.currentChr;
        }

      } else
        switch (this.types[i]) {
        case 1:
          values[i] = in.readFloat();
          break;
        case 3:
          values[i] = in.readInt();
          break;
        case 5:
          values[i] = in.readUTF();
          break;
        default:
          break;
        }
    }

    return result;
  }

  /**
   * Close the reader.
   * @throws IOException if an error occurs while closing the reader
   */
  public void close() throws IOException {

    this.in.close();
  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   * @param file Sequence measurement file to parse
   */
  public SerializedSequenceMeasurementsReader(final File file)
      throws IOException {

    if (file == null)
      throw new NullPointerException("File is null");

    this.in = FileUtils.createObjectInputReader(file);

    readHeader();
  }

}
