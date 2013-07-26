package org.dbmi.uima.tools.components.idash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.dbmi.uima.tools.components.idash.type.ClassLabel;

public class XMLCorpusReader extends CollectionReader_ImplBase {

	/**
	 * These parameters seek to decipher an xml file of the form
	 * <DocumentElementName DocumentIdAttribute="doc id">
	 * 	<TagSurroundingText>
	 * 		Clinical NLP Text goes here.
	 * 	</TagSurroundingText>
	 * </DocumentElementName>
	 */
	public static final String PARAM_DATA_PATH = "DataFilePath";
	public static final String PARAM_LABEL_PATH = "LabelFilePath";
	public static final String PARAM_DOC_ID_ELEMENTNAME = "DocumentElementName";
	public static final String PARAM_DOC_ID_ATTRIBUTENAME = "DocumentIdAttribute";
	public static final String PARAM_TEXTTAG = "TagSurroundingText";
	
	/**
	 * These parameters seek to decipher an xml file of the form
	 * <MultiLabelName MultilabelLabelAttribute="label name">
	 * 	<MultilabelDocTag MultilabelDocIdAttribute = "doc id" MultilabelDocValueAttribute = "Y" />
	 * </DocumentElementName>
	 */
	public static final String PARAM_MULTILABEL_LABELTAG = "MultilabelLabelName";
	public static final String PARAM_MULTILABEL_LABELATTRIBUTE = "MultilabelLabelAttribute";
	public static final String PARAM_MULTILABEL_DOCTAG = "MultilabelDocTag";
	public static final String PARAM_MULTILABEL_DOCIDATTRIBUTE = "MultilabelDocIdAttribute";
	public static final String PARAM_MULTILABEL_VALUEATTRIBUTE = "MultilabelDocValueAttribute";
	private String doc, docId, text;
	private String lLabel, lName, lDoc, lDocId, lValue;
	
	// LOG4J logger based on class name
	private Logger logger = Logger.getLogger(getClass().getName());
		
	// Invalid XML Characters to strip out of files
	Pattern INVALID_XML_CHARS = Pattern.compile(
			"[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\uD800\uDC00-\uDBFF\uDFFF]");
	
	// XML reader for data file
	XMLStreamReader mDataFileStream;
	
	// We read the next document's text and id and keep it ready here for a getNext() call.
	String mCurrentDocText, mCurrentDocId;
	int numDocumentsRead;
	
	// Class labels to read for each document (other annotations will be discarded)
	HashMap<String, Map<String, String>> mDocumentLabels;
	URL mDataPathURL;
	
	@Override
	public void initialize() throws ResourceInitializationException {
		// Load Config parameters
		doc = ((String) getConfigParameterValue(PARAM_DOC_ID_ELEMENTNAME)).trim();
		docId = ((String) getConfigParameterValue(PARAM_DOC_ID_ATTRIBUTENAME)).trim();
		text = ((String) getConfigParameterValue(PARAM_TEXTTAG)).trim();
		
		lLabel = ((String) getConfigParameterValue(PARAM_MULTILABEL_LABELTAG)).trim();
		lName = ((String) getConfigParameterValue(PARAM_MULTILABEL_LABELATTRIBUTE)).trim();
		lDoc = ((String) getConfigParameterValue(PARAM_MULTILABEL_DOCTAG)).trim();
		lDocId = ((String) getConfigParameterValue(PARAM_MULTILABEL_DOCIDATTRIBUTE)).trim();
		lValue = ((String) getConfigParameterValue(PARAM_MULTILABEL_VALUEATTRIBUTE)).trim();
		
		mDocumentLabels = new HashMap<String, Map<String, String>>();
		
		
		try {
			String filePath = ((String) getConfigParameterValue(PARAM_DATA_PATH)).trim();
			mDataFileStream = XMLInputFactory.newInstance().createXMLStreamReader(
					new FileInputStream(filePath));
			mDataPathURL = new File(filePath).getParentFile().getAbsoluteFile().
					toURI().toURL();
			
			numDocumentsRead = 0;
			
			// Read the labels first
			//readI2b2styleLabelFile();
			readCMCstyleLabelFile();
			
			// Read the first document and keep it ready for getNext() call
			readNextDocument();
			
		} catch (FileNotFoundException | XMLStreamException | FactoryConfigurationError | MalformedURLException e) {
			logger.error(e.getStackTrace().toString());
			throw new ResourceInitializationException(e);
		}	    
	}
	
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		//Temp debug limit
		//if (numDocumentsRead > 2)
		//	return false;
		return mCurrentDocText != null;
	}

	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		JCas jcas;
	    try {
	    	jcas = aCAS.getJCas();
	      
	    	// put document in CAS
			if(mCurrentDocText != null) {
				jcas.setDocumentText(mCurrentDocText);
				numDocumentsRead++;
				String currentDocId = mCurrentDocId;
				int currentDocLength = mCurrentDocText.length();
				
				// Fetch the next document data for next iteration
				readNextDocument();
				
				// Store location of source document in CAS. This information is critical
			    // if CAS Consumers will need to know where the original document contents are located.
			    addSourceDocumentInformation(jcas, currentDocId, currentDocLength);
			    
			    // Add class labels to the CAS
			    addClassLabels(jcas, currentDocId);
			}
	    } catch (CASException e) {
	    	throw new CollectionException(e);
	    } catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}
	
	private void addSourceDocumentInformation(JCas aJCas, 
			String documentName, int length) {
		SourceDocumentInformation srcDocInfo = new SourceDocumentInformation(aJCas);
		
		//TODO: Clean this up
	    srcDocInfo.setUri(String.format("%s/%s", mDataPathURL.toString(), documentName));
	    srcDocInfo.setOffsetInSource(0);
	    srcDocInfo.setDocumentSize(length);
	    srcDocInfo.setLastSegment(mCurrentDocId == null);
	    srcDocInfo.addToIndexes();
	}
	
	private void addClassLabels(JCas aJCas, String documentId) {
		
		// For each label
		for(String labelName : mDocumentLabels.keySet()) {
			Map<String, String> documentLabelMapping = mDocumentLabels.get(labelName);
			ClassLabel label = new ClassLabel(aJCas);
			label.setLabelName(labelName);
			String labelValue = documentLabelMapping.get(documentId);
			if (labelValue == null)
				logger.error(
						new StringBuilder("Document ").append(documentId).append(
						" has a missing label ").append(labelName).toString());
			label.setLabelValue(labelValue);
			label.addToIndexes();
		}
	}
	
	@Override
	public Progress[] getProgress() {
		return new Progress[] {
		  new ProgressImpl(numDocumentsRead, 0, "Documents (total unknown)")
		};
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}
	
	/*
	 * Read labels from the i2b2 dataset's structure, which looks like this:
	 * <?xml version="1.0" encoding="UTF-8"?>
	 * 	<diseaseset>
	 * 		<diseases source="intuitive">
	 * 			<disease name="Asthma">
	 * 				<doc id="1" judgment="N"/>
	 * 				<doc id="2" judgment="Y"/>
	 * 				...
	 * 			</disease>
	 * 		</diseases>
	 * 	</diseaseset>
	 * We assume there are no duplicate labels, i.e. disease "Asthma" shouldnt exist more than once
	 * in the file. In terms of the i2b2 dataset, please use the separate intuitive / textual labels and provide
	 * ONE of either of them to this program.
	 */
  @SuppressWarnings("unused")
  private void readI2b2styleLabelFile() throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
		String labelFilePath = ((String) getConfigParameterValue(PARAM_LABEL_PATH)).trim();
		XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(
				new FileInputStream(labelFilePath));

		// doc id vs label value mapping
		Map<String, String> currentLabelMapping = null;
		String currentLabelName;
		while (xmlStreamReader.hasNext()) {
			switch (xmlStreamReader.next()) {
			case XMLStreamReader.START_ELEMENT:
				String elementName = xmlStreamReader.getLocalName(); 
				if(lLabel.equals(elementName)) {
					currentLabelName = xmlStreamReader.getAttributeValue(null, lName);
					currentLabelMapping = new HashMap<String, String>();
					if(mDocumentLabels.containsKey(currentLabelName))
						logger.warn("Label exists more than once in the file: ".concat(currentLabelName));
					mDocumentLabels.put(currentLabelName, currentLabelMapping);
				} else if(lDoc.equals(elementName)) {
					logger.debug(new StringBuilder("Added class value ").append(xmlStreamReader.getAttributeValue(null, lValue)).append(
							" for document id ").append(xmlStreamReader.getAttributeValue(null, lDocId)).toString());
					currentLabelMapping.put(xmlStreamReader.getAttributeValue(null, lDocId),
							xmlStreamReader.getAttributeValue(null, lValue));
				}
				break;
			case XMLStreamReader.END_ELEMENT:
				if(lLabel.equals(xmlStreamReader.getLocalName())) {
					currentLabelName = "NULL";
					currentLabelMapping = null;
				}
			}
		}
	    xmlStreamReader.close();

	    logger.info(String.format("The following labels were read from the file '%s': %s", 
	    		labelFilePath, mDocumentLabels.keySet().toString() ));
	}
	
	private void readCMCstyleLabelFile() throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
		String labelFilePath = ((String) getConfigParameterValue(PARAM_LABEL_PATH)).trim();
		XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(
				new FileInputStream(labelFilePath));

		String lCurrentDocId = null;
		boolean readLabel = false;
		mDocumentLabels.clear();
		Map<String, String> currentLabelMapping;
		StringBuilder sb = new StringBuilder("Labels assigned to documents:");
		while (xmlStreamReader.hasNext()) {
			switch (xmlStreamReader.next()) {
			case XMLStreamReader.START_ELEMENT:
				String elementName = xmlStreamReader.getLocalName();
				if (doc.equals(elementName)) {
					lCurrentDocId = xmlStreamReader.getAttributeValue(null, docId).trim();
					sb.append("\n\t").append(lCurrentDocId).append(";\tLabels = ");
				}
				else if("code".equalsIgnoreCase(elementName)) {
					String codingCompany = xmlStreamReader.getAttributeValue(null, "origin").trim();
					if ("CMC_MAJORITY".equalsIgnoreCase(codingCompany.toUpperCase()))
							readLabel = true;
				}
				break;
			case XMLStreamReader.CHARACTERS:
				// If this event occurs within a <code> </code> block, read label
				if (readLabel) {
					String ICD9Code = xmlStreamReader.getText();
					if (mDocumentLabels.containsKey(ICD9Code))
						currentLabelMapping = mDocumentLabels.get(ICD9Code);
					else 
						currentLabelMapping = new HashMap<>();
					currentLabelMapping.put(lCurrentDocId, "1");
					sb.append(ICD9Code).append(", ");
					mDocumentLabels.put(ICD9Code, currentLabelMapping);
				}
				break;
			case XMLStreamReader.END_ELEMENT:
				String elName = xmlStreamReader.getLocalName();
				if ("code".equalsIgnoreCase(elName))
					readLabel = false;
				break;
			default:
				break;
			}
		}
	    xmlStreamReader.close();

	    logger.info(String.format("The following %d labels were read from the file '%s': %s", 
	    		mDocumentLabels.size(), labelFilePath, mDocumentLabels.keySet().toString() ));
	    logger.debug(sb.toString());
	}
	

	/* 
	 * Parse the data file and store the next document in mCurrentDocText
	 */
	private void readNextDocument() throws XMLStreamException {
		// Set current doc data to null incase the following fetching process fails
		mCurrentDocId = null;
		mCurrentDocText = null;
		
		// Fetch the next document id, and contained text.
		boolean collectText = false;
		StringBuilder docTextBuilder = new StringBuilder();
		while (mDataFileStream.hasNext()) {
			switch (mDataFileStream.next()) {
			case XMLStreamReader.START_ELEMENT:
				String elementName = mDataFileStream.getLocalName();
				if (doc.equals(elementName))
					mCurrentDocId = mDataFileStream.getAttributeValue(null, docId).trim();
				else if(text.equals(elementName))
					collectText = true;
				break;
			case XMLStreamReader.CHARACTERS:
				// If this event occurs within a <text> </text> block, add to doc text
				if (collectText) {
					docTextBuilder.append(" "); // incase a previous text block was parsed and stored.
					docTextBuilder.append(mDataFileStream.getText());
				}
				break;
			case XMLStreamReader.END_ELEMENT:
				String elName = mDataFileStream.getLocalName();
				if (text.equals(elName))
					collectText = false;
				else if(doc.equals(elName)) {
					// Finish processing of this doc (exit)
					mCurrentDocText = INVALID_XML_CHARS.matcher(docTextBuilder).
							replaceAll("");
					return;
				}
				break;
			default:
				break;
			}
		}
	}
}

