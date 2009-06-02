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

/**
 * This interface that define a Teolenn module.
 * @author Laurent Jourdren
 */
public interface Module {

  /**
   * Get the name of the module.
   * @return the name of the module
   */
  String getName();

  /**
   * Get the description of the module.
   * @return the description of the module
   */
  String getDescription();

  /**
   * Set a parameter for the module.
   * @param key key for the parameter
   * @param value value of the parameter
   */
  void setInitParameter(String key, String value);

  /**
   * Run the initialization phase of the module.
   * @throws TeolennException if an error occurs while the initialization phase
   */
  void init() throws TeolennException;

}
