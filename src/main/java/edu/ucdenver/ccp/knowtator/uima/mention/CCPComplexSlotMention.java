/* Apache UIMA v3 - First created by JCasGen Fri Apr 06 15:53:51 MDT 2018 */

package edu.ucdenver.ccp.knowtator.uima.mention;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.FSArray;

/**
 * A slot mention is deemed "complex" when its slot filler is a class mention as opposed to a String
 * (See non-complex slot mention for String fillers). An example of a complex slot mention is the
 * "transported entity" slot for the protein-transport class which would be filled with a protein
 * class mention. Updated by JCasGen Fri Apr 06 16:53:14 MDT 2018 XML source:
 * E:/Documents/GDrive/Projects/Knowtator/KnowtatorStandalone/src/main/resources/CcpTypeSystem.xml
 *
 * @generated
 */
@SuppressWarnings("unchecked")
public class CCPComplexSlotMention extends CCPSlotMention {

  /**
   * The constant _TypeName.
   *
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final String _TypeName =
      "edu.ucdenver.ccp.knowtator.uima.mention.CCPComplexSlotMention";

  /**
   * The constant typeIndexID.
   *
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int typeIndexID = JCasRegistry.register(CCPComplexSlotMention.class);
  /**
   * The constant type.
   *
   * @generated
   * @ordered
   */
  @SuppressWarnings("hiding")
  public static final int type = typeIndexID;
  /**
   * Gets type index id.
   *
   * @return index of the type
   * @generated
   */
  @Override
  public int getTypeIndexID() {
    return typeIndexID;
  }

  /* *******************
   *   Feature Offsets *
   * *******************/

  /** The constant _FeatName_classMentions. */
  public static final String _FeatName_classMentions = "classMentions";

  /* Feature Adjusted Offsets */
  private static final CallSite _FC_classMentions =
      TypeSystemImpl.createCallSite(CCPComplexSlotMention.class, "classMentions");
  private static final MethodHandle _FH_classMentions = _FC_classMentions.dynamicInvoker();

  /**
   * Never called. Disable default constructor
   *
   * @generated
   */
  protected CCPComplexSlotMention() {
    /* intentionally empty block */
  }

  /**
   * Internal - constructor used by generator
   *
   * @param type the type of this Feature Structure
   * @param casImpl the CAS this Feature Structure belongs to
   * @generated
   */
  public CCPComplexSlotMention(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }

  /**
   * Instantiates a new Ccp complex slot mention.
   *
   * @param jcas JCas to which this Feature Structure belongs
   * @generated
   */
  public CCPComplexSlotMention(JCas jcas) {
    super(jcas);
    readObject();
  }

  /**
   *
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable
   */
  private void readObject() {
    /*default - does nothing empty block */
  }

  // *--------------*
  // * Feature: classMentions

  /**
   * getter for classMentions - gets The class mentions which are the slot fillers for this complex
   * slot.
   *
   * @return value of the feature
   * @generated
   */
  public FSArray getClassMentions() {
    return (FSArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_classMentions)));
  }

  /**
   * setter for classMentions - sets The class mentions which are the slot fillers for this complex
   * slot.
   *
   * @param v value to set into the feature
   * @generated
   */
  public void setClassMentions(FSArray v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_classMentions), v);
  }

  /**
   * indexed getter for classMentions - gets an indexed value - The class mentions which are the
   * slot fillers for this complex slot.
   *
   * @param i index in the array to get
   * @return value of the element at index i
   * @generated
   */
  public CCPClassMention getClassMentions(int i) {
    return (CCPClassMention)
        (((FSArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_classMentions)))).get(i));
  }

  /**
   * indexed setter for classMentions - sets an indexed value - The class mentions which are the
   * slot fillers for this complex slot.
   *
   * @param i index in the array to set
   * @param v value to set into the array
   * @generated
   */
  public void setClassMentions(int i, CCPClassMention v) {
    ((FSArray) (_getFeatureValueNc(wrapGetIntCatchException(_FH_classMentions)))).set(i, v);
  }
}
