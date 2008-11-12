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

package fr.ens.transcriptome.teolenn.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class implements a thread poll for parralization of iterations.
 * @author Laurent Jourdren
 */
public abstract class PoolLoopHandler implements Runnable {
  protected static class LoopRange {
    public int start, end;
  }

  protected static class PoolHandlerFactory implements ThreadFactory {

    public Thread newThread(Runnable r) {
      Thread t = new Thread(r);
      t.setDaemon(true);
      return t;
    }
  }

  static protected ThreadPoolExecutor threadpool;
  static protected int maxThreads = 1;
  protected int startLoop, endLoop, curLoop, numThreads;

  synchronized static void getThreadPool(int threads) {
    if (threadpool == null)
      threadpool =
          new ThreadPoolExecutor(1, 1, 50000L, TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<Runnable>(), new PoolHandlerFactory());
    if (threads > maxThreads) {
      maxThreads = threads;
      threadpool.setMaximumPoolSize(maxThreads);
      threadpool.setCorePoolSize(maxThreads);
    }
  }

  public PoolLoopHandler(int start, int end, int threads) {
    numThreads = threads;
    getThreadPool(numThreads);
    setRange(start, end);
  }

  public synchronized void setRange(int start, int end) {
    startLoop = start;
    endLoop = end;
    reset();
  }

  public synchronized void reset() {
    curLoop = startLoop;
  }

  protected synchronized LoopRange loopGetRange() {
    if (curLoop >= endLoop)
      return null;
    LoopRange ret = new LoopRange();
    ret.start = curLoop;
    curLoop += (endLoop - startLoop) / numThreads + 1;
    ret.end = (curLoop < endLoop) ? curLoop : endLoop;
    return ret;
  }

  public abstract void loopDoRange(final int start, final int end);

  public void loopProcess() {
    reset();
    FutureTask t[] = new FutureTask[numThreads];
    for (int i = 0; i < numThreads; i++) {
      t[i] = new FutureTask<PoolLoopHandler>(this, null);
      threadpool.execute(t[i]);
    }
    for (int i = 0; i < numThreads; i++) {
      try {
        t[i].get();
      } catch (ExecutionException ee) {
      } catch (InterruptedException ie) {
      }
    }
  }

  public void run() {
    LoopRange str;
    while ((str = loopGetRange()) != null) {
      loopDoRange(str.start, str.end);
    }
  }
}
