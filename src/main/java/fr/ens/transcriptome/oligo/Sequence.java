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

public class Sequence {

  private int id;
  private String sequence;
  private String name;

  public final String getSequence() {

    return sequence;
  }

  public final void setSequence(final String sequence) {
    this.sequence = sequence;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public final float getTm() {

    return getTm(50, 50);
  }

  public final float getTm(final float dnac, final float saltc) {

    return MeltingTemp.tmstalucDNA(this.sequence, dnac, saltc);
  }

  public final float getGCPercent() {

    if (this.sequence == null)
      return Float.NaN;

    final int len = this.sequence.length();

    int count = 0;

    for (int i = 0; i < len; i++) {

      if (this.sequence.charAt(i) == 'G' || this.sequence.charAt(i) == 'C')
        count++;
    }

    return (float) count / (float) len;
  }

}
