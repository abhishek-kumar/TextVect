

/* First created by JCasGen Mon Feb 11 21:59:36 PST 2013 */
package org.dbmi.uima.tools.components.idash.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Assigns a label to the document, with a numerical value
 * Updated by JCasGen Thu Feb 21 02:28:50 PST 2013
 * XML source: /Users/abhishek/Workspaces/DBMI/TextVect/V3NLP/derma_2013.01.31/FeatureEncoder/desc/ClassLabelType.xml
 * @generated */
public class ClassLabel extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ClassLabel.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected ClassLabel() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public ClassLabel(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public ClassLabel(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public ClassLabel(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: labelName

  /** getter for labelName - gets 
   * @generated */
  public String getLabelName() {
    if (ClassLabel_Type.featOkTst && ((ClassLabel_Type)jcasType).casFeat_labelName == null)
      jcasType.jcas.throwFeatMissing("labelName", "org.dbmi.uima.tools.components.idash.type.ClassLabel");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ClassLabel_Type)jcasType).casFeatCode_labelName);}
    
  /** setter for labelName - sets  
   * @generated */
  public void setLabelName(String v) {
    if (ClassLabel_Type.featOkTst && ((ClassLabel_Type)jcasType).casFeat_labelName == null)
      jcasType.jcas.throwFeatMissing("labelName", "org.dbmi.uima.tools.components.idash.type.ClassLabel");
    jcasType.ll_cas.ll_setStringValue(addr, ((ClassLabel_Type)jcasType).casFeatCode_labelName, v);}    
   
    
  //*--------------*
  //* Feature: labelValue

  /** getter for labelValue - gets 
   * @generated */
  public String getLabelValue() {
    if (ClassLabel_Type.featOkTst && ((ClassLabel_Type)jcasType).casFeat_labelValue == null)
      jcasType.jcas.throwFeatMissing("labelValue", "org.dbmi.uima.tools.components.idash.type.ClassLabel");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ClassLabel_Type)jcasType).casFeatCode_labelValue);}
    
  /** setter for labelValue - sets  
   * @generated */
  public void setLabelValue(String v) {
    if (ClassLabel_Type.featOkTst && ((ClassLabel_Type)jcasType).casFeat_labelValue == null)
      jcasType.jcas.throwFeatMissing("labelValue", "org.dbmi.uima.tools.components.idash.type.ClassLabel");
    jcasType.ll_cas.ll_setStringValue(addr, ((ClassLabel_Type)jcasType).casFeatCode_labelValue, v);}    
  }

    