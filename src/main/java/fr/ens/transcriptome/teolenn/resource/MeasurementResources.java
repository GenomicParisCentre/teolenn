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

package fr.ens.transcriptome.teolenn.resource;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements as a singleton, data that can be shared between two or
 * more Measurement. It can avoid to load many time the same information.
 * @author Laurent Jourdren
 */
public final class MeasurementResources {

  private Map<String, Object> resources = new HashMap<String, Object>();
  private static MeasurementResources singleton;

  /**
   * Test if a resource exists.
   * @param resourceName the name of the resource
   * @return a resource object.
   */
  public boolean isResource(final String resourceName) {

    return this.resources.containsKey(resourceName);
  }

  /**
   * Get a resource.
   * @param resourceName the name of the resource
   * @return a resource object.
   */
  public Object getResource(final String resourceName) {

    return this.resources.get(resourceName);
  }

  /**
   * Set a resource.
   * @param resourceName name of the resource to set
   * @param resource the resource object
   */
  public void setResource(final String resourceName, final Object resource) {

    this.resources.put(resourceName, resource);
  }

  /**
   * Clear all the resources.
   */
  public void clear() {

    this.resources.clear();
  }

  //
  // Constructor
  //

  private MeasurementResources() {
  }

  //
  // static methods
  //

  /**
   * Get the resources
   * @return a measurement resource
   */
  public static final MeasurementResources getResources() {

    if (singleton == null)
      singleton = new MeasurementResources();

    return singleton;
  }
}
