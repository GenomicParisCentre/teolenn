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

package fr.ens.transcriptome.teolenn.sequence;

/**
 * This class define a sequence.
 * @author Laurent Jourdren
 */
public class Sequence {

  private int id;
  private String sequence;
  private String name;

  /**
   * Get the sequence of the sequence.
   * @return a string with the sequence
   */
  public final String getSequence() {

    return sequence;
  }

  /**
   * Set the sequence.
   * @param sequence Sequence to set
   */
  public final void setSequence(final String sequence) {
    this.sequence = sequence;
  }

  /**
   * Get the id of the sequence.
   * @return the id of the sequence
   */
  public int getId() {
    return id;
  }

  /**
   * Set the id of the sequence
   * @param id id to set
   */
  public void setId(final int id) {
    this.id = id;
  }

  /**
   * Set the name of the sequence.
   * @return the name of the sequence
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the sequence
   * @param name the name to set
   */
  public void setName(final String name) {
    this.name = name;
  }

  public final float getTm() {

    return getTm(50, 50);
  }

  /**
   * Get the tm of the sequence.
   * @param dnac DNA concentration [nM]
   * @param saltc salt concentration [mM
   * @return the tm temp for the sequence
   */
  public final float getTm(final float dnac, final float saltc) {

    return MeltingTemp.tmstalucDNA(this.sequence, dnac, saltc);
  }

  /**
   * Get the GC percent for the sequence.
   * @return the GC percent for the sequenc
   */
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

  /**
   * Get the start position of the oligonucleotide.
   * @return the start position of the oligonucleotide
   */
  public int getStartPositionOligo() {

    if (this.name == null)
      return -1;

    int startPos = this.name.indexOf(":subseq(");
    int endPos = this.name.indexOf(",", startPos);

    return Integer.parseInt(this.name.substring(startPos + 8, endPos));
  }

  /**
   * Get the length of the oligonucleotide.
   * @return the length of the oligonucleotide
   */
  public int getLengthOligo() {

    int startPos = this.name.indexOf(":subseq(");
    int startPos2 = this.name.indexOf(",", startPos);
    int endPos = this.name.indexOf(")", startPos2);

    return Integer.parseInt(this.name.substring(startPos2 + 1, endPos));
  }

  /**
   * Override the default toString method.
   * @return a String representation of the Sequence
   */
  public String toString() {

    return "(" + this.name + "," + this.sequence + ")";
  }

  //
  // Constructors
  //

  /**
   * Public constructor.
   */
  public Sequence() {
  }

  /**
   * Public constructor.
   * @param name name of the sequence
   * @param sequence sequence itself
   */
  public Sequence(String name, String sequence) {

    setName(name);
    setSequence(sequence);
  }

}
