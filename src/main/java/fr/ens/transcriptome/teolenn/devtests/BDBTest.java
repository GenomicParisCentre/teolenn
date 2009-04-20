package fr.ens.transcriptome.teolenn.devtests;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class BDBTest {

  Environment myDbEnvironment = null;
  Database myDatabase = null;
  private static Charset charset = Charset.forName("UTF-8");
  private static String DB_FILE = "/tmp/dbEnv";

  private void createDB() {

    try {

      // this.myDbEnvironment = new Environment(new File(DB_FILE), envConfig);
      EnvironmentConfig envConfig = new EnvironmentConfig();
      envConfig.setAllowCreate(true);

      myDbEnvironment = new Environment(new File(DB_FILE), envConfig);

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

    String aKey = "lolo";

    Object[] obj = {"toto", 12, 12.3};

    EntryBinding<Object[]> myBinding =
        TupleBinding.getPrimitiveBinding(Object[].class);

    DatabaseEntry theKey = new DatabaseEntry(aKey.getBytes(charset));
    DatabaseEntry theData = new DatabaseEntry(toBytes(obj));

    // DatabaseEntry theData = new DatabaseEntry();
    // System.out.println(myBinding);
    // myBinding.objectToEntry(obj, theData);

    try {
      myDatabase.put(null, theKey, theData);
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // Close
    try {

      if (myDatabase != null)
        myDatabase.close();

      if (myDbEnvironment != null)
        myDbEnvironment.close();

    } catch (DatabaseException dbe) {

    }

  }

  private void readDB() {

    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setAllowCreate(true);
    try {

      this.myDbEnvironment = new Environment(new File(DB_FILE), envConfig);
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

    String aKey = "lolo";

    DatabaseEntry theKey = new DatabaseEntry(aKey.getBytes(charset));
    DatabaseEntry theData = new DatabaseEntry();

    EntryBinding<Object[]> myBinding =
        TupleBinding.getPrimitiveBinding(Object[].class);

    try {
      if (myDatabase.get(null, theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {

        byte[] retData = theData.getData();

        // Object [] obj = myBinding.entryToObject(theData);
        Object[] obj = toObjectArray(retData);

        // String foundData = new String(retData, charset);
        System.out.println("found: " + Arrays.toString(obj));

      } else {
        System.out.println("Nothing found");
      }
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // Close
    try {

      if (myDatabase != null)
        myDatabase.close();

      if (myDbEnvironment != null)
        myDbEnvironment.close();

    } catch (DatabaseException dbe) {

    }

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

  private Object[] toObjectArray(final byte[] bytes) {

    try {
      ObjectInputStream in =
          new ObjectInputStream(new ByteArrayInputStream(bytes));

      return (Object[]) in.readObject();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }

  private void run() {

    createDB();
    readDB();

  }

  public static void main(String[] args) {

    BDBTest db = new BDBTest();
    db.run();

  }

}
