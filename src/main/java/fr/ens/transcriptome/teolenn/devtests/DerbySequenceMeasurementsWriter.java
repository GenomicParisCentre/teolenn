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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsWriter;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

public class DerbySequenceMeasurementsWriter implements
    SequenceMeasurementsWriter {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  private boolean headerDone;
  private Connection connect;
  private PreparedStatement preparedStatement;
  private long last;
  private List<Long> times = new ArrayList<Long>();

  private void writeHeader(SequenceMeasurements sm) throws IOException {

    if (headerDone)
      return;

    final StringBuilder sbCreate = new StringBuilder();

    // sbCreate.append("CREATE TABLE mes (id INT PRIMARY KEY");
    sbCreate.append("CREATE TABLE mes (id INT");

    final List<Measurement> lms = sm.getMeasurements();

    for (Measurement m : lms) {

      sbCreate.append(", ");
      sbCreate.append(m.getName());
      sbCreate.append(" ");

      Object type = m.getType();
      if (Float.class == type)
        sbCreate.append("REAL");
      else if (Integer.class == type)
        sbCreate.append("INTEGER");
      else if (String.class == type)
        sbCreate.append("VARCHAR(255)");
    }
    sbCreate.append(")");

    Statement s;
    try {
      s = connect.createStatement();

      logger.info("SQL: " + sbCreate.toString());
      s.execute(sbCreate.toString());

      // final String query = "CREATE INDEX idIndex ON mes (id)";
      // logger.info("SQL: " + query);
      // s.execute(query);

    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    StringBuilder sbFunc = new StringBuilder();

    sbFunc.append("CREATE FUNCTION externalmes\n()\nRETURNS TABLE\n(");
    for (Measurement m : lms) {

      sbCreate.append("  ");
      sbCreate.append(m.getName());
      sbCreate.append(" ");

      Object type = m.getType();
      if (Float.class == type)
        sbCreate.append("REAL");
      else if (Integer.class == type)
        sbCreate.append("INTEGER");
      else if (String.class == type)
        sbCreate.append("VARCHAR(255)");

      sbCreate.append("\n");
    }
    sbCreate
        .append(")\nLANGUAGE JAVA\nPARAMETER STYLE DERBY_JDBC_RESULT_SET\n"
            + "READS SQL DATA\nEXTERNAL NAME 'com.acme.hrSchema.EmployeeTable.read'");

    try {
      s = connect.createStatement();

      logger.info("SQL: " + sbCreate.toString());
      s.execute(sbCreate.toString());

      // final String query = "CREATE INDEX idIndex ON mes (id)";
      // logger.info("SQL: " + query);
      // s.execute(query);

    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    final StringBuilder sbInsert = new StringBuilder();
    sbInsert.append("INSERT INTO mes (id");

    for (Measurement m : lms) {
      sbInsert.append(",");
      sbInsert.append(m.getName());
    }
    sbInsert.append(") VALUES (?");

    final int size = lms.size();
    for (int i = 0; i < size; i++)
      sbInsert.append(",?");
    sbInsert.append(")");

    logger.info("SQL: " + sbInsert.toString());

    try {
      this.preparedStatement =
          this.connect.prepareStatement(sbInsert.toString());

    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    this.headerDone = true;
    this.last = System.currentTimeMillis();
  }

  public void close() throws IOException {

    try {
      this.preparedStatement.executeBatch();
      this.connect.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public void writeSequenceMesurement(SequenceMeasurements sm)
      throws IOException {

    if (!headerDone)
      writeHeader(sm);

    try {

      final int id = sm.getId();

      this.preparedStatement.setInt(1, id);

      final Object[] values = sm.getArrayMeasurementValues();

      for (int i = 0; i < values.length; i++)
        this.preparedStatement.setObject(i + 2, values[i]);

      // this.preparedStatement.executeUpdate();
      this.preparedStatement.addBatch();

      if ((id % 100000) == 0)
        this.preparedStatement.executeBatch();

      if ((id % 100000) == 0) {
        final long last = System.currentTimeMillis();
        final long duration = last - this.last;
        this.times.add(duration);

        logger.info("write #"
            + sm.getId() + "\t" + (duration / 1000.0) + "s\t" + mean());
        this.last = last;
      }

    } catch (SQLException e) {

      e.printStackTrace();
    }

  }

  private void init(final String dbName) {

    final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    final String Dbname = dbName;
    final String conecURL = "jdbc:derby:" + Dbname;

    final Properties myProps = new Properties();
    myProps.put("create", "true");
    // System.setProperty("derby.storage.pageCacheSize", "" + (4 * 1024 *
    // 1024));
    System.setProperty("derby.system.durability", "test");
    System.setProperty("derby.storage.pageSize", "32768");
    // System.setProperty("derby.storage.minimumRecordSize", "128");
    System.setProperty("derby.database.defaultConnectionMode", "noAccess");

    try {
      Class.forName(driver);
      this.connect = DriverManager.getConnection(conecURL, myProps);
    } catch (ClassNotFoundException e) {

      e.printStackTrace();
    } catch (SQLException e) {

      e.printStackTrace();
    }

  }

  private String mean() {

    long sum = 0;

    for (long l : this.times)
      sum += l;

    return (sum / this.times.size() / 1000.0) + "s";
  }

  //
  // Constructor
  //

  public DerbySequenceMeasurementsWriter(final File dbDir) {

    init(dbDir.getName());
  }

}
