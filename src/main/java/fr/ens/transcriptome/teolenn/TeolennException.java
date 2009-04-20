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

package fr.ens.transcriptome.teolenn;

/**
 * A nestable Teolenn exception. This class came from from Biojava code.
 * @author Laurent Jourdren
 * @author Matthew Pocock
 */
public class TeolennException extends Exception {

  /**
   * Create a new TeolennException with a message.
   * @param message the message
   */
  public TeolennException(final String message) {
    super(message);
  }

  /**
   * Create a new TeolennException with a cause.
   * @param ex the Throwable that caused this NividicException
   */
  public TeolennException(final Throwable ex) {
    super(ex);
  }

  /**
   * Create a new TeolennException with a cause and a message.
   * @param ex the Throwable that caused this NividicException
   * @param message the message
   * @deprecated use new NividicException(message, ex) instead
   */
  public TeolennException(final Throwable ex, final String message) {
    this(message, ex);
  }

  /**
   * Create a new TeolennException with a cause and a message.
   * @param message the message
   * @param ex the Throwable that caused this NividicException
   */
  public TeolennException(final String message, final Throwable ex) {
    super(message, ex);
  }

  /**
   * Create a new TeolennException.
   */
  public TeolennException() {
    super();
  }
}
