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

package fr.ens.transcriptome.teolenn.util;

/**
 * This class implements a loopHandler with self scheduling.
 * @author Laurent Jourdren
 */
public abstract class SelfLoopHandler extends PoolLoopHandler {
  protected int groupSize;

  public SelfLoopHandler(int start, int end, int size, int threads) {

    super(start, end, threads);
    groupSize = size;
  }

  protected synchronized LoopRange loopGetRange() {

    if (curLoop >= endLoop)
      return null;
    LoopRange ret = new LoopRange();
    ret.start = curLoop;
    curLoop += groupSize;
    ret.end = (curLoop < endLoop) ? curLoop : endLoop;

    return ret;
  }
}
