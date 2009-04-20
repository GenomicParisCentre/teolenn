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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;

import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsWriter;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

public class BDBSequenceMeasurementsWriter implements
    SequenceMeasurementsWriter {

  private static Charset charset = Charset.forName("ISO-8859-1");

  private Environment myDbEnvironment = null;
  private Database myDatabase = null;
  private final DatabaseEntry theKey = new DatabaseEntry();
  private final DatabaseEntry theData = new DatabaseEntry();

  private boolean headerDone;

  private void writeHeader(SequenceMeasurements sm) throws IOException {

    if (headerDone)
      return;

    final Object[] obj = new Object[sm.size()];

    final String[] names = sm.getArrayMesurementNames();

    for (int i = 0; i < names.length; i++)
      obj[i] = names[i];

    theKey.setData(intToByteArray(-1));
    theData.setData(toBytes(obj));

    System.out.println("write " + Arrays.toString(obj));

    try {
      myDatabase.put(null, theKey, theData);
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    this.headerDone = true;
  }

  public void close() throws IOException {
    // TODO Auto-generated method stub

    try {

      if (this.myDatabase != null)
        this.myDatabase.close();

      if (this.myDbEnvironment != null)
        this.myDbEnvironment.close();

    } catch (DatabaseException dbe) {
      dbe.printStackTrace();
    }
  }

  public void writeSequenceMesurement(final SequenceMeasurements sm)
      throws IOException {

    if (!headerDone)
      writeHeader(sm);

    theKey.setData(intToByteArray(sm.getId()));
    theData.setData(toBytes(sm.getArrayMeasurementValues()));

    if (sm.getId() == 1)
      System.out
          .println("write "
              + sm.getId() + "\t"
              + Arrays.toString(sm.getArrayMeasurementValues()));

    try {
      myDatabase.put(null, theKey, theData);
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private static final byte[] intToByteArray(int value) {
    return new byte[] {(byte) (value >>> 24), (byte) (value >>> 16),
        (byte) (value >>> 8), (byte) value};
  }

  private byte[] toBytes(final Object[] obj) {

    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutput out = new ObjectOutputStream(bos);
      out.writeObject(obj);
      out.close();

      return bos.toByteArray();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  //
  // Constructor
  //

  public BDBSequenceMeasurementsWriter(final File dbDir) throws IOException {

    if (!dbDir.exists())
      if (!dbDir.mkdirs())
        throw new IOException("Unable to create db directory");

    if (!dbDir.isDirectory())
      throw new IOException("Not a directory");

    try {

      // this.myDbEnvironment = new Environment(new File(DB_FILE), envConfig);
      EnvironmentConfig envConfig = new EnvironmentConfig();
      envConfig.setAllowCreate(true);

      myDbEnvironment = new Environment(dbDir, envConfig);

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

  }
}
