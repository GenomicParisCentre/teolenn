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

import fr.ens.transcriptome.teolenn.measurement.io.FileSequenceMeasurementsReader;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsReader;
import fr.ens.transcriptome.teolenn.measurement.io.SequenceMeasurementsWriter;
import fr.ens.transcriptome.teolenn.measurement.io.SerializedSequenceMeasurementsReader;
import fr.ens.transcriptome.teolenn.measurement.io.SerializedSequenceMeasurementsWriter;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;

public class WriterTest {

  public static void main(String[] args) throws IOException {

    File dir = new File("oligo.mes");

    if (dir.exists())
      dir.delete();

    final long start = System.currentTimeMillis();
    
    SequenceMeasurementsWriter w =
        new SerializedSequenceMeasurementsWriter(dir);
    SequenceMeasurementsReader r =
        new FileSequenceMeasurementsReader(new File(
            "/home/jourdren/tmp/design_candida2/filtered.mes"));

    SequenceMeasurements sm = null;

    while ((sm = r.next(sm)) != null)
      w.writeSequenceMesurement(sm);

    w.close();
    r.close();

    final long end = System.currentTimeMillis();
    
    System.out.println( (end-start) + " ms");
    
//    r = new SerializedSequenceMeasurementsReader(dir);
//    r.next();
//    r.next();
//
//    r.close();

    //System.exit(0);
  }

}
