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

package fr.ens.transcriptome.oligo.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ens.transcriptome.oligo.Sequence;
import fr.ens.transcriptome.oligo.SequenceIterator;
import fr.ens.transcriptome.oligo.SequenceWriter;
import fr.ens.transcriptome.oligo.util.FileUtils;
import fr.ens.transcriptome.oligo.util.ProcessUtils;
import fr.ens.transcriptome.oligo.util.StringUtils;

public class RedundancyFilter implements SequenceFilter {

  private static final String SOAP_PATH =
      "/export/home2/users/sgdb/jourdren/soap-align/soap";
  private static final String SOAP_ARGS = " -s 12 -v 5 -r 1 -w 1000 -p 3";

  private File baseDir;
  private BufferedReader br;
  private String currentChr;
  private final Map<Integer, Integer> currentChrResult =
      new HashMap<Integer, Integer>();

  // private static final Scanner scanner = new Scanner("\t");

//  private static final Pattern soapResultPattern =
//  // Pattern
//      // .compile("^(.*):subseq\\((\\d+),(\\d+)\\)\t.*\t.*\t(\\d+)\t.*\t(\\d+)\t([-,+])\t(.*)\t(\\d+)+\t(\\d+)$");
//
//      Pattern
//          .compile("^(.*):subseq\\((\\d+),(\\d+)\\)\t.*\t.*\t(\\d+)\t.*\t(\\d+)\t([-,+])\t(.*)\t(\\d+)+\t(\\d+).*$");

  private static final Pattern seqNamePattern =
      Pattern.compile("^(.*):subseq\\((\\d+),(\\d+)\\)$");

  public boolean accept(final Sequence sequence) {

    final String name = sequence.getName();

    return test(name);
  }

  private final void parseResultFile(final String chrSequence)
      throws IOException {

    if (this.br != null)
      this.br.close();

    this.br =
        FileUtils.createBufferedReader(new File(this.baseDir, chrSequence
            + ".sop"));

    this.currentChrResult.clear();

    Scanner s = new Scanner(br);
    s.useDelimiter("\t|\n");

    while (true)
      try {
        final String oligoName = s.next();
        final int subSeqPos = oligoName.indexOf(":subseq(");
        final int commaPos = oligoName.indexOf(",", subSeqPos);
        // final int paraPos = oligoName.indexOf(")", commaPos);

        final String chr = oligoName.substring(0, subSeqPos);
        final int oligoPos =
            Integer.parseInt(oligoName.substring(subSeqPos + 8, commaPos));
        // final int oligoLen =
        // Integer.parseInt(oligoName.substring(commaPos + 1, paraPos));

        s.next();
        s.next();

        final int nbMatches = s.nextInt();
        s.next();
        final int matchLen = s.nextInt();
        final String matchStrand = s.next();
        final String matchChr = s.next();
        final int matchStart = s.nextInt() - 1;
        final int matchType = s.nextInt();

        // System.out.println(chr
        // + "\t" + oligoPos + "\t" + nbMatches + "\t" + matchLen + "\t"
        // + matchStrand + "\t" + matchChr + "\t" + matchStart + "\t"
        // + matchType);

        if (nbMatches == 1
            && matchType == 0 && "+".equals(matchStrand)
            && chr.equals(matchChr) && oligoPos == matchStart)
          this.currentChrResult.put(oligoPos, matchLen);

        s.nextLine();
      } catch (NoSuchElementException e) {

        break;
      }

    // while ((line = br.readLine()) != null) {
    //
    //
    //      
    // final Matcher m = soapResultPattern.matcher(line);
    //
    // if (!m.matches())
    // throw new RuntimeException("Unable to parse line: " + line);
    //
    // final String chr = m.group(1);
    // final int startPos = Integer.parseInt(m.group(2));
    // // final int len = Integer.parseInt(m.group(3));
    // final int nbMatches = Integer.parseInt(m.group(4));
    // final int matchLen = Integer.parseInt(m.group(5));
    // final String matchStrand = m.group(6);
    // final String matchChr = m.group(7);
    // final int matchStart = Integer.parseInt(m.group(8)) - 1;
    // final String matchType = m.group(9);
    //
    // if (nbMatches == 1
    // && matchLen == 60 && "0".equals(matchType) && "+".equals(matchStrand)
    // && chr.equals(matchChr) && startPos == matchStart)
    // this.currentChrResult.put(startPos, matchLen);
    // }

  }

  private boolean test(final String sequenceName) {

    if (sequenceName == null)
      return false;

    final Matcher m = seqNamePattern.matcher(sequenceName);

    if (!m.matches())
      throw new RuntimeException("Unable to parse sequence name: "
          + sequenceName);

    final String chr = m.group(1);
    final int startPos = Integer.parseInt(m.group(2));
    final int len = Integer.parseInt(m.group(3));

    try {

      if (!chr.equals(this.currentChr)) {
        parseResultFile(chr);
        this.currentChr = chr;
      }

      Integer result = this.currentChrResult.get(startPos);

      return result != null && result == len;

    } catch (IOException e) {
      throw new RuntimeException("Error while reading Soap results: "
          + e.getMessage());
    }

  }

  private void createParameterFile(final File parameterFile,
      final File[] oligosFiles) throws IOException {

    FileWriter fw = new FileWriter(parameterFile);

    for (int i = 0; i < oligosFiles.length; i++) {

      fw.append("-a ");
      final String oligoFile = oligosFiles[i].getAbsolutePath();
      fw.append(oligoFile);
      fw.append(" -o ");
      fw.append(StringUtils.basename(oligoFile) + ".sop");
      fw.append(SOAP_ARGS);
      fw.append("\n");

    }

    fw.close();
  }

  //
  // Constructor
  //

  public RedundancyFilter(final File referenceFile, final File[] oligosFiles)
      throws IOException {

    File paramFile = File.createTempFile("soap-", ".param");

    createParameterFile(paramFile, oligosFiles);

    final String cmd =
        SOAP_PATH
            + " -d " + referenceFile.getAbsolutePath() + " "
            + paramFile.getAbsolutePath();

    ProcessUtils.exec(cmd);

    paramFile.delete();

    this.baseDir = oligosFiles[0].getParentFile();
  }

  private RedundancyFilter() {

  }

  public static void main(String[] args) throws IOException,
      InterruptedException {

    // Pattern p =
    // Pattern
    // .compile("^(.*):subseq\\((\\d+),(\\d+)\\)\t.*\t.*\t(\\d+)\t.*\t(\\d+)\t([-,+])\t(.*)\t(\\d+)+\t(\\d+)$");
    // String s =
    // "scaffold_22:subseq(0,60)"
    // + "\t"
    // + "AGTAAGGTATACCTTAAAAGCTATAATATAGTACCCTTTTTATATATTAAAAAAGGCATA"
    // + "\t"
    // + "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh"
    // + "\t" + "1" + "\t" + "a" + "\t" + "60" + "\t" + "+" + "\t"
    // + "scaffold_22" + "\t" + "1" + "\t" + "0";
    //
    // System.out.println(Arrays.toString(s.split("\t")));
    //
    // Matcher m = p.matcher(s);
    // System.out.println(m.matches());
    // String chr = m.group(1);
    // int startPos = Integer.parseInt(m.group(2));
    // int len = Integer.parseInt(m.group(3));
    // int nbMatches = Integer.parseInt(m.group(4));
    // int matchLen = Integer.parseInt(m.group(5));
    // String matchStrand = m.group(6);
    // String matchChr = m.group(7);
    // int matchStart = Integer.parseInt(m.group(8)) - 1;
    // int matchType = Integer.parseInt(m.group(9));
    //
    // if (nbMatches == 1
    // && matchLen == 60 && matchType == 0 && "+".equals(matchStrand)
    // && chr.equals(matchChr) && startPos == matchStart)
    //
    // System.out.println(chr
    // + "\t" + startPos + "\t" + len + "\t" + nbMatches + "\t" + matchLen
    // + "\t" + matchStrand + "\t" + matchChr + "\t" + matchStart + "\t"
    // + matchType);
    //
    // if (true)
    // return;

    Thread.sleep(0);

    long start = System.currentTimeMillis();

    RedundancyFilter uf = new RedundancyFilter();
    uf.baseDir = new File("/home/jourdren/tmp/testseq/finaltest");

    File outputFile = new File(uf.baseDir, "test.seq");
    SequenceWriter sw = new SequenceWriter(outputFile);

    SequenceIterator si =
        new SequenceIterator(new File(uf.baseDir, "scaffold_21.oligo"));

    while (si.hasNext()) {

      si.next();
      if (uf.accept(si))
        sw.write(si);

    }

    sw.close();

    long end = System.currentTimeMillis();

    System.out.println((end - start) + " ms.");

  }

}
