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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class DerbyTest {

  public static void main(String[] args) {

    String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    String Dbname = "essai";
    String conecURL = "jdbc:derby:" + Dbname + ";create=true";
    try {
      Class.forName(driver);
      Connection connect = DriverManager.getConnection(conecURL);

      Statement s = connect.createStatement();
      boolean r;
      // r =
      // s
      // .execute("CREATE TABLE FIRSTTABLE (ID INT PRIMARY KEY, NAME VARCHAR(12))");
      // System.out.println("r=" + r);

      r =
          s
              .execute("INSERT INTO firsttable VALUES  (40,'TEN'), (50,'TWEENTY'), (60,'THIRTY')");
      System.out.println("r=" + r);

      ResultSet rs = s.executeQuery("SELECT * FROM firsttable WHERE id=20");
      ResultSetMetaData rsmd = rs.getMetaData();
      int colomnCount = rsmd.getColumnCount();
      for (int i = 1; i <= colomnCount; i++) {
        System.out.print(rsmd.getColumnName(i) + "\t");
      }
      while (rs.next()) {
        System.out.print("-");
        for (int i = 1; i <= colomnCount; i++) {
          System.out.print(rs.getObject(i) + "\t");
        }
        System.out.println("");
      }

    } catch (java.lang.ClassNotFoundException e) {
      System.out.println("Erreur de chargement des pilotes");
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
