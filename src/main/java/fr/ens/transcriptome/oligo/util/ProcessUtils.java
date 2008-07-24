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

package fr.ens.transcriptome.oligo.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Utility class for launching external process.
 * @author Laurent Jourdren
 */
public final class ProcessUtils {

  /**
   * Execute a command with the OS.
   * @param cmd Command to execute
   * @throws IOException if an error occurs while running the process
   */
  public static void exec(final String cmd) throws IOException {

    System.out.println("cmd: " + cmd);

    final long startTime = System.currentTimeMillis();

    Process p = Runtime.getRuntime().exec(cmd);

    InputStream std = p.getInputStream();
    BufferedReader stdr = new BufferedReader(new InputStreamReader(std));

    String l = null;

    while ((l = stdr.readLine()) != null) {
      System.out.println(l);
    }

    InputStream err = p.getInputStream();
    BufferedReader errr = new BufferedReader(new InputStreamReader(err));

    String l2 = null;

    while ((l2 = errr.readLine()) != null)
      System.err.println(l2);

    // System.out.println(cmd);

    final long endTime = System.currentTimeMillis();

    System.out.println("Done in " + (endTime - startTime) + " ms.");
  }

  /**
   * Execute a command with the OS and save the output in file.
   * @param cmd Command to execute
   * @param outputFile The output file
   * @throws IOException if an error occurs while running the process
   */
  public static void execWriteOutput(String cmd, File outputFile)
      throws IOException {

    System.out.println("cmd: " + cmd);

    final long startTime = System.currentTimeMillis();

    Process p = Runtime.getRuntime().exec(cmd);

    InputStream std = p.getInputStream();

    FileOutputStream fos = new FileOutputStream(outputFile);

    FileUtils.copy(std, fos);

    fos.close();

    InputStream err = p.getInputStream();
    BufferedReader errr = new BufferedReader(new InputStreamReader(err));

    String l2 = null;

    while ((l2 = errr.readLine()) != null)
      System.err.println(l2);

    final long endTime = System.currentTimeMillis();

    System.out.println("Done in " + (endTime - startTime) + " ms.");
  }

  public static class ParalellExec extends GuidedLoopHandler {

    private final List<String> cmds = new ArrayList<String>();
    private final List<File> outputFiles = new ArrayList<File>();

    /**
     * Add a task to execute.
     * @param cmd Command to execute
     */
    public void addTask(final String cmd) {

      addTask(cmd, null);
    }

    /**
     * Add a task to execute.
     * @param cmd Command to execute
     * @param outputFile output file for the standard output of the process
     */
    public void addTask(final String cmd, final File outputFile) {

      if (cmd == null)
        return;

      this.cmds.add(cmd);
      this.outputFiles.add(outputFile);
    }

    /**
     * Launch all the tasks.
     */
    public void execTasks() {

      this.setRange(0, this.cmds.size());
      loopProcess();
    }

    @Override
    public void loopDoRange(int start, int end) {

      for (int i = start; i < end; i++) {

        String cmd = this.cmds.get(i);
        File outputFile = this.outputFiles.get(i);

        try {

          System.out.println("PExec: (" + Thread.currentThread() + ") " + cmd);

          if (outputFile == null)
            exec(cmd);
          else
            execWriteOutput(cmd, outputFile);

        } catch (IOException e) {

          throw new RuntimeException(e.getMessage());
        }

      }

    }

    //
    // Constructor
    //

    /**
     * Public constructor.
     * @param start index of start
     * @param end index of end
     * @param min minimal of task by process
     * @param threads number of threads
     */
    public ParalellExec(final int start, final int end, final int min,
        final int threads) {
      super(start, end, min, threads);
    }

    /**
     * Public constructor.
     * @param min minimal of task by process
     * @param threads number of threads
     */
    public ParalellExec(final int min, final int threads) {
      this(0, 0, min, threads);
    }

  }

  //
  // Constructor
  //

  private ProcessUtils() {
  }

}
