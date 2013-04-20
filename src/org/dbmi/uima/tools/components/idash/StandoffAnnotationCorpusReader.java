package org.dbmi.uima.tools.components.idash;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.examples.SourceDocumentInformation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;
import org.dbmi.uima.tools.components.idash.type.ClassLabel;

public class StandoffAnnotationCorpusReader extends CollectionReader_ImplBase {

	public static final String PARAM_CORPUS_PATH = "CorpusLocation";
	public static final String PARAM_LABEL_NAMES = "LabelNames";
	public static final String PARAM_ENCODING = "Encoding";
	public static final String PARAM_LANGUAGE = "Language";
	
	// LOG4J logger based on class name
	private Logger logger = Logger.getLogger(getClass().getName());
		
	// Invalid XML Characters to strip out of files
	Pattern INVALID_XML_CHARS = Pattern.compile(
			"[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\uD800\uDC00-\uDBFF\uDFFF]");
	
	ArrayList<File> mDataFiles;
	ArrayList<File> mLabelFiles;
	
	// Class labels to read for each document (other annotations will be discarded)
	HashSet<String> mClassLabels;
	String mEncoding;
	String mLanguage;
	
	int mCurrentIndex; // current file being processed
	
	@Override
	public void initialize() throws ResourceInitializationException {
		// Load config parameters
		File directory = new File(((String) getConfigParameterValue(PARAM_CORPUS_PATH)).trim());
		mEncoding  = (String) getConfigParameterValue(PARAM_ENCODING);
	    mLanguage  = (String) getConfigParameterValue(PARAM_LANGUAGE);
	    mClassLabels = new HashSet<String>();
	    for(String lblName : (String[]) getConfigParameterValue(PARAM_LABEL_NAMES))
	    	mClassLabels.add(lblName);
	    logger.debug("These labels will be added to the encoded dataset: ".
	    		concat(mClassLabels.toString()));
	    
	    mDataFiles = new ArrayList<File>();
	    mLabelFiles = new ArrayList<File>();
	    mCurrentIndex = 0;
	    
	    // Get the docs and man_anns directory for data and labels respectively
	    File[] topLevelDirs = directory.listFiles();
	    File docsDirectory = null, labelsDirectory = null;
	    for (File f : topLevelDirs) {
	    	String fname = f.getName().trim();
	    	if("docs".equals(fname) && f.isDirectory())
	    		docsDirectory = f;
	    	else if("man_anns".equals(fname) && f.isDirectory())
	    		labelsDirectory = f;
	    }
	    if (docsDirectory == null)
	    	throw new ResourceInitializationException(
	    			"No 'docs' directory found in corpus!", null);
	    if(labelsDirectory == null)
	    	logger.info(
	    		"No 'man_anns' directory found in corpus. No labels will be encoded.");
	    
	    // Add the documents in the doc directory
	    for (File f : docsDirectory.listFiles()) {
	    	if (f.isDirectory()) {
	    		File[] dataFiles = f.listFiles(new FilenameFilter() {
	    		    public boolean accept(File dir, String name) {
	    		        return name.toLowerCase().endsWith("report.txt");
	    		    }
	    		});
	    		
	    		if(dataFiles.length > 0) {
	    			mDataFiles.add(dataFiles[0]);
	    			logger.debug("Queued data file: ".concat(dataFiles[0].getPath()));
	    			
	    			if(labelsDirectory != null) {
	    				String labelFilePath = String.format("%s%s%s%s%s", labelsDirectory.getPath(),
	    						File.separator, f.getName(), File.separator, dataFiles[0].getName());
	    				mLabelFiles.add(new File(labelFilePath));
	    				logger.debug("Queued label file: ".concat(labelFilePath));
	    			}
	    		}
	    	}
	    }
	    
	}
	
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return mCurrentIndex < mDataFiles.size();
	}

	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		JCas jcas;
	    try {
	      jcas = aCAS.getJCas();
	    } catch (CASException e) {
	      throw new CollectionException(e);
	    }
	    
	    // open input stream to file
	    File file = (File) mDataFiles.get(mCurrentIndex);
	    String text = FileUtils.file2String(file, mEncoding);
	    String cleanText = INVALID_XML_CHARS.matcher(text).replaceAll("");
	      // put document in CAS
	    jcas.setDocumentText(cleanText);

	    // set language if it was explicitly specified as a configuration parameter
	    if (mLanguage != null) {
	      ((DocumentAnnotation) jcas.getDocumentAnnotationFs()).setLanguage(mLanguage);
	    }

	    // Also store location of source document in CAS. This information is critical
	    // if CAS Consumers will need to know where the original document contents are located.
	    addSourceDocumentInformation(jcas, new File(file.getParent()));
	    
	    // Add class labels to the CAS
	    addClassLabels(jcas);

	    mCurrentIndex++;
	    
	}
	
	private void addSourceDocumentInformation(JCas aJCas, File file) throws MalformedURLException {
		SourceDocumentInformation srcDocInfo = new SourceDocumentInformation(aJCas);
	    srcDocInfo.setUri(file.getAbsoluteFile().toURL().toString());
	    srcDocInfo.setOffsetInSource(0);
	    srcDocInfo.setDocumentSize((int) file.length());
	    srcDocInfo.setLastSegment(mCurrentIndex == mDataFiles.size());
	    srcDocInfo.addToIndexes();
	}
	
	private void addClassLabels(JCas aJCas) throws IOException {
		if(mLabelFiles.size() == 0)
			return;
		File file = (File) mLabelFiles.get(mCurrentIndex);
	    String text = FileUtils.file2String(file, mEncoding);
	    for(String line : text.split("\\n")) {
	    	if (line.startsWith("#"))
	    		continue;
	    	String[] columns = line.split("\\t");
	    	if (columns.length > 4 && mClassLabels.contains(columns[3])) {
	    		try {
	    			ClassLabel label = new ClassLabel(aJCas);
		    		label.setLabelName(columns[3]);
		    		//label.setLabelValue(Integer.parseInt(columns[4].replaceAll("\\D", "")));
		    		label.setLabelValue(columns[4].trim());
		    		label.addToIndexes();
	    		} catch (NumberFormatException ex) {
	    			logger.warn(String.format(
	    					"Unable to add label '%s' for document %s", columns[3], file.getPath()));
	    		}
	    	}
	    }
	}
	
	@Override
	public Progress[] getProgress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}
}
