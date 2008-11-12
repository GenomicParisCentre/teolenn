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

package fr.ens.transcriptome.teolenn;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import fr.ens.transcriptome.teolenn.filter.SequenceFilter;
import fr.ens.transcriptome.teolenn.filter.SequenceFilterRegistery;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.MeasurementRegistery;
import fr.ens.transcriptome.teolenn.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.teolenn.measurement.ScaffoldMeasurement;
import fr.ens.transcriptome.teolenn.measurement.filter.MeasurementFilter;
import fr.ens.transcriptome.teolenn.measurement.filter.MeasurementFilterRegistery;

/**
 * This is the main class of the application.
 * @author Laurent Jourdren
 */
public class Main {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  private Design design = new Design();

  /**
   * Read the design file and run the design
   * @param designFile The design file
   * @param genomeFile The genome file
   * @param genomeMaskedFile The genome masked file
   * @param outputDir The output dir
   * @throws IOException if an error occurs while reading the design file
   * @throws DocumentException if an error occurs while parsing the design file
   */
  public void readDesign(final File designFile, final File genomeFile,
      final File genomeMaskedFile, File outputDir) throws IOException,
      DocumentException {

    SAXReader saxReader = new SAXReader();
    Document document = saxReader.read(new FileReader(designFile));

    Element root = document.getRootElement();
    Element designElement = root;
    // root element
    // for (Iterator i = root.elementIterator("design"); i.hasNext();) {
    // final Element designElement = (Element) i.next();

    // windowlength element
    for (Iterator i2 = designElement.elementIterator("windowlength"); i2
        .hasNext();)
      this.design.setWindowLength(Integer.parseInt(((Element) i2.next())
          .getTextTrim()));

    // oligolength element
    for (Iterator i3 = designElement.elementIterator("oligolength"); i3
        .hasNext();)
      this.design.setOligoLength(Integer.parseInt(((Element) i3.next())
          .getTextTrim()));

    // windowstep element
    int windowstep = -1;
    for (Iterator i4 = designElement.elementIterator("windowstep"); i4
        .hasNext();)
      windowstep = Integer.parseInt(((Element) i4.next()).getTextTrim());
    this.design.setWindowStep(windowstep == -1
        ? this.design.getWindowLength() : windowstep);

    // genomefile element
    if (genomeFile != null)
      this.design.setGenomeFile(genomeFile);
    else
      for (Iterator i5 = designElement.elementIterator("genomefile"); i5
          .hasNext();)
        this.design
            .setGenomeFile(new File(((Element) i5.next()).getTextTrim()));

    // genomemakedfile element
    if (genomeMaskedFile != null)
      this.design.setGenomeMaskedFile(genomeMaskedFile);
    else
      for (Iterator i6 = designElement.elementIterator("genomemaskedfile"); i6
          .hasNext();) {
        final String filename = ((Element) i6.next()).getTextTrim();
        if (!"".equals(filename))
          this.design.setGenomeMaskedFile(new File(filename));
      }

    // outputdir element
    if (outputDir != null)
      this.design.setOutputDir(outputDir);
    else
      for (Iterator i7 = designElement.elementIterator("outputdir"); i7
          .hasNext();)
        this.design.setOutputDir(new File(((Element) i7.next()).getTextTrim()));

    if (this.design.getGenomeFile() == null
        || !this.design.getGenomeFile().isFile())
      throw new InvalidParameterException("genome file is not found"
          + (this.design.getGenomeFile() == null ? "." : ": "
              + this.design.getGenomeFile()));

    if (this.design.getGenomeMaskedFile() != null
        && !this.design.getGenomeMaskedFile().isFile())
      throw new InvalidParameterException("genome masked file is not found"
          + (this.design.getGenomeMaskedFile() == null ? "." : ": "
              + this.design.getGenomeMaskedFile()));

    // Test the validity of the
    if (this.design.getOutputDir() == null
        || !this.design.getOutputDir().isDirectory())
      throw new InvalidParameterException("output directory is not found"
          + (this.design.getOutputDir() == null ? "." : ": "
              + this.design.getOutputDir()));

    isSkipElementEnable(designElement, "sequencefilters");

    final Design d = design;

    // Test if phases must be skipped
    d.setSkipSequenceFilters(isSkipElementEnable(designElement,
        "sequencefilters"));
    d.setSkipMeasurementsComputation(isSkipElementEnable(designElement,
        "measurements"));
    d.setSkipMeasurementsFilters(isSkipElementEnable(designElement,
        "measurementfilters"));

    // Start the computation
    d.phase0();

    if (!d.isSkipPhase1())
      d.phase1CreateAllOligos();

    if (!d.isSkipPhase2())
      d.phase2FilterAllOligos(parseSequenceFilters(designElement));

    // Don't skip this step, Add ons measurement need to be registered
    d.phase3CalcMeasurements(parseMeasurements(designElement));

    if (!d.isSkipPhase4())
      d.phase4FilterMeasurements(parseMeasurementFilters(designElement), true);

    d.phase5Select(parseSelectWeights(designElement));
  }

  /**
   * Parse the "sequencefilters" element of the DOM.
   * @param rootElement root element of the document
   * @return a list of SequenceFilter objects
   * @throws IOException if an error occurs while parsing
   */
  private List<SequenceFilter> parseSequenceFilters(final Element rootElement)
      throws IOException {

    List<SequenceFilter> list = new ArrayList<SequenceFilter>();

    for (Iterator i = rootElement.elementIterator("sequencefilters"); i
        .hasNext();) {
      final Element filters = (Element) i.next();

      for (Iterator i2 = filters.elementIterator("sequencefilter"); i2
          .hasNext();) {
        final Element filter = (Element) i2.next();

        String filterName = null;

        for (Iterator i3 = filter.elementIterator("name"); i3.hasNext();) {
          final Element name = (Element) i3.next();
          filterName = name.getTextTrim();
        }

        if (filterName == null) {
          logger.warning("Filter without name.");
          continue;
        }

        // Add the sequence filter to the registery if it is a plug in
        for (Iterator i4 = filter.elementIterator("class"); i4.hasNext();) {
          final Element clazz = (Element) i4.next();
          String filterClass = clazz.getTextTrim();
          SequenceFilterRegistery
              .addSequenceFilterType(filterName, filterClass);
        }

        // Get the parameters of the sequenceFilter
        final Properties properties = getElementParameters(filter);
        final SequenceFilter f =
            SequenceFilterRegistery.getSequenceFilter(filterName);

        if (f == null)
          logger.warning("Unknown sequence filter: " + filterName);
        else {

          for (Map.Entry<Object, Object> entry : properties.entrySet())
            // Set the initialization parameters for the sequence filter
            f.setInitParameter((String) entry.getKey(), (String) entry
                .getValue());

          list.add(f);
        }
      }

    }

    final File genomeFile = this.design.getGenomeFile();
    final File outputDir = this.design.getOutputDir();
    final int windowSize = this.design.getWindowLength();
    final int oligoSize = this.design.getOligoLength();

    // Set the initialization parameter of the sequence filters
    for (SequenceFilter sq : list) {
      sq.setInitParameter("_genomefile", genomeFile.getAbsolutePath());
      sq.setInitParameter("_genomemaskedfile", genomeFile.getAbsolutePath());
      sq.setInitParameter("_outputdir", outputDir.getAbsolutePath());
      sq.setInitParameter("_windowsize", Integer.toString(windowSize));
      sq.setInitParameter("_oligolength", Integer.toString(oligoSize));
      sq.setInitParameter("_extensionfilter", Design.OLIGO_SUFFIX);
    }

    return list;
  }

  /**
   * Parse the "measurements" element of the DOM.
   * @param rootElement root element of the document
   * @return a list of Measurement objects
   * @throws IOException if an error occurs while parsing
   */
  private List<Measurement> parseMeasurements(final Element rootElement)
      throws IOException {

    final List<Measurement> list = new ArrayList<Measurement>();

    list.add(new ScaffoldMeasurement());
    list.add(new OligoStartMeasurement());

    for (Iterator i = rootElement.elementIterator("measurements"); i.hasNext();) {
      final Element measurements = (Element) i.next();

      for (Iterator i2 = measurements.elementIterator("measurement"); i2
          .hasNext();) {
        final Element measurement = (Element) i2.next();

        String measurementName = null;

        for (Iterator i3 = measurement.elementIterator("name"); i3.hasNext();) {
          final Element name = (Element) i3.next();
          measurementName = name.getTextTrim();
        }

        if (measurementName == null) {
          logger.warning("Measurement without name.");
          continue;
        }

        // Skip if user attempt to add another Scaffold measurement
        if (ScaffoldMeasurement.MEASUREMENT_NAME.toLowerCase().equals(
            measurementName.toLowerCase()))
          continue;

        // Skip if user attempt to add another oligo start measurement
        if (OligoStartMeasurement.MEASUREMENT_NAME.toLowerCase().equals(
            measurementName.toLowerCase()))
          continue;

        // Add the measurement to registery if it is a plug in
        for (Iterator i4 = measurement.elementIterator("class"); i4.hasNext();) {
          final Element clazz = (Element) i4.next();
          String measurementClass = clazz.getTextTrim();
          MeasurementRegistery.addMeasurementType(measurementName,
              measurementClass);
        }

        // Get the parameters of the measurement
        final Properties properties = getElementParameters(measurement);

        final Measurement m =
            MeasurementRegistery.getMeasurement(measurementName);
        if (m == null)
          logger.warning("Unknown measurement: " + measurementName);
        else {

          // Set the initialization parameters for the measurement
          for (Map.Entry<Object, Object> entry : properties.entrySet())
            m.setInitParameter((String) entry.getKey(), (String) entry
                .getValue());

          list.add(m);
        }
      }

    }

    final File genomeFile = this.design.getGenomeFile();
    final File outputDir = this.design.getOutputDir();
    final int windowSize = this.design.getWindowLength();
    final int oligoSize = this.design.getOligoLength();

    // Set the initialization parameters of the measurements
    for (Measurement m : list) {
      m.setInitParameter("_genomefile", genomeFile.getAbsolutePath());
      m.setInitParameter("_genomemaskedfile", genomeFile.getAbsolutePath());
      m.setInitParameter("_outputdir", outputDir.getAbsolutePath());
      m.setInitParameter("_windowsize", Integer.toString(windowSize));
      m.setInitParameter("_oligolength", Integer.toString(oligoSize));
    }

    return list;
  }

  /**
   * Parse the "measurementfilters" element of the DOM.
   * @param rootElement root element of the document
   * @return a list of MeasurementFilter objects
   * @throws IOException if an error occurs while parsing
   */
  private List<MeasurementFilter> parseMeasurementFilters(
      final Element rootElement) throws IOException {

    final List<MeasurementFilter> list = new ArrayList<MeasurementFilter>();

    for (Iterator i = rootElement.elementIterator("measurementfilters"); i
        .hasNext();) {
      final Element filters = (Element) i.next();

      for (Iterator i2 = filters.elementIterator("measurementfilter"); i2
          .hasNext();) {
        final Element filter = (Element) i2.next();

        String measurementFilterName = null;

        for (Iterator i3 = filter.elementIterator("name"); i3.hasNext();) {
          final Element name = (Element) i3.next();
          measurementFilterName = name.getTextTrim();
        }

        if (measurementFilterName == null) {
          logger.warning("Measurement filter without name.");
          continue;
        }

        // Add the measurement to registery if it is a plug in
        for (Iterator i4 = filter.elementIterator("class"); i4.hasNext();) {
          final Element clazz = (Element) i4.next();
          String measurementClass = clazz.getTextTrim();
          MeasurementFilterRegistery.addMeasurementFilterType(
              measurementFilterName, measurementClass);
        }

        // Get the parameters of the measurement filters
        final Properties properties = getElementParameters(filter);

        final MeasurementFilter mf =
            MeasurementFilterRegistery
                .getMeasuremrentFilter(measurementFilterName);
        if (mf == null)
          logger.warning("Unknown measurement: " + measurementFilterName);
        else {

          // Set the initialization parameters for the measurement filters
          for (Map.Entry<Object, Object> entry : properties.entrySet())
            mf.setInitParameter((String) entry.getKey(), (String) entry
                .getValue());

          list.add(mf);
        }
      }

    }

    final File genomeFile = this.design.getGenomeFile();
    final File outputDir = this.design.getOutputDir();
    final int windowSize = this.design.getWindowLength();
    final int oligoSize = this.design.getOligoLength();

    // Set the initialization parameters of the measurements filters
    for (MeasurementFilter mf : list) {
      mf.setInitParameter("_genomefile", genomeFile.getAbsolutePath());
      mf.setInitParameter("_genomemaskedfile", genomeFile.getAbsolutePath());
      mf.setInitParameter("_outputdir", outputDir.getAbsolutePath());
      mf.setInitParameter("_windowsize", Integer.toString(windowSize));
      mf.setInitParameter("_oligolength", Integer.toString(oligoSize));
    }

    return list;
  }

  /**
   * Parse the "select" element of the DOM.
   * @param rootElement root element of the document
   * @return a list of weights objects
   * @throws IOException if an error occurs while parsing
   */
  private Select.WeightsSetter parseSelectWeights(final Element rootElement)
      throws IOException {

    // Map of weights
    final Map<String, Float> selectWeights = new HashMap<String, Float>();

    // Map of properties
    final Map<String, Properties> selectProperties =
        new HashMap<String, Properties>();

    for (Iterator i = rootElement.elementIterator("select"); i.hasNext();) {
      final Element select = (Element) i.next();

      for (Iterator i2 = select.elementIterator("measurement"); i2.hasNext();) {
        final Element measurement = (Element) i2.next();

        String measurementName = null;
        String measurementWeight = null;

        for (Iterator i3 = measurement.elementIterator("name"); i3.hasNext();) {
          final Element name = (Element) i3.next();
          measurementName = name.getTextTrim();
        }

        if (measurementName == null) {
          logger.warning("Measurement without name.");
          continue;
        }

        // Get the weight for the measurement
        for (Iterator i4 = measurement.elementIterator("weight"); i4.hasNext();) {
          final Element weight = (Element) i4.next();
          measurementWeight = weight.getTextTrim();
        }

        try {
          selectWeights.put(measurementName, Float
              .parseFloat(measurementWeight));
        } catch (NumberFormatException e) {
          logger.warning("Invalid "
              + measurementName + " weight: " + measurementWeight);
        }

        // Get the properties of the measurement
        selectProperties
            .put(measurementName, getElementParameters(measurement));
      }
    }

    return new Select.WeightsSetter() {

      @Override
      public void setWeights(SequenceMeasurements sm) {

        // Set the weights
        for (Map.Entry<String, Float> e : selectWeights.entrySet())
          sm.setWeight(sm.getMeasurement(e.getKey()), e.getValue());

        // Set the properties
        for (Map.Entry<String, Properties> e : selectProperties.entrySet()) {

          final String name = e.getKey();
          final Properties properties = e.getValue();

          for (Map.Entry<Object, Object> e2 : properties.entrySet()) {
            sm.getMeasurement(name).setProperty((String) e2.getKey(),
                (String) e2.getValue());

          }
        }
      }
    };

  }

  /**
   * Get the parameter of an element
   * @param element Element to parse
   * @return a Properties object with the name and values of the parameters
   */
  private final Properties getElementParameters(final Element element) {

    final Properties result = new Properties();

    for (Iterator i4 = element.elementIterator("parameters"); i4.hasNext();) {
      final Element params = (Element) i4.next();

      for (Iterator i5 = params.elementIterator("parameter"); i5.hasNext();) {
        final Element param = (Element) i5.next();

        String pKey = null;
        String pValue = null;

        for (Iterator i6 = param.elementIterator("name"); i6.hasNext();) {
          final Element name = (Element) i6.next();
          pKey = name.getTextTrim();
        }

        for (Iterator i7 = param.elementIterator("value"); i7.hasNext();) {
          final Element value = (Element) i7.next();
          pValue = value.getTextTrim();

          if ("${windowsize}".equals(pValue))
            pValue = Integer.toString(this.design.getWindowLength());
          else if ("${genomefile}".equals(pValue))
            pValue = this.design.getGenomeFile().getAbsolutePath();
          else if ("${oligolength}".equals(pValue))
            pValue = Integer.toString(this.design.getOligoLength());

        }

        result.setProperty(pKey, pValue);
      }

    }

    return result;
  }

  /**
   * Test if the phase of the element must be skipped.
   * @param rootElement DOM root element
   * @param elementName Name of the tag to test
   * @return true if the phase of the element must be skipped
   */
  private final boolean isSkipElementEnable(final Element rootElement,
      final String elementName) {

    boolean result = false;

    for (Iterator i = rootElement.elementIterator(elementName); i.hasNext();) {
      final Element e = (Element) i.next();

      result = Boolean.parseBoolean(e.attributeValue("skip").trim());
    }

    return result;
  }

  /**
   * Show version of the application.
   */
  private static void version() {

    System.out.println(Globals.APP_NAME
        + " version " + Globals.APP_VERSION + " (" + Globals.APP_BUILD_NUMBER
        + ")");
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
  private static void licence() {

    System.out.println(Globals.LICENCE_TXT);
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
    options.addOption("licence", false,
        "display information about the licence of this software");
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

      if (line.hasOption("licence"))
        licence();

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

    Main cli = new Main();

    try {
      cli.readDesign(designFile, genomeFile, genomeMaskedFile, outputDir);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }

  }
}
