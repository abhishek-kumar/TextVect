
/* First created by JCasGen Mon Feb 11 21:59:36 PST 2013 */
package org.dbmi.uima.tools.components.idash.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** Assigns a label to the document, with a numerical value
 * Updated by JCasGen Thu Feb 21 02:28:50 PST 2013
 * @generated */
public class ClassLabel_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      @Override
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ClassLabel_Type.this.useExistingInstance) {
  			 // Return eq fs instance if already created
  		     FeatureStructure fs = ClassLabel_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ClassLabel(addr, ClassLabel_Type.this);
  			   ClassLabel_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ClassLabel(addr, ClassLabel_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ClassLabel.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.dbmi.uima.tools.components.idash.type.ClassLabel");
 
  /** @generated */
  final Feature casFeat_labelName;
  /** @generated */
  final int     casFeatCode_labelName;
  /** @generated */ 
  public String getLabelName(int addr) {
        if (featOkTst && casFeat_labelName == null)
      jcas.throwFeatMissing("labelName", "org.dbmi.uima.tools.components.idash.type.ClassLabel");
    return ll_cas.ll_getStringValue(addr, casFeatCode_labelName);
  }
  /** @generated */    
  public void setLabelName(int addr, String v) {
        if (featOkTst && casFeat_labelName == null)
      jcas.throwFeatMissing("labelName", "org.dbmi.uima.tools.components.idash.type.ClassLabel");
    ll_cas.ll_setStringValue(addr, casFeatCode_labelName, v);}
    
  
 
  /** @generated */
  final Feature casFeat_labelValue;
  /** @generated */
  final int     casFeatCode_labelValue;
  /** @generated */ 
  public String getLabelValue(int addr) {
        if (featOkTst && casFeat_labelValue == null)
      jcas.throwFeatMissing("labelValue", "org.dbmi.uima.tools.components.idash.type.ClassLabel");
    return ll_cas.ll_getStringValue(addr, casFeatCode_labelValue);
  }
  /** @generated */    
  public void setLabelValue(int addr, String v) {
        if (featOkTst && casFeat_labelValue == null)
      jcas.throwFeatMissing("labelValue", "org.dbmi.uima.tools.components.idash.type.ClassLabel");
    ll_cas.ll_setStringValue(addr, casFeatCode_labelValue, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public ClassLabel_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_labelName = jcas.getRequiredFeatureDE(casType, "labelName", "uima.cas.String", featOkTst);
    casFeatCode_labelName  = (null == casFeat_labelName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_labelName).getCode();

 
    casFeat_labelValue = jcas.getRequiredFeatureDE(casType, "labelValue", "uima.cas.String", featOkTst);
    casFeatCode_labelValue  = (null == casFeat_labelValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_labelValue).getCode();

  }
}



    