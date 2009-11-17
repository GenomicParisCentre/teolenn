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

package fr.ens.transcriptome.teolenn.selector;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import fr.ens.transcriptome.teolenn.Globals;

/**
 * This class define a registery for sequence selector
 * @author Laurent Jourdren
 */
public class SequenceSelectorRegistery {
  private static Logger logger = Logger.getLogger(Globals.APP_NAME);

  private static Map<String, Class> registery = new HashMap<String, Class>();

  /**
   * Add a sequence selector
   * @param name name of the sequence selector
   * @param clazz Class of the sequence selector
   */
  public static void addSequenceSelectorType(final String name,
      final Class clazz) {

    if (name == null || clazz == null)
      return;

    final String lowerName = name.toLowerCase();

    if (registery.containsKey(lowerName))
      logger.warning("Selector "
          + name + " already exits, override previous selector.");

    registery.put(lowerName, clazz);
  }

  /**
   * Add a sequence selector
   * @param name name of the sequence selector
   * @param className Name of of the sequence selector
   */
  public static void addSequenceSelectorType(final String name,
      final String className) {

    if (name == null || "".equals(name) || className == null)
      return;

    try {
      Class clazz = SequenceSelectorRegistery.class.forName(className);

      addSequenceSelectorType(name.toLowerCase(), clazz);

      logger.info("Add external selector: " + name);

    } catch (ClassNotFoundException e) {

      logger.severe("Cannot find "
          + className + " for " + name + " selector addon");
      throw new RuntimeException("Cannot find "
          + className + " for " + name + " selector addon");

    }
  }

  private static boolean testClassType(final Class clazz) {

    if (clazz == null)
      return false;

    try {
      return clazz.newInstance() instanceof SequenceSelector;
    } catch (InstantiationException e) {
      logger.severe("Can't create instance of "
          + clazz.getName()
          + ". Maybe your class doesn't have a void constructor.");
    } catch (IllegalAccessException e) {
      logger.severe("Can't access to " + clazz.getName());
    }

    return false;
  }

  /**
   * Get a new instance of a sequence selector from its name.
   * @param name The name of the sequence selector filter to get
   * @return a new instance of a sequence selector or null if the requested
   *         sequence selector doesn't exists
   */
  public static SequenceSelector getSequenceSelector(final String name) {

    if (name == null)
      return null;

    Class clazz = registery.get(name.toLowerCase());

    if (clazz == null)
      return null;

    try {

      return (SequenceSelector) clazz.newInstance();

    } catch (InstantiationException e) {
      System.err.println("Unable to instantiate "
          + name
          + " filter. Maybe this filter doesn't have a void constructor.");
      logger.severe("Unable to instantiate "
          + name
          + " filter. Maybe this filter doesn't have a void constructor.");
      return null;
    } catch (IllegalAccessException e) {
      return null;
    }

  }

  //
  // Static Constructor
  //

  static {

    addSequenceSelectorType(TilingSelector.SELECTOR_NAME, TilingSelector.class);
    addSequenceSelectorType(ZoneSelector.SELECTOR_NAME, ZoneSelector.class);
  }

}
