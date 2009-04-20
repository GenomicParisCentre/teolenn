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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.MeasurementRegistery;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsReader;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

public class DerbySequenceMeasurementsReader implements
    SequenceMeasurementsReader {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  private Measurement[] ms;
  private Connection connect;
  private ResultSet resultSet;

  public void close() throws IOException {
    // TODO Auto-generated method stub

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

    final ResultSet rs = this.resultSet;

    try {

      if (!rs.next())
        return null;

      result.setId(rs.getInt(1));
      final Object[] values = result.getArrayMeasurementValues();
      for (int i = 0; i < values.length; i++)
        values[i] = rs.getObject(i + 2);

      return result;

    } catch (SQLException e) {

      e.printStackTrace();
    }

    return null;
  }

  private void readHeader() throws IOException {

    try {

      final String query = "SELECT * from mes";
      logger.info("SQL: " + query);
      final Statement s = connect.createStatement();
      this.resultSet = s.executeQuery(query);

      ResultSetMetaData metadata = this.resultSet.getMetaData();

      // Get Column names
      String[] mNames = new String[metadata.getColumnCount()];
      for (int i = 0; i < mNames.length; i++) {
        String colomnName = metadata.getColumnName(i + 1);
        mNames[i] = colomnName;
      }

      this.ms = new Measurement[mNames.length - 1];

      for (int i = 1; i < mNames.length; i++) {
        Measurement m = MeasurementRegistery.getMeasurement(mNames[i]);
        if (m == null)
          throw new RuntimeException("Unknown measurement: " + mNames[i]);
        ms[i - 1] = m;
      }
    } catch (SQLException e) {

      e.printStackTrace();
    }

  }

  private void init(final String dbName) {

    final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    final String Dbname = dbName;
    final String conecURL = "jdbc:derby:" + Dbname + ";create=true";

    try {
      Class.forName(driver);
      this.connect = DriverManager.getConnection(conecURL);
    } catch (ClassNotFoundException e) {

      e.printStackTrace();
    } catch (SQLException e) {

      e.printStackTrace();
    }

  }

  //
  // Constructor
  //

  public DerbySequenceMeasurementsReader(final File dbDir) throws IOException {

    init(dbDir.getName());
    readHeader();
  }

}
