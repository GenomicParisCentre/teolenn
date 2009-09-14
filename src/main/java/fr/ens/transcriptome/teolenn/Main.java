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
 * or to join the Teolenn Google group, visit the home page
 * at:
 *
 *      http://www.transcriptome.ens.fr/teolenn
 *
 */

package fr.ens.transcriptome.teolenn;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dom4j.DocumentException;

/**
 * This is the main class of the application.
 * @author Laurent Jourdren
 */
public class Main {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  /**
   * Show version of the application.
   */
  private static void version() {

    System.out.println(Globals.APP_NAME
        + " version " + Globals.APP_VERSION + " (" + Globals.APP_BUILD_NUMBER
        + " on " + Globals.APP_BUILD_DATE + ")");
    System.exit(0);
  }

  /**
   * Show licence information about this application.
   */
  private static void about() {

    System.out.println(Globals.ABOUT_TXT);
    System.exit(0);
  }

  /**
   * Show information about this application.
   */
  private static void license() {

    System.out.println(Globals.LICENSE_TXT);
    System.exit(0);
  }

  /**
   * Show command line help.
   * @param options Options of the software
   */
  private static void help(final Options options) {

    // Show help message
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(Globals.APP_NAME_LOWER_CASE
        + " [options] design [genome [genome_masked [output_dir]]]", options);

    System.exit(0);
  }

  /**
   * Create options for command line
   * @return an Options object
   */
  private static Options makeOptions() {

    // create Options object
    final Options options = new Options();

    // add t option
    options.addOption("version", false, "show version of the software");
    options
        .addOption("about", false, "display information about this software");
    options.addOption("h", "help", false, "display this help");
    options.addOption("license", false,
        "display information about the license of this software");
    options.addOption("v", "verbose", false, "display external tools output");
    options.addOption("silent", false, "don't show log on console");

    options.addOption(OptionBuilder.withArgName("number").hasArg()
        .withDescription("number of threads to use").create("threads"));

    options.addOption(OptionBuilder.withArgName("file").hasArg()
        .withDescription("configuration file to use").create("conf"));

    options.addOption(OptionBuilder.withArgName("file").hasArg()
        .withDescription("external log file").create("log"));

    options.addOption(OptionBuilder.withArgName("level").hasArg()
        .withDescription("log level").create("loglevel"));

    return options;
  }

  /**
   * Parse the options of the command line
   * @param args command line arguments
   * @return the number of optional arguments
   */
  private static int parseCommandLine(final String args[]) {

    final Options options = makeOptions();
    final CommandLineParser parser = new GnuParser();

    int argsOptions = 0;

    try {

      // parse the command line arguments
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("help"))
        help(options);

      if (line.hasOption("about"))
        about();

      if (line.hasOption("version"))
        version();

      if (line.hasOption("license"))
        license();

      // Load configuration if exists
      try {
        if (line.hasOption("conf")) {
          Settings.loadSettings(new File(line.getOptionValue("conf")));
          argsOptions += 2;
        } else
          Settings.loadSettings();
      } catch (IOException e) {
        logger.severe("Error while reading configuration file.");
        System.exit(1);
      }

      // Set the number of threads
      if (line.hasOption("threads"))
        try {
          argsOptions += 2;
          Settings.setMaxthreads(Integer.parseInt(line
              .getOptionValue("threads")));
        } catch (NumberFormatException e) {
          logger.warning("Invalid threads number");
        }

      // Set the verbose mode for extenal tools
      if (line.hasOption("verbose")) {
        Settings.setStandardOutputForExecutable(true);
        argsOptions++;
      }

      // Set Log file
      if (line.hasOption("log")) {

        argsOptions += 2;
        try {
          Handler fh = new FileHandler(line.getOptionValue("log"));
          fh.setFormatter(Globals.LOG_FORMATTER);
          logger.setUseParentHandlers(false);

          logger.addHandler(fh);
        } catch (IOException e) {
          logger.severe("Error while creating log file: " + e.getMessage());
          System.exit(1);
        }
      }

      // Set the silent option
      if (line.hasOption("silent"))
        logger.setUseParentHandlers(false);

      // Set log level
      if (line.hasOption("loglevel")) {

        argsOptions += 2;
        try {
          logger.setLevel(Level.parse(line.getOptionValue("loglevel")
              .toUpperCase()));
        } catch (IllegalArgumentException e) {

          logger
              .warning("Unknown log level ("
                  + line.getOptionValue("loglevel")
                  + "). Accepted values are [SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST].");

        }
      }

    } catch (ParseException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (SecurityException e) {
      logger.severe(e.getMessage());
      System.exit(1);
    }

    // If there is no arguments after the option, show help
    if (argsOptions == args.length) {
      System.err.println("No inputs files.");
      System.err.println("type: "
          + Globals.APP_NAME_LOWER_CASE + " -h for more informations.");
      System.exit(1);
    }

    return argsOptions;
  }

  //
  // Main method
  //

  /**
   * Main method.
   * @param args command line arguments
   */
  public static void main(final String[] args) throws IOException,
      DocumentException {

    // Set log level
    logger.setLevel(Globals.LOG_LEVEL);
    logger.getParent().getHandlers()[0].setFormatter(Globals.LOG_FORMATTER);

    // Parse the command line
    final int argsOptions = parseCommandLine(args);

    final File designFile = new File(args[argsOptions + 0]);
    final File genomeFile =
        args.length > argsOptions + 1 ? new File(args[argsOptions + 1]) : null;
    final File genomeMaskedFile =
        args.length > argsOptions + 2 ? new File(args[argsOptions + 2]) : null;
    final File outputDir =
        args.length > argsOptions + 3 ? new File(args[argsOptions + 3]) : null;

    try {

      final DesignReader designReader = new DesignReader();

      // Read design
      designReader.readDesign(designFile, genomeFile, genomeMaskedFile,
          outputDir);

      // Execute design
      designReader.getDesign().execute();

    } catch (Exception e) {
      System.err.println(e.getMessage());
      if (Globals.DEBUG)
        e.printStackTrace();
      System.exit(1);
    }

  }
}
