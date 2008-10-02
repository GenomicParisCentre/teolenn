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

import fr.ens.transcriptome.oligo.filter.SequenceFilter;
import fr.ens.transcriptome.oligo.filter.SequenceFilterRegistery;
import fr.ens.transcriptome.oligo.measurement.Measurement;
import fr.ens.transcriptome.oligo.measurement.MeasurementRegistery;
import fr.ens.transcriptome.oligo.measurement.OligoStartMeasurement;
import fr.ens.transcriptome.oligo.measurement.ScaffoldMeasurement;
import fr.ens.transcriptome.oligo.measurement.filter.MeasurementFilter;
import fr.ens.transcriptome.oligo.measurement.filter.MeasurementFilterRegistery;

public class CLI {

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

    // windowsize element
    for (Iterator i2 = designElement.elementIterator("windowSize"); i2
        .hasNext();)
      this.design.setWindowSize(Integer.parseInt(((Element) i2.next())
          .getText()));

    // oligosize element
    for (Iterator i3 = designElement.elementIterator("oligosize"); i3.hasNext();)
      this.design.setOligoLength(Integer.parseInt(((Element) i3.next())
          .getText()));

    if (genomeFile != null)
      this.design.setGenomeFile(genomeFile);
    else
      for (Iterator i4 = designElement.elementIterator("genomefile"); i4
          .hasNext();)
        this.design.setGenomeFile(new File(((Element) i4.next()).getText()));

    if (genomeMaskedFile != null)
      this.design.setGenomeMaskedFile(genomeMaskedFile);
    else
      for (Iterator i5 = designElement.elementIterator("genomemaskedfile"); i5
          .hasNext();)
        this.design.setGenomeMaskedFile(new File(((Element) i5.next())
            .getText()));

    if (outputDir != null)
      this.design.setOutputDir(outputDir);
    else
      for (Iterator i6 = designElement.elementIterator("outputdir"); i6
          .hasNext();)
        this.design.setOutputDir(new File(((Element) i6.next()).getText()));

    if (this.design.getGenomeFile() == null
        || !this.design.getGenomeFile().isFile())
      throw new InvalidParameterException("genome file is not found"
          + (this.design.getGenomeFile() == null ? "." : ": "
              + this.design.getGenomeFile()));

    if (this.design.getGenomeMaskedFile() == null
        || !this.design.getGenomeMaskedFile().isFile())
      throw new InvalidParameterException("genome masked file is not found"
          + (this.design.getGenomeMaskedFile() == null ? "." : ": "
              + this.design.getGenomeMaskedFile()));

    // Test the validity of the
    if (this.design.getOutputDir() == null
        || !this.design.getOutputDir().isDirectory())
      throw new InvalidParameterException("output directory is not found"
          + (this.design.getOutputDir() == null ? "." : ": "
              + this.design.getOutputDir()));

    design.phase1CreateAllOligos();
    design.phase2FilterAllOligos(verifySequenceFilters(designElement));
    design.phase3CalcMeasurements(verifyMeasurements(designElement));
    design.phase4FilterMeasurements(verifyMeasurementFilters(designElement),
        true);
    design.phase5Select(verifySelect(designElement));
  }

  private List<SequenceFilter> verifySequenceFilters(final Element rootElement)
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
          filterName = name.getText().trim();
        }

        if (filterName == null) {
          logger.warning("Filter without name.");
          continue;
        }

        // Add the sequence filter to the registery if it is a plug in
        for (Iterator i4 = filter.elementIterator("class"); i4.hasNext();) {
          final Element clazz = (Element) i4.next();
          String filterClass = clazz.getText().trim();
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
          for (Object key : properties.keySet())
            // Set the initialization parameters for the sequence filter
            f.setInitParameter((String) key, (String) properties.get(key));

          list.add(f);
        }
      }

    }

    final File genomeFile = this.design.getGenomeFile();
    final File outputDir = this.design.getOutputDir();
    final int windowSize = this.design.getWindowSize();
    final int oligoSize = this.design.getOligoLength();

    // Initialize the sequence filters
    for (SequenceFilter sq : list) {
      sq.setInitParameter("_genomefile", genomeFile.getAbsolutePath());
      sq.setInitParameter("_genomemaskedfile", genomeFile.getAbsolutePath());
      sq.setInitParameter("_outputdir", outputDir.getAbsolutePath());
      sq.setInitParameter("_windowsize", Integer.toString(windowSize));
      sq.setInitParameter("_oligolength", Integer.toString(oligoSize));

      sq.setInitParameter("_extensionfilter", Design.OLIGO_SUFFIX);
      sq.init();
    }

    return list;
  }

  private List<Measurement> verifyMeasurements(final Element rootElement)
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
          measurementName = name.getText().trim();
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
          String measurementClass = clazz.getText().trim();
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
          for (Object key : properties.keySet())
            m.setInitParameter((String) key, (String) properties.get(key));

          list.add(m);
        }
      }

    }

    final File genomeFile = this.design.getGenomeFile();
    final File outputDir = this.design.getOutputDir();
    final int windowSize = this.design.getWindowSize();
    final int oligoSize = this.design.getOligoLength();

    // Initialize the measurements
    for (Measurement m : list) {
      m.setInitParameter("_genomefile", genomeFile.getAbsolutePath());
      m.setInitParameter("_genomemaskedfile", genomeFile.getAbsolutePath());
      m.setInitParameter("_outputdir", outputDir.getAbsolutePath());
      m.setInitParameter("_windowsize", Integer.toString(windowSize));
      m.setInitParameter("_oligolength", Integer.toString(oligoSize));
      m.init();
    }

    return list;
  }

  private List<MeasurementFilter> verifyMeasurementFilters(
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
          measurementFilterName = name.getText().trim();
        }

        if (measurementFilterName == null) {
          logger.warning("Measurement filter without name.");
          continue;
        }

        // Add the measurement to registery if it is a plug in
        for (Iterator i4 = filter.elementIterator("class"); i4.hasNext();) {
          final Element clazz = (Element) i4.next();
          String measurementClass = clazz.getText().trim();
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
          for (Object key : properties.keySet())
            mf.setInitParameter((String) key, (String) properties.get(key));

          list.add(mf);
        }
      }

    }

    final File genomeFile = this.design.getGenomeFile();
    final File outputDir = this.design.getOutputDir();
    final int windowSize = this.design.getWindowSize();
    final int oligoSize = this.design.getOligoLength();

    // Initialize the measurements
    for (MeasurementFilter m : list) {
      m.setInitParameter("_genomefile", genomeFile.getAbsolutePath());
      m.setInitParameter("_genomemaskedfile", genomeFile.getAbsolutePath());
      m.setInitParameter("_outputdir", outputDir.getAbsolutePath());
      m.setInitParameter("_windowsize", Integer.toString(windowSize));
      m.setInitParameter("_oligolength", Integer.toString(oligoSize));
      m.init();
    }

    return list;
  }

  private Select.WeightsSetter verifySelect(final Element rootElement)
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
          measurementName = name.getText().trim();
        }

        if (measurementName == null) {
          logger.warning("Measurement without name.");
          continue;
        }

        // Get the weight for the measurement
        for (Iterator i4 = measurement.elementIterator("weight"); i4.hasNext();) {
          final Element weight = (Element) i4.next();
          measurementWeight = weight.getText().trim();
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

        // TODO Auto-generated method stub

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
          pKey = name.getText().trim();
        }

        for (Iterator i7 = param.elementIterator("value"); i7.hasNext();) {
          final Element value = (Element) i7.next();
          pValue = value.getText().trim();

          if ("${windowsize}".equals(pValue))
            pValue = Integer.toString(this.design.getWindowSize());
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

  //
  // Main method
  //

  public static void main(String[] args) throws IOException, DocumentException {

    CLI cli = new CLI();
    cli.readDesign(new File(args[0]), null, null, null);

  }

}