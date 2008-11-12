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

package fr.ens.transcriptome.teolenn.util;

import java.io.IOException;
import java.io.Writer;

public class UnSynchronizedBufferedWriter extends Writer {

  private final static int CAPACITY = 8192;

  private char[] buffer = new char[CAPACITY];
  private int position = 0;
  private Writer out;
  private boolean closed = false;

  public UnSynchronizedBufferedWriter(Writer out) {
    this.out = out;
  }

  public void write(char[] text, int offset, int length) throws IOException {
    checkClosed();
    while (length > 0) {
      int n = Math.min(CAPACITY - position, length);
      System.arraycopy(text, offset, buffer, position, n);
      position += n;
      offset += n;
      length -= n;
      if (position >= CAPACITY)
        flushInternal();
    }
  }

  public void write(String s) throws IOException {
    write(s, 0, s.length());
  }

  public void write(String s, int offset, int length) throws IOException {
    checkClosed();
    while (length > 0) {
      int n = Math.min(CAPACITY - position, length);
      s.getChars(offset, offset + n, buffer, position);
      position += n;
      offset += n;
      length -= n;
      if (position >= CAPACITY)
        flushInternal();
    }
  }

  public void write(int c) throws IOException {
    checkClosed();
    if (position >= CAPACITY)
      flushInternal();
    buffer[position] = (char) c;
    position++;
  }

  public void flush() throws IOException {
    flushInternal();
    out.flush();
  }

  private void flushInternal() throws IOException {
    if (position != 0) {
      out.write(buffer, 0, position);
      position = 0;
    }
  }

  public void close() throws IOException {
    closed = true;
    this.flush();
    out.close();
  }

  private void checkClosed() throws IOException {
    if (closed)
      throw new IOException("Writer is closed");
  }
}
