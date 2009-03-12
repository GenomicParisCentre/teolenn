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

package fr.ens.transcriptome.teolenn.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.Globals;
import fr.ens.transcriptome.teolenn.Settings;

/**
 * Utility class for launching external process.
 * @author Laurent Jourdren
 */
public final class ProcessUtils {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  /**
   * Execute a command with the OS.
   * @param cmd Command to execute
   * @param stdOutput don't show the result of the command on the standard
   *          output
   * @throws IOException if an error occurs while running the process
   */
  public static void exec(final String cmd, final boolean stdOutput)
      throws IOException {

    logger.fine("execute: " + cmd);

    final long startTime = System.currentTimeMillis();

    Process p = Runtime.getRuntime().exec(cmd);

    InputStream std = p.getInputStream();
    BufferedReader stdr = new BufferedReader(new InputStreamReader(std));

    String l = null;

    while ((l = stdr.readLine()) != null) {
      if (stdOutput)
        System.out.println(l);
    }

    InputStream err = p.getInputStream();
    BufferedReader errr = new BufferedReader(new InputStreamReader(err));

    String l2 = null;

    while ((l2 = errr.readLine()) != null)
      System.err.println(l2);

    stdr.close();
    errr.close();

    final long endTime = System.currentTimeMillis();

    logger.fine("Done in " + (endTime - startTime) + " ms.");
  }

  /**
   * Execute a command with the OS and save the output in file.
   * @param cmd Command to execute
   * @param outputFile The output file
   * @throws IOException if an error occurs while running the process
   */
  public static void execWriteOutput(String cmd, File outputFile)
      throws IOException {

    logger.fine("execute: " + cmd);

    final long startTime = System.currentTimeMillis();

    Process p = Runtime.getRuntime().exec(cmd);

    InputStream std = p.getInputStream();

    FileOutputStream fos = new FileOutputStream(outputFile);

    FileUtils.copy(std, fos);

    InputStream err = p.getInputStream();
    BufferedReader errr = new BufferedReader(new InputStreamReader(err));

    String l2 = null;

    while ((l2 = errr.readLine()) != null)
      System.err.println(l2);

    fos.close();
    errr.close();

    final long endTime = System.currentTimeMillis();

    logger.fine("Done in " + (endTime - startTime) + " ms.");
  }

  /**
   * Execute a command with the OS and return the output in a string.
   * @param cmd Command to execute
   * @return a string with the output the command
   * @throws IOException if an error occurs while running the process
   */
  public static String execToString(String cmd) throws IOException {

    logger.fine("execute: " + cmd);

    final long startTime = System.currentTimeMillis();

    Process p = Runtime.getRuntime().exec(cmd);

    InputStream std = p.getInputStream();

    BufferedReader stdr = new BufferedReader(new InputStreamReader(std));

    StringBuffer sb = new StringBuffer();
    String l1 = null;

    while ((l1 = stdr.readLine()) != null) {
      sb.append(l1);
      sb.append('\n');
    }

    InputStream err = p.getInputStream();
    BufferedReader errr = new BufferedReader(new InputStreamReader(err));

    String l2 = null;

    while ((l2 = errr.readLine()) != null)
      System.err.println(l2);

    stdr.close();
    errr.close();

    try {

      final int exitValue = p.waitFor();
      final long endTime = System.currentTimeMillis();

      if (exitValue == 126)
        throw new IOException("Command invoked cannot execute");

      if (exitValue == 127)
        throw new IOException("Command not found");

      logger.fine("Done in " + (endTime - startTime) + " ms.");
    } catch (InterruptedException e) {

      logger.severe("Interrupted exception: " + e.getMessage());
    }

    return sb.toString();
  }

  public static class ParalellExec extends SelfLoopHandler {

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

          logger.fine("PExec: (" + Thread.currentThread() + ") " + cmd);

          if (outputFile == null)
            exec(cmd, Settings.isStandardOutputForExecutable());
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
