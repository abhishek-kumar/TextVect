package org.dbmi.uima.tools.components.idash;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Calculates statistics about a feature group (e.g. words)
 * which become useful when we need to do feature selection / reduction.
 * Everytime a feature is encountered in a document, the method 
 * {@link FeatureStats#process(String, Integer) process} is called to update counts.
 * In the end, {@link #removeRareTerms(int, int) removeRareTerms} gets rid of rare terms
 * 
 * @author abhishek
 */
public class FeatureStats {
	/* Total count of each term */
	Map<String, Integer> termCounts = new HashMap<String, Integer>();
	
	/* Document count for occurrences of each term */
	Map<String, Integer> termDocumentCounts = new HashMap<String, Integer>();
	
	/* Terms seen so far so that we don't account for a term twice in doc counts */
	Set<String> termsSeenSoFar = new HashSet<String>();
	
	int documentsSeenSoFar = 0;
	
	/**
	 * Prepare dataset for new document processing
	 */
	public void newDocument() {
		termsSeenSoFar.clear();
		++documentsSeenSoFar;
	}
	
	/**
	 * Process a {@code term} found in a document.
	 * {@link #process(String, Integer) process} should only be called once per document
	 * per term.
	 * @param term Name of the term found
	 * @param count Count of the term in the new document.
	 */
	public void process(String term, Integer count) {
		// Ignore null terms
		if(term == null)
			return;

		int prevCount = 0, prevDocCount = 0;
		if(termCounts.containsKey(term))
			prevCount = termCounts.get(term);
		if (termDocumentCounts.containsKey(term))
			prevDocCount = termDocumentCounts.get(term);
		
		// Add to term counts
		termCounts.put(term, prevCount + count);
		
		// Add to document count if this a new term for this document
		if(!termsSeenSoFar.contains(term)) {
			termsSeenSoFar.add(term);
			termDocumentCounts.put(term, prevDocCount + 1);
		}
	}
	
	public int size() {
		return termCounts.size();
	}
	
	public int getDocumentsSeenSoFar() {
		return documentsSeenSoFar;
	}
	
	public Set<String> getTerms() {
		return termCounts.keySet();
	}
	
	public Boolean termExists(String term) {
		return termCounts.containsKey(term);
	}
	
	/**
	 * Get the document frequency of a given term. Useful
	 * for calculating TF*IDF representation of features
	 * @param term
	 * @return
	 */
	public double getDocumentFrequency(String term) {
		if(termDocumentCounts.containsKey(term))
			return  ((double) (termDocumentCounts.get(term))) / 
					((double) (documentsSeenSoFar));
		else 
			return 0.0;
	}
	
	public int removeRareTerms(int termCountCutoff, int documentCountCutoff) {
		int numTermsRemoved = 0;
		
		// Remove all terms with termCount <= termCountCutoff
		// or termDocumentCount <= documentCountCutoff
		for (Iterator<Map.Entry<String, Integer>> it = termCounts.entrySet().iterator(); 
			     it.hasNext();) {
		    Map.Entry<String, Integer> entry = it.next();
		    String term = entry.getKey();
		    if(termCounts.get(term) <= termCountCutoff ||
			   termDocumentCounts.get(term) <= documentCountCutoff) {
				it.remove();
				termDocumentCounts.remove(term);
				++numTermsRemoved;
			}
		}
		
		return numTermsRemoved;
	}
	
}