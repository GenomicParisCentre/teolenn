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
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import fr.ens.transcriptome.teolenn.measurement.ChromosomeMeasurement;
import fr.ens.transcriptome.teolenn.measurement.Measurement;
import fr.ens.transcriptome.teolenn.measurement.MeasurementRegistery;
import fr.ens.transcriptome.teolenn.measurement.OligoLengthMeasurement;
import fr.ens.transcriptome.teolenn.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.teolenn.measurement.filter.MeasurementFilter;
import fr.ens.transcriptome.teolenn.measurement.filter.MeasurementFilterRegistery;
import fr.ens.transcriptome.teolenn.output.DefaultOutput;
import fr.ens.transcriptome.teolenn.output.Output;
import fr.ens.transcriptome.teolenn.output.OutputRegistery;
import fr.ens.transcriptome.teolenn.selector.SequenceSelector;
import fr.ens.transcriptome.teolenn.selector.SequenceSelectorRegistery;
import fr.ens.transcriptome.teolenn.sequence.SequenceMeasurements;
import fr.ens.transcriptome.teolenn.sequence.filter.SequenceFilter;
import fr.ens.transcriptome.teolenn.sequence.filter.SequenceFilterRegistery;

/**
 * Tis class allow to read the design file.
 * @author Laurent Jourdren
 */
public class DesignReader {

  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  private DesignCommand design;
  private Properties constants;

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

    this.design = new DesignCommand();
    this.constants = new Properties();

    logger.info(Globals.APP_NAME
        + " version " + Globals.APP_VERSION + " (" + Globals.APP_BUILD_NUMBER
        + " " + Globals.APP_BUILD_DATE + ")");

    SAXReader saxReader = new SAXReader();
    Document document = saxReader.read(new FileReader(designFile));

    Element root = document.getRootElement();
    Element designElement = root;

    double designFileVersion = 0.0;
    for (Iterator i1 = designElement.elementIterator("formatversion"); i1
        .hasNext();)
      designFileVersion =
          Double.parseDouble(((Element) i1.next()).getTextTrim());

    if (designFileVersion != Globals.DESIGN_FILE_VERSION) {
      System.err.println("Invalid version of your "
          + Globals.APP_NAME + " design file.");
      System.exit(1);
    }

    // constants element
    this.constants = getElementConstants(designElement);

    for (Iterator i2 = designElement.elementIterator("startposition"); i2
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

    // oligoIntervallength element
    for (Iterator i4 = designElement.elementIterator("oligointervallength"); i4
        .hasNext();)
      this.design.setOligoIntervalLength(Integer.parseInt(getValue(i4)));
    setConstant("oligointervallength", ""
        + this.design.getOligoIntervalLength());

    // genomefile element
    if (genomeFile != null)
      this.design.setGenomeFile(genomeFile);
    else
      for (Iterator i5 = designElement.elementIterator("genomefile"); i5
          .hasNext();)
        this.design.setGenomeFile(new File(getValue(i5)));
    setConstant("genomefile", ""
        + this.design.getGenomeFile().getAbsolutePath());

    // genomemakedfile element
    if (genomeMaskedFile != null)
      this.design.setGenomeMaskedFile(genomeMaskedFile);
    else
      for (Iterator i6 = designElement.elementIterator("genomemaskedfile"); i6
          .hasNext();) {
        final String filename = getValue(i6);
        if (!"".equals(filename))
          this.design.setGenomeMaskedFile(new File(filename));
      }
    setConstant("genomemaskedfile", ""
        + this.design.getGenomeMaskedFile().getAbsolutePath());

    // outputdir element
    if (outputDir != null)
      this.design.setOutputDir(outputDir);
    else
      for (Iterator i7 = designElement.elementIterator("outputdir"); i7
          .hasNext();) {
        final String path = getValue(i7);
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

    // Set the outputs
    d.setOutputsList(parseOutput(designElement));

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
    list.add(new OligoLengthMeasurement());

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

          if (!sm.isMeasurement(name)) {
            logger.warning("Unknown measurement: " + name);
            continue;
          }

          for (Map.Entry<Object, Object> e2 : properties.entrySet()) {

            sm.getMeasurement(name).setProperty((String) e2.getKey(),
                (String) e2.getValue());

          }
        }
      }
    };

  }

  /**
   * Parse the "output" element of the DOM.
   * @param rootElement root element of the document
   * @return a selector objects
   * @throws TeolennException if an error occurs while parsing
   */
  private List<Output> parseOutput(final Element rootElement)
      throws TeolennException {

    List<Output> list = new ArrayList<Output>();

    for (Iterator i = rootElement.elementIterator("output"); i.hasNext();) {

      final Element selector = (Element) i.next();

      String selectorName = null;

      for (Iterator i1 = selector.elementIterator("name"); i1.hasNext();) {
        final Element name = (Element) i1.next();
        selectorName = name.getTextTrim();
      }

      // Add the selector to registery if it is a plug in
      for (Iterator i2 = selector.elementIterator("class"); i2.hasNext();) {
        final Element clazz = (Element) i2.next();
        String outputClass = clazz.getTextTrim();
        OutputRegistery.addOutputType(selectorName, outputClass);
      }

      // Get the parameters of the measurement
      final Properties properties = getElementParameters(selector);

      Output output = OutputRegistery.getOutput(selectorName);

      if (output == null) {
        logger.warning("Unknown output: " + selectorName);
        throw new TeolennException("Unknown output: " + selectorName);
      }

      // Set the initialization parameters for the selector
      for (Map.Entry<Object, Object> entry : properties.entrySet())
        output.setInitParameter((String) entry.getKey(), (String) entry
            .getValue());

      list.add(output);
    }

    if (list.size() == 0)
      list.add(new DefaultOutput());

    // Set the default initialization parameters of the outputs
    for (Output o : list)
      this.design.setDefaultModuleInitParameters(o);

    return list;
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
          pKey = name.getTextTrim().toLowerCase();
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
          pKey = name.getTextTrim().toLowerCase();
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
   * Get the read design.
   * @return a design object
   */
  public DesignCommand getDesign() {

    return this.design;
  }

}
