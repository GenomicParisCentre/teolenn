package fr.ens.transcriptome.teolenn;

/**
 * This abstract class allow the user to set weights and other parameters
 * before selection.
 * @author Laurent Jourdren
 */
public abstract class WeightsSetter {

  /**
   * Define the weigths to use for selection
   * @param sm SequenceMeasurement that contains all the measurements to use
   *          by the selection algorithm.
   */
  public abstract void setWeights(final SequenceMeasurements sm);
}