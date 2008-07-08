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

public class Oligo {

  private String sequence;
  private float complexity;
  private float gcPercent;
  private float tm;
  private float unicity;
  private float deltaG;
  
  public String getSequence() {
    return sequence;
  }
  public float getComplexity() {
    return complexity;
  }
  public float getGcPercent() {
    return gcPercent;
  }
  public float getTm() {
    return tm;
  }
  public float getUnicity() {
    return unicity;
  }
  public float getDeltaG() {
    return deltaG;
  }
  public void setSequence(String sequence) {
    this.sequence = sequence;
  }
  public void setComplexity(float complexity) {
    this.complexity = complexity;
  }
  public void setGcPercent(float gcPercent) {
    this.gcPercent = gcPercent;
  }
  public void setTm(float tm) {
    this.tm = tm;
  }
  public void setUnicity(float unicity) {
    this.unicity = unicity;
  }
  public void setDeltaG(float deltaG) {
    this.deltaG = deltaG;
  }
  
  
  
  
}
