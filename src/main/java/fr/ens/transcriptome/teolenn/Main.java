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

import fr.ens.transcriptome.teolenn.measurement.ChromosomeMeasurement;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.MeasurementRegistery;
import fr.ens.transcriptome.teolenn.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.teolenn.measurement.filter.MeasurementFilter;
import fr.ens.transcriptome.teolenn.measurement.filter.MeasurementFilterRegistery;
import fr.ens.transcriptome.teolenn.selector.SequenceSelector;
import fr.ens.transcriptome.teolenn.selector.SequenceSelectorRegistery;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.sequence.filter.SequenceFilter;
import fr.ens.transcriptome.teolenn.sequence.filter.SequenceFilterRegistery;

/**
 * This is the main class of the application.
 * @author Laurent Jourdren
 */
public class Main {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  private DesignCommand design = new DesignCommand();
  private double designFileVersion;
  private Properties constants = new Properties();

  /**
   * Read the design file and run the design
   * @param designFile The design file
   * @param genomeFile The genome file
   * @param genomeMaskedFile The genome masked file
   * @param outputDir The output dir
   * @throws TeolennException if an error occurs while computing the design
   * @throws IOException if an error occurs while reading the design file
   * @throws DocumentException if an error occurs while parsing the design file
   */
  public void readDesign(final File designFile, final File genomeFile,
      final File genomeMaskedFile, File outputDir) throws TeolennException,
      IOException, DocumentException {

    logger.info(Globals.APP_NAME
        + " version " + Globals.APP_VERSION + " (" + Globals.APP_BUILD_NUMBER
        + " " + Globals.APP_BUILD_DATE + ")");

    SAXReader saxReader = new SAXReader();
    Document document = saxReader.read(new FileReader(designFile));

    Element root = document.getRootElement();
    Element designElement = root;

    for (Iterator i1 = designElement.elementIterator("formatversion"); i1
        .hasNext();)
      this.designFileVersion =
          Double.parseDouble(((Element) i1.next()).getTextTrim());

    if (this.designFileVersion != Globals.DESIGN_FILE_VERSION) {
      System.err.println("Invalid version of your "
          + Globals.APP_NAME + " design file.");
      System.exit(1);
    }

    // constants element
    this.constants = getElementConstants(designElement);

    for (Iterator i2 = designElement.elementIterator("startPosition"); i2
        .hasNext();) {

      final String sp = ((Element) i2.next()).getTextTrim();
      if ("1".equals(sp))
        this.design.setStart1(true);
      else
        this.design.setStart1(false);
    }
    setConstant("startPosition", "" + this.design.isStart1());

    // oligolength element
    for (Iterator i3 = designElement.elementIterator("oligolength"); i3
        .hasNext();)
      this.design.setOligoLength(Integer.parseInt(getValue(i3)));
    setConstant("oligolength", "" + this.design.getOligoLength());

    // genomefile element
    if (genomeFile != null)
      this.design.setGenomeFile(genomeFile);
    else
      for (Iterator i4 = designElement.elementIterator("genomefile"); i4
          .hasNext();)
        this.design.setGenomeFile(new File(getValue(i4)));
    setConstant("genomefile", ""
        + this.design.getGenomeFile().getAbsolutePath());

    // genomemakedfile element
    if (genomeMaskedFile != null)
      this.design.setGenomeMaskedFile(genomeMaskedFile);
    else
      for (Iterator i5 = designElement.elementIterator("genomemaskedfile"); i5
          .hasNext();) {
        final String filename = getValue(i5);
        if (!"".equals(filename))
          this.design.setGenomeMaskedFile(new File(filename));
      }
    setConstant("genomemaskedfile", ""
        + this.design.getGenomeMaskedFile().getAbsolutePath());

    // outputdir element
    if (outputDir != null)
      this.design.setOutputDir(outputDir);
    else
      for (Iterator i6 = designElement.elementIterator("outputdir"); i6
          .hasNext();) {
        final String path = getValue(i6);
        if (!"".equals(path))
          this.design.setOutputDir((new File(path)).getCanonicalFile());
      }
    setConstant("outputdir", "" + this.design.getOutputDir().getAbsolutePath());

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

    // Test the validity of the outptdir
    if (this.design.getOutputDir() == null
        || !this.design.getOutputDir().isDirectory())
      throw new InvalidParameterException("output directory is not found"
          + (this.design.getOutputDir() == null ? "." : ": "
              + this.design.getOutputDir()));

    isSkipElementEnable(designElement, "sequencefilters");

    final DesignCommand d = design;

    // Test if phases must be skipped
    d.setSkipSequenceCreation(isSkipElementEnable(designElement,
        "sequencecreation"));
    d.setSkipSequenceFilters(isSkipElementEnable(designElement,
        "sequencefilters"));
    d.setSkipMeasurementsComputation(isSkipElementEnable(designElement,
        "measurements"));
    d.setSkipMeasurementsFilters(isSkipElementEnable(designElement,
        "measurementfilters"));
    d.setSkipSelector(isSkipElementEnable(designElement, "selector"));

    // Set the sequenceFilters
    d.setSequenceFiltersList(parseSequenceFilters(designElement));

    // Set the measurements
    d.setMeasurementsList(parseMeasurements(designElement));

    // Set the measurement filters
    d.setMeasurementFiltersList(parseMeasurementFilters(designElement));

    // Set the selector
    d.setSelector(parseSelector(designElement));

    // Set the weights
    d.setWeightSetters(parseSelectWeights(designElement));

    // Start the computation
    d.execute();

  }

  /**
   * Execute the design.
   * @throws TeolennException if an error occurs while running the design
   */
  public void execute() throws TeolennException {

    this.design.execute();
  }

  /**
   * Set a constant.
   * @param constantName Name of the constant
   * @param constantValue Value of the constant
   */
  private void setConstant(final String constantName, final String constantValue) {

    this.constants.setProperty(constantName, constantValue);
  }

  private String getValue(final Iterator i) {

    return getValue((Element) i.next());
  }

  private String getValue(final Element e) {

    if (e == null)
      return "";

    return getValue(e.getTextTrim());
  }

  private String getValue(final String s) {

    if (s.startsWith("${") && s.endsWith("}")) {

      final String constantName = s.substring(2, s.length() - 1);
      if (this.constants.containsKey(constantName))
        return this.constants.getProperty(constantName);
    }

    return s;
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

    // Set the defaults initialization parameters of the sequence filters
    for (SequenceFilter sq : list)
      this.design.setDefaultModuleInitParameters(sq);

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

    list.add(new ChromosomeMeasurement());
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
        if (ChromosomeMeasurement.MEASUREMENT_NAME.toLowerCase().equals(
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

    // Set the default initialization parameters of the measurements
    for (Measurement m : list)
      this.design.setDefaultModuleInitParameters(m);

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

    // Set the defaults initialization parameters of the measurements filters
    for (MeasurementFilter mf : list)
      this.design.setDefaultModuleInitParameters(mf);

    return list;
  }

  /**
   * Parse the "selector" element of the DOM.
   * @param rootElement root element of the document
   * @return a selector objects
   * @throws TeolennException if an error occurs while parsing
   */
  private SequenceSelector parseSelector(final Element rootElement)
      throws TeolennException {

    for (Iterator i = rootElement.elementIterator("selector"); i.hasNext();) {

      final Element selector = (Element) i.next();

      String selectorName = null;

      for (Iterator i1 = selector.elementIterator("name"); i1.hasNext();) {
        final Element name = (Element) i1.next();
        selectorName = name.getTextTrim();
      }

      // Add the selector to registery if it is a plug in
      for (Iterator i2 = selector.elementIterator("class"); i2.hasNext();) {
        final Element clazz = (Element) i2.next();
        String selectorClass = clazz.getTextTrim();
        SequenceSelectorRegistery.addSequenceSelectorType(selectorName,
            selectorClass);
      }

      // Get the parameters of the measurement
      final Properties properties = getElementParameters(selector);

      SequenceSelector s =
          SequenceSelectorRegistery.getSequenceSelector(selectorName);

      if (s == null) {
        logger.warning("Unknown selector: " + selectorName);
        throw new TeolennException("Unknown selector: " + selectorName);
      }

      // Set the initialization parameters for the selector
      for (Map.Entry<Object, Object> entry : properties.entrySet())
        s.setInitParameter((String) entry.getKey(), (String) entry.getValue());

      // Set defaults parameters
      this.design.setDefaultModuleInitParameters(s);

      return s;
    }

    throw new TeolennException("No selector found.");
  }

  /**
   * Parse the "select" element of the DOM.
   * @param rootElement root element of the document
   * @return a list of weights objects
   * @throws IOException if an error occurs while parsing
   */
  private WeightsSetter parseSelectWeights(final Element rootElement)
      throws IOException {

    // Map of weights
    final Map<String, Float> selectWeights = new HashMap<String, Float>();

    // Map of properties
    final Map<String, Properties> selectProperties =
        new HashMap<String, Properties>();

    for (Iterator i = rootElement.elementIterator("selector"); i.hasNext();) {
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

    return new WeightsSetter() {

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
          pValue = getValue(value.getTextTrim());
        }

        if (pKey != null && pValue != null)
          result.setProperty(pKey, pValue);
      }

    }

    return result;
  }

  /**
   * Get the constants
   * @param element Element to parse
   * @return a Properties object with the name and values of the parameters
   */
  private final Properties getElementConstants(final Element element) {

    final Properties result = new Properties();

    for (Iterator i4 = element.elementIterator("constants"); i4.hasNext();) {
      final Element params = (Element) i4.next();

      for (Iterator i5 = params.elementIterator("constant"); i5.hasNext();) {
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

        }

        if (pKey != null || pValue != null)
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

      final String value = e.attributeValue("skip");
      if (value == null)
        return false;

      result = Boolean.parseBoolean(value.trim());
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

      if (line.hasOption("licence"))
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

      final Main main = new Main();

      // Read design
      main.readDesign(designFile, genomeFile, genomeMaskedFile, outputDir);

      // Execute design
      main.execute();

    } catch (Exception e) {
      System.err.println(e.getMessage());
      if (Globals.DEBUG)
        e.printStackTrace();
      System.exit(1);
    }

  }
}
