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

package fr.ens.transcriptome.oligo.measurement;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ens.transcriptome.oligo.FastaExplode;
import fr.ens.transcriptome.oligo.Sequence;
import fr.ens.transcriptome.oligo.Settings;
import fr.ens.transcriptome.oligo.util.FileUtils;
import fr.ens.transcriptome.oligo.util.ProcessUtils;
import fr.ens.transcriptome.oligo.util.StringUtils;

/**
 * This class define a measurement that compute the unicity of a sequence.
 * @author Laurent Jourdren
 */
public final class UnicityMeasurement extends FloatMeasurement {

  private static final String SEQ_GZ_WITHOUT_X_EXTENSION = ".seqX.fa.gz";
  private static final String MUP_EXTENSION = ".mup";
  private static final String MUP_DIR = "mup";
  private static final String IDX_DIR = "idx";
  private static final String FMIDX_DIR = "fmidx";

  private int oligoLength = 60;

  private File baseDir;
  private String currentChr;
  private Map<Integer, Integer> mupDict = new HashMap<Integer, Integer>();
  private double uniquenessMax;

  /* Dictionary to store mup en positions. Here only for speed optimization */
  private final Map<Integer, Integer> mupEnd = new HashMap<Integer, Integer>();

  // "/home/jourdren/local/bin/gt";

  private static final Pattern seqNamePattern =
      Pattern.compile("^(.*):subseq\\((\\d+),(\\d+)\\)$");

  /**
   * Calc the measurement of a sequence.
   * @param sequence the sequence to use for the measurement
   * @return a float value
   */
  protected float calcFloatMeasurement(final Sequence sequence) {

    final String sequenceName = sequence.getName();

    final Matcher m = seqNamePattern.matcher(sequenceName);

    if (!m.matches())
      throw new RuntimeException("Unable to parse sequence name: "
          + sequenceName);

    final String chr = m.group(1);
    final int startPos = Integer.parseInt(m.group(2));

    try {

      if (!chr.equals(this.currentChr)) {
        parseResultFile(chr);
        this.currentChr = chr;
      }

    } catch (IOException e) {
      throw new RuntimeException("Error while reading mup results: "
          + e.getMessage());
    }

    return uscoreCalculation(startPos); // this.mup_dict.get(startPos);
  }

  /**
   * Get the name of the measurement.
   * @return the name of the measurement
   */
  public String getName() {

    return "Unicity";
  }

  /**
   * Get the description of the measurement.
   * @return the description of the measurement
   */
  public String getDescription() {

    return "Unicity Measurement";
  }

  /**
   * Get the score for the measurement.
   * @param value value
   * @return the score
   */
  public float getScore(final Object value) {

    final double uniqueness = ((Float) value).doubleValue();

    return (float) (uniqueness / this.uniquenessMax);
  }

  /**
   * Set a property of the measurement.
   * @param key key of the property to set
   * @param value value of the property to set
   */
  public void setProperty(final String key, final String value) {

    if (key == null || value == null)
      return;

    if ("max".equals(key)) {
      this.uniquenessMax = Double.parseDouble(value);

    } else
      super.setProperty(key, value);
  }

  /**
   * Build fmindex.
   * @param params parameters files
   * @throws IOException if an error occurs while running gt
   */
  private void build_fmindex(final File[] params) throws IOException {

    File idxDir = new File(this.baseDir, IDX_DIR);

    if (!idxDir.isDirectory())
      if (!idxDir.mkdirs())
        throw new IOException("Can't create directory for index directory: "
            + idxDir.getAbsolutePath());

    // Generating indices

    String[] idxOri = {"rev", "cpl"};

    for (int i = 0; i < params.length; i++) {

      File f = params[i];

      String idxName = StringUtils.basename(f.getName());

      // Use parallel executions
      final ProcessUtils.ParalellExec pexec =
          new ProcessUtils.ParalellExec(2, 3);

      for (int j = 0; j < idxOri.length; j++) {

        String o = idxOri[j];

        String cmd =
            Settings.getGenomeToolsPath()
                + " suffixerator -db " + f + " -dir " + o + " -indexname "
                + idxDir.getAbsolutePath() + File.separator + idxName + "." + o
                + " -dna -pl -suf -tis -lcp -bwt";

        pexec.addTask(cmd);
      }

      pexec.execTasks();

    }

    // Generating FMINDEX

    File fmidxDir = new File(this.baseDir, FMIDX_DIR);

    if (!fmidxDir.isDirectory())
      if (!fmidxDir.mkdirs())
        throw new IOException("Can't create directory for fm index: "
            + fmidxDir.getAbsolutePath());

    String BASENAME = "scaffold";

    File fmidxFile = new File(fmidxDir, BASENAME);

    File[] filesIndex = FileUtils.listFilesByExtension(idxDir, ".cpl.prj");

    Arrays.sort(filesIndex);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < filesIndex.length; i++) {

      for (int j = 0; j < idxOri.length; j++) {

        String o = idxOri[j];

        sb.append(StringUtils.basename(filesIndex[i].getAbsolutePath()));
        sb.append(".");
        sb.append(o);
        sb.append(" ");
      }

    }

    final String index = sb.toString();

    final String cmd2 =
        Settings.getGenomeToolsPath()
            + " mkfmindex -fmout " + fmidxFile
            + " -size small -noindexpos -ii " + index;

    ProcessUtils.exec(cmd2);

    final String cmd3 =
        Settings.getGenomeToolsPath()
            + " suffixerator -plain -tis -indexname " + fmidxFile + " -smap "
            + fmidxFile + ".al1 -db " + fmidxFile + ".bwt";

    ProcessUtils.exec(cmd3);
  }

  /**
   * Build unique sub.
   * @param filesToProcess Files to process
   * @param basename basename
   * @param maxPrefixLength maximal prefix length
   * @throws IOException if an exception occurs while executing gt
   */
  private void run_uniquesub(File[] filesToProcess, String basename,
      int maxPrefixLength) throws IOException {

    File mupDir = new File(this.baseDir, MUP_DIR);
    File fmidxDir = new File(this.baseDir, FMIDX_DIR);

    if (!mupDir.isDirectory())
      if (!mupDir.mkdirs())
        throw new IOException("Can't create directory for mup directory: "
            + mupDir.getAbsolutePath());

    // Use parallel executions
    final ProcessUtils.ParalellExec pexec = new ProcessUtils.ParalellExec(1, 3);

    for (int i = 0; i < filesToProcess.length; i++) {
      File f = filesToProcess[i];

      final String cmd =
          Settings.getGenomeToolsPath()
              + " uniquesub -output querypos sequence -max " + maxPrefixLength
              + " -fmi " + fmidxDir.getAbsolutePath() + File.separator
              + basename + " -query " + f.getAbsolutePath();

      // Create the output file name
      final String outname = StringUtils.basename(f.getName()) + MUP_EXTENSION;

      // Execute the external process
      // ProcessUtils.execWriteOutput(cmd, new File(mupDir, outname));
      pexec.addTask(cmd, new File(mupDir, outname));
    }

    pexec.execTasks();
  }

  /**
   * Function that calculate uniqueness
   * @param sequenceStart sequence start position
   */
  private float uscoreCalculation(final int sequenceStart) {

    // Oligo end position calculation
    final int sequenceEnd = sequenceStart + this.oligoLength - 1;

    // Clear the mupEnd hashtable (optimization)
    this.mupEnd.clear();

    for (int sequencePosition = sequenceStart; sequencePosition <= sequenceEnd; sequencePosition++) {

      if (mupDict.containsKey(sequencePosition)) {

        // Store the end position of the mup
        final int temp_mup_end =
            sequencePosition + mupDict.get(sequencePosition) - 1;

        if (temp_mup_end <= sequenceEnd) {

          final int val =
              mupEnd.containsKey(temp_mup_end) ? mupEnd.get(temp_mup_end) : 0;

          mupEnd.put(temp_mup_end, val + 1);
        }

      }
    }

    return mupEnd.size();
  }

  /**
   * Parse a resuly file.
   * @param scaffold scaffold witch result file must be parsed
   * @throws IOException if an error occurs while reading result file
   */
  private void parseResultFile(final String scaffold) throws IOException {

    final File mupDir = new File(this.baseDir, MUP_DIR);
    final File file = new File(mupDir, scaffold + MUP_EXTENSION);

    // Load the minimum unique prefix file in a list
    mupDict.clear();

    BufferedReader br = FileUtils.createBufferedReader(file);
    String line = null;

    // final Pattern line_test = Pattern.compile("^\\d+\\s");
    final Pattern lineSplitPattern = Pattern.compile(" ");

    while ((line = br.readLine()) != null) {

      // final Matcher m = line_test.matcher(line);

      // if (!m.matches())
      if (!Character.isDigit(line.charAt(0)))
        continue;

      // Retrieve each column of the line
      final String[] line_attributes = lineSplitPattern.split(line);

      final int pos = Integer.parseInt(line_attributes[0]);
      final int len = Integer.parseInt(line_attributes[1]);

      mupDict.put(pos, len);
    }

    // Close mup file
    br.close();

  }

  //
  // Constructor
  //

  /**
   * Public constructor.
   */
  public UnicityMeasurement() {

    super(0, 1);
  }

  /**
   * Public constructor.
   * @param genomeFile Genome file to use
   * @param oligoLength the oligo length
   * @throws IOException if an error occurs while creating data
   */
  public UnicityMeasurement(final File genomeFile, final int oligoLength)
      throws IOException {

    this(genomeFile, oligoLength, 15);
  }

  /**
   * Public constructor.
   * @param genomeFile Genome file to use
   * @param oligoLength the oligo length
   * @param maxPrefixLength Maximal prefix length
   * @throws IOException if an error occurs while creating data
   */
  public UnicityMeasurement(final File genomeFile, final int oligoLength,
      final int maxPrefixLength) throws IOException {

    super(0, oligoLength);

    // Set the oligo length
    this.oligoLength = oligoLength;

    this.baseDir = genomeFile.getAbsoluteFile().getParentFile();

    // Create sequence files without X
    FastaExplode.fastaExplode(genomeFile, this.baseDir, "",
        SEQ_GZ_WITHOUT_X_EXTENSION, true, true);

    // Get the list of sequences files created
    final File[] files =
        FileUtils
            .listFilesByExtension(this.baseDir, SEQ_GZ_WITHOUT_X_EXTENSION);

    Arrays.sort(files);

    // Build fmindex
    build_fmindex(files);

    // Build unique sub
    run_uniquesub(files, StringUtils.removeNonAlphaAtEndOfString(FileUtils
        .getPrefix(files)), maxPrefixLength);
  }

}
