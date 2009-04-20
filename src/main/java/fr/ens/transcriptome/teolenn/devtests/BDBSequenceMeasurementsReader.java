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
 * or to join the Teolenn mailing list, visit the home page
 * at:
 *
 *      http://www.transcriptome.ens.fr/teolenn
 *
 */

package fr.ens.transcriptome.teolenn.devtests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.MeasurementRegistery;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsReader;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

public class BDBSequenceMeasurementsReader implements
    SequenceMeasurementsReader {

  private static Charset charset = Charset.forName("ISO-8859-1");

  private Environment myDbEnvironment = null;
  private Database myDatabase = null;
  private Cursor myCursor = null;
  private final DatabaseEntry theKey = new DatabaseEntry();
  private final DatabaseEntry theData = new DatabaseEntry();

  private Measurement[] ms;

  private void readHeader() throws IOException {

    theKey.setData(intToByteArray(-1));

    try {
      if (myDatabase.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {

        byte[] retData = theData.getData();
        final Object[] obj = toObjectArray(retData);
        System.out.println("read: " + Arrays.toString(obj));
        this.ms = new Measurement[obj.length];

        for (int i = 0; i < obj.length; i++) {
          final String mName = (String) obj[i];
          Measurement m = MeasurementRegistery.getMeasurement(mName);
          if (m == null)
            throw new RuntimeException("Unknown measurement: " + mName);
          ms[i] = m;
        }

      } else {
        System.out.println("No header found !!!");
      }
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public SequenceMeasurements next() throws IOException {

    return next(null);
  }

  public SequenceMeasurements next(SequenceMeasurements sm) throws IOException {

    final SequenceMeasurements result;

    if (sm == null) {

      result = new SequenceMeasurements();
      for (int i = 0; i < ms.length; i++)
        result.addMesurement(ms[i]);
      result.setArrayMeasurementValues(new Object[ms.length]);

    } else
      result = sm;

    try {
      if (myCursor.getNext(theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {

        int index = byteArrayToInt(theKey.getData());

        if (index == -1)
          return next(sm);

        final Object[] obj = toObjectArray(theData.getData());
        result.setId(index);

        final Object[] values = result.getArrayMeasurementValues();

        System.arraycopy(obj, 0, values, 0, obj.length);
        // To optimize with arraycopy
        // for (int i = 0; i < this.ms.length; i++)
        // values[i] = obj[i];

        if (index == 1)
          System.out.println("read: " + index + "\t" + Arrays.toString(values));

      } else {
        System.out.println("Nothing found");
      }
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return result;
  }

  private Object[] toObjectArray(final byte[] bytes) {

    try {
      ObjectInputStream in =
          new ObjectInputStream(new ByteArrayInputStream(bytes));

      final Object[] result = (Object[]) in.readObject();

      in.close();

      return result;

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }

  public void close() throws IOException {
    // TODO Auto-generated method stub

    try {

      if (this.myCursor != null)
        this.myCursor.close();

      if (this.myDatabase != null)
        this.myDatabase.close();

      if (this.myDbEnvironment != null)
        this.myDbEnvironment.close();

    } catch (DatabaseException dbe) {
      dbe.printStackTrace();
    }
  }

  private static final byte[] intToByteArray(int value) {
    return new byte[] {(byte) (value >>> 24), (byte) (value >>> 16),
        (byte) (value >>> 8), (byte) value};
  }

  private static final int byteArrayToInt(byte[] b) {
    return (b[0] << 24)
        + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
  }

  //
  // Constructor
  //

  public BDBSequenceMeasurementsReader(final File dbDir) throws IOException {

    if (!dbDir.exists())
      if (!dbDir.mkdirs())
        throw new IOException("Unable to create db directory");

    if (!dbDir.isDirectory())
      throw new IOException("Not a directory");

    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setAllowCreate(true);
    try {

      this.myDbEnvironment = new Environment(dbDir, envConfig);
      DatabaseConfig dbConfig = new DatabaseConfig();
      dbConfig.setAllowCreate(true);
      myDatabase =
          myDbEnvironment.openDatabase(null, "sampleDatabase", dbConfig);

    } catch (EnvironmentLockedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    readHeader();

    try {
      this.myCursor = myDatabase.openCursor(null, null);
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}
