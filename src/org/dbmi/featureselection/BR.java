package org.dbmi.featureselection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.log4j.Logger;

import weka.attributeSelection.ASSearch;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LibLINEAR;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;


public class BR  {

	int K; 						// number of labels 
	List<String> labelNames;
	Map<String, Integer> labelIndex;
	List<Instances> datasets;	// one dataset per label
	List<Classifier> baseClassifiers;
	List<Remove> baseLabelFilters;
	
	// Feature selection decision
	private boolean doFeatureSelection = false;
	private boolean useGreedyFeatureSelection = false;
	List<AttributeSelection> baseFilters;
	Map<String, Integer> selectedLabels = null;
	float avgNumSelectedLabels = 0;
	
	
	private Logger logger = Logger.getLogger(getClass().getName());
	
	public void buildClassifier(Instances dataset_) throws Exception {
		// Determine number of labels, K; and the label names
		getLabelInfo(dataset_);
	}

	/**
	 * Determines the label count and index of each label in the dataset
	 * @param dataset training set
	 */
	private void getLabelInfo(Instances dataset) {
		int n = dataset.numAttributes();
		int k = 0;
		labelNames = new ArrayList<String>();
		labelIndex = new HashMap<>();
		
		for (int i = 0; i < n; ++i) {
			String attributeName = dataset.attribute(i).name();
			if (attributeName.startsWith("Label")) {
				labelNames.add(attributeName);
				labelIndex.put(attributeName, i);
				++k;
			}
		}
		K = k;
	}
	
	/**
	 * Use cross validation to tune regularization parameter and return this tuned value.
	 * @param dataset training set
	 * @return Best regularization weight.
	 * @throws Exception 
	 */
	private double gridSearchC(Instances dataset) throws Exception {
		int best_c_exp = -20; double bestScore = -10000.0;
		logger.debug("... Cross validating base model...");
		for (int c_exp = -10; c_exp < 10; c_exp += 2) {
			double C = Math.pow(10, c_exp);
			//Logistic LR = new Logistic(); LR.setDebug(false); LR.setMaxIts(-1); LR.setUseConjugateGradientDescent(true); LR.setRidge(C);
			LibLINEAR LibL = createLRClassifier(); LibL.setCost(C);
			Evaluation eval = new Evaluation(dataset);
			eval.crossValidateModel(LibL, dataset, 10, new Random()); // 10-fold CV
			
			double score = eval.correct(); // Select based on accuracy
			logger.debug("...... c_exp = " + c_exp + "; Score = " + score);
			if (score > bestScore) {
				bestScore = score;
				best_c_exp = c_exp;
			}
		}
		return Math.pow(10, best_c_exp);
	}
	

	/**
	 * Classify all labels for a given instance and return y_hat
	 */
	public double[] classifyMLInstance(int instanceNo, Instances dataset) throws Exception {
		double[] predictedValues = new double[K];
		for (int i=0; i<K; ++i) {
			double prediction = 0.0;
			// calculate prediction
			dataset.setClassIndex(labelIndex.get(labelNames.get(i)));
			prediction = baseClassifiers.get(i).classifyInstance(dataset.instance(instanceNo));
			predictedValues[i] = prediction;
		}
		return predictedValues;
	}
	
	public double[][] classifyMLInstances(Instances testSet) throws Exception {
		int m = testSet.numInstances();
		double[][] predictions = new double[m][K];
		
		// for each label
		for(int j=0; j<K; ++j) {
			testSet.setClassIndex(labelIndex.get(labelNames.get(j)));
			
			// remove other labels
			Instances baseDataset = Filter.useFilter(testSet, baseLabelFilters.get(j));

			//remove discarded features
			if(doFeatureSelection) {
				baseDataset = Filter.useFilter(testSet, baseFilters.get(j));
			}
			
			// for each instance
			for(int i=0; i<m; ++i)
				predictions[i][j] = baseClassifiers.get(j).classifyInstance(baseDataset.instance(i));
		}
		return predictions;
	}

	public double[][] classify(Instances trainingSet, Instances testSet) throws Exception {
		// Determine number of labels, K
		getLabelInfo(trainingSet);
		int m = testSet.numInstances();
		double[][] predictions = new double[m][K];
		selectedLabels = new HashMap<>();
		avgNumSelectedLabels = 0;
		
		// for each label
		for(int j=0; j<K; ++j) {
			logger.debug("Processing for label " + j);
			testSet.setClassIndex(labelIndex.get(labelNames.get(j)));
			trainingSet.setClassIndex(labelIndex.get(labelNames.get(j)));
			
			// remove other labels
			Remove remove = getRemoveOtherLabels(j, trainingSet);
			Instances baseTrainingDataset = Filter.useFilter(trainingSet, remove);
			Instances baseTestDataset = Filter.useFilter(testSet, remove);

			//remove discarded features
			if(doFeatureSelection) {
				AttributeSelection as = createFeatureSelectionFilter(baseTrainingDataset); 
				baseTrainingDataset = Filter.useFilter(baseTrainingDataset, as);
				baseTestDataset = Filter.useFilter(baseTestDataset, as);
				logger.info("... After feature selection, num attributes = " + baseTrainingDataset.numAttributes());
				incrementFeatureCount(selectedLabels, baseTrainingDataset);
			}
			avgNumSelectedLabels += baseTrainingDataset.numAttributes();

			
			// Build classifier
			double bestC = gridSearchC(baseTrainingDataset);
			//double bestC = 100;
			logger.debug("... best regularization parameter is " + bestC );
			LibLINEAR LR = createLRClassifier();
			LR.setCost(bestC);
			LR.buildClassifier(baseTrainingDataset);
			
			// for each instance
			logger.debug("... predicting");
			for(int i=0; i<m; ++i)
				predictions[i][j] = LR.classifyInstance(baseTestDataset.instance(i));
			System.gc();
		}
		avgNumSelectedLabels /= K;
		StringBuilder sb = new StringBuilder("Number of times each selected feature was selected:\n");
		for (Entry<String, Integer> entry : selectedLabels.entrySet())
			sb.append(entry.getKey()).append("\t: ").append(entry.getValue()).append("\n");
		
		logger.debug(sb.toString());
		return predictions;
	}
	
	public List<String> getLabelNames() {
		return labelNames;
	}
	
	public Map<String, Integer> getLabelIndices() {
		return labelIndex;
	}

	
	private LibLINEAR createLRClassifier() {
		LibLINEAR LibL = new LibLINEAR();
		LibL.setBias(1.0);  LibL.setSVMType(new SelectedTag(0, LibLINEAR.TAGS_SVMTYPE));
		LibL.setEps(0.01); LibL.setNormalize(false); LibL.setProbabilityEstimates(false); LibL.setDebug(false);
		return LibL;
	}
	
	
	private AttributeSelection createFeatureSelectionFilter(Instances dataset) throws Exception {
		AttributeSelection as = new AttributeSelection();
		CfsSubsetEval eval = new CfsSubsetEval();
		as.setEvaluator(eval);
		
		// Select the feature selection method
		ASSearch searchMethod = null;
		if (useGreedyFeatureSelection) {
			GreedyStepwise search = new GreedyStepwise();
			search.setSearchBackwards(false);
			searchMethod = search;
			logger.debug("Using greedy feature selection method");
		} else {
			BestFirst search = new BestFirst();
			search.setDirection(new SelectedTag(1, BestFirst.TAGS_SELECTION));
			searchMethod = search;
			logger.debug("Using bestfirst feature selection method");
		}
		as.setSearch(searchMethod);
		as.setInputFormat(dataset);
		return as;
	}
	
	public void setFeatureSelection(boolean status, boolean useGreedyMethod) {
		doFeatureSelection = status;
		useGreedyFeatureSelection = useGreedyMethod;
	}
	
	private Remove getRemoveOtherLabels(int labelNo, Instances dataset) throws Exception {
		StringBuilder indicesToRemove = new StringBuilder();
		String comma = "";
		for (String labelName : labelNames) 
			if (labelIndex.get(labelName) != labelIndex.get(labelNames.get(labelNo))) {
				indicesToRemove.append(comma).append(labelIndex.get(labelName) + 1); // indices to remove is 1 based
				comma = ",";
			}
		Remove remove = new Remove();
		remove.setAttributeIndices(indicesToRemove.toString());
		remove.setInvertSelection(false);
		remove.setInputFormat(dataset);
		logger.info("... Remove filter for label " + labelNames.get(labelNo) + "# " + (labelNo) + 
				" (attribute # " + dataset.classIndex() + 
				") selected to remove: " + indicesToRemove.toString());
		return remove;
	}
	
	private void incrementFeatureCount(Map<String, Integer> map, Instances dataset) throws Exception {
		int J = dataset.numAttributes();
		for (int j=0; j<J; ++j) {
			String a = dataset.attribute(j).name();
			int prev = 0;
			if (map.containsKey(a))
				prev = map.get(a);
			map.put(a, prev+1);
		}
		
		// Print the best 4 features for the current label
		Ranker ranker = new Ranker();
		InfoGainAttributeEval ig = new InfoGainAttributeEval();
		ig.buildEvaluator(dataset);
		int[] attributeRanks = ranker.search(ig, dataset);
		StringBuilder sb = new StringBuilder("Top 5 Features for the label " + 
				dataset.attribute(dataset.classIndex()).name() + "\n\t");
		for (int i = 0; i < Math.min(3, attributeRanks.length); ++i)
			sb.append(dataset.attribute(attributeRanks[i]).name()
					.replaceAll("Entities_", "UMLS CUI (")
					.replaceAll("Words_", "Word token `")
					.replaceAll("CanonicalWords_", "Canonical forms of `") + "  ");
		logger.debug(sb.toString());
	}
	
	public Map<String, Integer> getSelectedLabels() {
		return selectedLabels;
	}
}
