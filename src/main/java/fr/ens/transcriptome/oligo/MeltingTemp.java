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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;

public class MeltingTemp {

  // universal gas constant in Cal/degrees C*Mol
  private static final double R = 1.987;

  public static final float tmstalucDNA(final String s, final float dnac,
      final float saltc) {

    float dh = 0; // DeltaH. Enthalpy
    float ds = 0; // deltaS Entropy

    final float R = 1.987f;
    final String sup = s.toUpperCase();

    final float[] tcRes = tercorrDNA(sup, ds, dh);
    // double vsTC = tcRes[0];
    float vs = tcRes[0];
    float vh = tcRes[1];

    // DNA/DNA
    // Allawi and SantaLucia (1997). Biochemistry 36 : 10581-10594
    vh =
        vh
            + (overcount(sup, "AA")) * 7.9f + (overcount(sup, "TT")) * 7.9f
            + (overcount(sup, "AT")) * 7.2f + (overcount(sup, "TA")) * 7.2f
            + (overcount(sup, "CA")) * 8.5f + (overcount(sup, "TG")) * 8.5f
            + (overcount(sup, "GT")) * 8.4f + (overcount(sup, "AC")) * 8.4f;
    vh =
        vh
            + (overcount(sup, "CT")) * 7.8f + (overcount(sup, "AG")) * 7.8f
            + (overcount(sup, "GA")) * 8.2f + (overcount(sup, "TC")) * 8.2f;
    vh =
        vh
            + (overcount(sup, "CG")) * 10.6f + (overcount(sup, "GC")) * 9.8f
            + (overcount(sup, "GG")) * 8f + (overcount(sup, "CC")) * 8f;
    vs =
        vs
            + (overcount(sup, "AA")) * 22.2f + (overcount(sup, "TT")) * 22.2f
            + (overcount(sup, "AT")) * 20.4f + (overcount(sup, "TA")) * 21.3f;
    vs =
        vs
            + (overcount(sup, "CA")) * 22.7f + (overcount(sup, "TG")) * 22.7f
            + (overcount(sup, "GT")) * 22.4f + (overcount(sup, "AC")) * 22.4f;
    vs =
        vs
            + (overcount(sup, "CT")) * 21.0f + (overcount(sup, "AG")) * 21.0f
            + (overcount(sup, "GA")) * 22.2f + (overcount(sup, "TC")) * 22.2f;
    vs =
        vs
            + (overcount(sup, "CG")) * 27.2f + (overcount(sup, "GC")) * 24.4f
            + (overcount(sup, "GG")) * 19.9f + (overcount(sup, "CC")) * 19.9f;
    ds = vs;
    dh = vh;

    ds = ds - 0.368f * (s.length() - 1f) * (float) Math.log(saltc / 1e3f);
    final float k = (dnac / 4.0f) * 1e-9f;

    return ((1000f * (-dh)) / (-ds + (R * ((float) Math.log(k))))) - 273.15f;
  }

  /**
   * Returns DNA/DNA tm using nearest neighbor thermodynamics
   * @param sequence
   * @param dnac
   * @param saltc
   * @param rna
   * @return
   */
  public static final float tmstalucRNA(final String s, final int dnac,
      final int saltc) {

    float dh = 0; // DeltaH. Enthalpy
    float ds = 0; // deltaS Entropy

    final float R = 1.987f;
    final String sup = s.toUpperCase();

    final float[] tcRes = tercorrRNA(sup, ds, dh);
    // double vsTC = tcRes[0];
    float vs = tcRes[0];
    float vh = tcRes[1];

    // RNA/RNA hybridisation of Xia et al (1998)
    // Biochemistry 37: 14719-14735
    vh =
        vh
            + (overcount(sup, "AA")) * 6.82f + (overcount(sup, "TT")) * 6.6f
            + (overcount(sup, "AT")) * 9.38f + (overcount(sup, "TA")) * 7.69f
            + (overcount(sup, "CA")) * 10.44f + (overcount(sup, "TG")) * 10.5f
            + (overcount(sup, "GT")) * 11.4f + (overcount(sup, "AC")) * 10.2f;
    vh =
        vh
            + (overcount(sup, "CT")) * 10.48f + (overcount(sup, "AG")) * 7.6f
            + (overcount(sup, "GA")) * 12.44f + (overcount(sup, "TC")) * 13.3f;
    vh =
        vh
            + (overcount(sup, "CG")) * 10.64f + (overcount(sup, "GC")) * 14.88f
            + (overcount(sup, "GG")) * 13.39f + (overcount(sup, "CC")) * 12.2f;
    vs =
        vs
            + (overcount(sup, "AA")) * 19.0f + (overcount(sup, "TT")) * 18.4f
            + (overcount(sup, "AT")) * 26.7f + (overcount(sup, "TA")) * 20.5f;
    vs =
        vs
            + (overcount(sup, "CA")) * 26.9f + (overcount(sup, "TG")) * 27.8f
            + (overcount(sup, "GT")) * 29.5f + (overcount(sup, "AC")) * 26.2f;
    vs =
        vs
            + (overcount(sup, "CT")) * 27.1f + (overcount(sup, "AG")) * 19.2f
            + (overcount(sup, "GA")) * 32.5f + (overcount(sup, "TC")) * 35.5f;
    vs =
        vs
            + (overcount(sup, "CG")) * 26.7f + (overcount(sup, "GC")) * 36.9f
            + (overcount(sup, "GG")) * 32.7f + (overcount(sup, "CC")) * 29.7f;
    ds = vs;
    dh = vh;

    ds = ds - 0.368f * (s.length() - 1f) * (float) Math.log(saltc / 1e3f);

    final float k = (dnac / 4.0f) * 1e-9f;

    return ((1000f * (-dh)) / (-ds + (R * ((float) Math.log(k))))) - 273.15f;

  }

  private static final float[] tercorrDNA(final String stri, final float ds,
      final float dh) {

    float deltah = 0;
    float deltas = 0;

    // DNA/DNA
    // Allawi and SantaLucia (1997). Biochemistry 36 : 10581-10594

    if (stri.startsWith("G") || stri.startsWith("C")) {
      deltah = deltah - 0.1f;
      deltas = deltas + 2.8f;
    } else if (stri.startsWith("A") || stri.startsWith("T")) {
      deltah = deltah - 2.3f;
      deltas = deltas - 4.1f;
    }
    if (stri.endsWith("G") || stri.endsWith("C")) {
      deltah = deltah - 0.1f;
      deltas = deltas + 2.8f;
    } else if (stri.endsWith("A") || stri.endsWith("T")) {
      deltah = deltah - 2.3f;
      deltas = deltas - 4.1f;
    }

    final float dhL = dh + deltah;
    final float dsL = ds + deltas;

    return new float[] {dsL, dhL};

  }

  private static final float[] tercorrRNA(final String stri, final float ds,
      final float dh) {

    float deltah = 0;
    float deltas = 0;

    // RNA
    if (stri.startsWith("G") || stri.startsWith("C")) {
      deltah = deltah - 3.61f;
      deltas = deltas - 1.5f;
    } else if (stri.startsWith("A")
        || stri.startsWith("T") || stri.startsWith("U")) {
      deltah = deltah - 3.72f;
      deltas = deltas + 10.5f;
    }

    if (stri.endsWith("G") || stri.endsWith("C")) {
      deltah = deltah - 3.61f;
      deltas = deltas - 1.5f;
    } else if (stri.endsWith("A") || stri.endsWith("T") || stri.endsWith("U")) {
      deltah = deltah - 3.72f;
      deltas = deltas + 10.5f;
    }

    final float dhL = dh + deltah;
    final float dsL = ds + deltas;

    return new float[] {dsL, dhL};
  }

  /**
   * """Returns how many p are on st, works even for overlapping"""
   */
  private static final int overcount(final String st, final String p) {

    int ocu = 0;

    int x = 0;

    while (true) {

      int i = st.indexOf(p, x);
      if (i == -1)
        break;

      ocu = ocu + 1;
      x = i + 1;
    }

    return ocu;
  }

  public static final void createTmFile(final File inputFile,
      final File outputDir) throws IOException {

    try {
      // prepare a BufferedReader for file io
      BufferedReader br = Util.createBufferedReader(inputFile);
      // BufferedReader br = new BufferedReader(new FileReader(inputFile));

      String scafold = inputFile.getName().split("_")[1];

      File f = new File(outputDir, "scaffold." + scafold + ".tm2");

      Writer writer = Util.createBufferedWriter(f);

      String format = "fasta";
      String alphabet = "dna";

      /*
       * get a Sequence Iterator over all the sequences in the file.
       * SeqIOTools.fileToBiojava() returns an Object. If the file read is an
       * alignment format like MSF and Alignment object is returned otherwise a
       * SequenceIterator is returned.
       */
      SequenceIterator iter =
          (SequenceIterator) SeqIOTools.fileToBiojava(format, alphabet, br);

      StringBuilder sb = new StringBuilder();

      int count = 0;
      while (iter.hasNext()) {

        Sequence seq = iter.nextSequence();
        final String s = seq.seqString();

        sb.append(scafold);
        sb.append("_");
        sb.append(count);
        sb.append("\t");
        sb.append(tmstalucDNA(s, 50, 50));
        sb.append("\n");

        writer.write(sb.toString());
        sb.setLength(0);

        count++;
      }

      br.close();
      writer.close();

    } catch (FileNotFoundException ex) {
      // can't find file specified by args[0]
      ex.printStackTrace();
    } catch (BioException ex) {
      // error parsing requested format
      ex.printStackTrace();
    }

  }

  public static void main(String[] args) throws IOException,
      InterruptedException {

    if (false) {
      String seqTest =
          "ctaacctaaccctaaccctaaccctaaccctaaccctaaccctaaccctaaccctaaccc";

      System.out.println(tmstalucDNA(seqTest, 50f, 50f));

      return;
    }

    // File inputDir = new File("/home/jourdren/tmp/testseq/t2");
    // File outputDir = new File("/home/jourdren/tmp/testseq/tm");

    Thread.sleep(20000);

    File inputDir = new File(args[0]);
    File outputDir = new File(args[1]);

    File[] files = inputDir.listFiles(new FilenameFilter() {

      public boolean accept(File dir, String name) {

        return name.endsWith(".fa2");
      }
    });

    for (int i = 0; i < files.length; i++) {

      createTmFile(files[i], outputDir);
    }

  }

}