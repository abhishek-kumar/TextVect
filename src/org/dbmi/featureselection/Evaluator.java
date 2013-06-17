package org.dbmi.featureselection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.Classifier;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveRange;
import weka.filters.unsupervised.instance.SparseToNonSparse;


public class Evaluator {
	int K, C;
	List<String> labelNamesInOrder;
	Map<String, Integer> labelIndices;
	String[] classValues; // initialized in constructor 
	
	// confusion matrix values
	double[] TP, TN, FP, FN; // counts per class
	double exactMatch;
	double[] exactMatch_samples;
	double microPrecision, microRecall, microFMeasure;
	double[] microPrecision_samples, microRecall_samples, microFMeasure_samples;
	double macroPrecision, macroRecall, macroFMeasure;
	double[] macroPrecision_samples, macroRecall_samples, macroFMeasure_samples;
	private Logger logger = Logger.getLogger(getClass().getName());
	double m; // number of test documents
	double avgSelectedFeatures = 0;
	
	public Evaluator(List<String> classValuesList) throws Exception {
		if (classValuesList.isEmpty())
			throw new Exception("Cannot initialize evaluator without any class values!");
		C = classValuesList.size();
		classValues = new String[C];
		classValues = classValuesList.toArray(classValues);
	}
	
	public void evaluateModel(BR cls, Instances testSet, double[][] predictions) throws Exception {
		// Initialize label info and statistics
		labelNamesInOrder = cls.getLabelNames();
		labelIndices = cls.getLabelIndices();
		K = labelNamesInOrder.size();
		TP = new double[C]; TN = new double[C]; FP = new double[C]; FN = new double[C];
		exactMatch = 0; 
		m = predictions.length;

		microPrecision = 0.0; microRecall = 0.0; microFMeasure = 0.0;
		macroPrecision = 0.0; macroRecall = 0.0; macroFMeasure = 0.0;
		microPrecision_samples = new double[(int) m]; microRecall_samples = new double[(int) m]; microFMeasure_samples = new double[(int) m];
		macroPrecision_samples = new double[(int) m]; macroRecall_samples = new double[(int) m]; macroFMeasure_samples = new double[(int) m];
		exactMatch_samples  = new double[(int) m];
		
		// Predict and record statistics
		//double[][] predictions = cls.classify(trainingSet, testSet);
		
		for (int i=0; i<m; ++i) {
			String[] prediction = convertToNominal(testSet, predictions[i]);
			String[] groundTruth = convertToNominal(testSet, getGroundTruth(testSet.instance(i)));
			recordStats(prediction, groundTruth, i);
		}
		
		// Calculate metrics
		calculateMetrics(TP, TN, FP, FN); // only updates micro / macro metrics
		exactMatch =  exactMatch / m;
		avgSelectedFeatures = cls.avgNumSelectedLabels;

		printResults();
	}
	
	void printResults() {
		
		double microPrecision_stdev=0,microRecall_stdev=0 , microFMeasure_stdev=0, 
		macroPrecision_stdev=0, macroRecall_stdev=0, macroFMeasure_stdev=0, exactMatch_stdev=0;
		
		for (int i=0; i<m; ++i) {
			microPrecision_stdev += Math.pow(microPrecision_samples[i] - microPrecision, 2);
			microRecall_stdev += Math.pow(microRecall_samples[i] - microRecall, 2);
			microFMeasure_stdev += Math.pow(microFMeasure_samples[i] - microFMeasure, 2);
			
			macroPrecision_stdev += Math.pow(macroPrecision_samples[i] - macroPrecision, 2);
			macroRecall_stdev += Math.pow(macroRecall_samples[i] - macroRecall, 2);
			macroFMeasure_stdev += Math.pow(macroFMeasure_samples[i] - macroFMeasure, 2);
			
			exactMatch_stdev += Math.pow(exactMatch_samples[i] - exactMatch, 2);
		}
		
		microPrecision_stdev = Math.sqrt(microPrecision_stdev/m);
		microRecall_stdev = Math.sqrt(microRecall_stdev/m);
		microFMeasure_stdev = Math.sqrt(microFMeasure_stdev/m);
		macroPrecision_stdev = Math.sqrt(macroPrecision_stdev/m);
		macroRecall_stdev = Math.sqrt(macroRecall_stdev/m);
		macroFMeasure_stdev = Math.sqrt(macroFMeasure_stdev/m);
		exactMatch_stdev = Math.sqrt(exactMatch_stdev / m);
		
		StringBuilder sb = new StringBuilder("Test set metrics: \n");
		DecimalFormat df = new DecimalFormat("##.####");
		df.setRoundingMode(RoundingMode.HALF_UP);
		sb.append("\tExact match accuracy: ").append(df.format(exactMatch)).append("\\pm ").append(df.format(convertStdevToCI(exactMatch_stdev))).append("\n");
		sb.append("\tmicro-precision: ").append(df.format(microPrecision)).append("\\pm ").append(df.format(convertStdevToCI(microPrecision_stdev))).append("\n");
		sb.append("\tmicro-recall: ").append(df.format(microRecall)).append("\\pm ").append(df.format(convertStdevToCI(microRecall_stdev))).append("\n");
		sb.append("\tmicro-f-measure: ").append(df.format(microFMeasure)).append("\\pm ").append(df.format(convertStdevToCI(microFMeasure_stdev))).append("\n");
		sb.append("\tmacro-precision: ").append(df.format(macroPrecision)).append("\\pm ").append(df.format(convertStdevToCI(macroPrecision_stdev))).append("\n");
		sb.append("\tmacro-recall: ").append(df.format(macroRecall)).append("\\pm ").append(df.format(convertStdevToCI(macroRecall_stdev))).append("\n");
		sb.append("\tmacro-f-measure: ").append(df.format(macroFMeasure)).append("\\pm ").append(df.format(convertStdevToCI(macroFMeasure_stdev))).append("\n");
		sb.append("Avg number of features used: ").append(df.format(avgSelectedFeatures));
		logger.info(sb.toString());
	}
	/**
	 * Calculates and sets micro-averaged and macro averaged metrics based on confusion matrix supplied.
	 * Prerequisites: C must be set correctly, and each cell of the confusion matrix should be C dimensional.
	 */
	private void calculateMetrics(double[] tp, double[] tn, double fp[], double fn[]) {
		// Micro-averaging
		double tpSum = 0, tnSum = 0, fpSum = 0, fnSum = 0;
		for (int j=0; j<C; ++j) {
			tpSum += tp[j];
			tnSum += tn[j];
			fpSum += fp[j];
			fnSum += fn[j];
		}
		if (tpSum > 0) {
			microPrecision = tpSum / (tpSum + fpSum);
			microRecall = tpSum / (tpSum + fnSum);
			microFMeasure = (2*microPrecision*microRecall) / (microPrecision + microRecall);
		}
		
		// macro averaging
		macroPrecision = 0.0; macroRecall = 0.0; macroFMeasure = 0.0;
		for (int j=0; j<C; ++j) 
			if (tp[j] > 0.0) {
				double mp = tp[j] / (tp[j] + fp[j]); macroPrecision += mp;
				double mr = tp[j] / (tp[j] + fn[j]); macroRecall += mr;
				macroFMeasure += (2*mp*mr) / (mp + mr);
			}
		macroPrecision /= C; macroRecall /= C; macroFMeasure /= C;
	}
	
	/***
	 * Given a standard deviation measure, convert it into a 95% confidence interval value.
	 * Make sure that m is set correctly, and is in the range 100 <= m <= 5000.
	 * @param input Standard deviation in the value being measured.
	 * @return
	 */
	private double convertStdevToCI(double input) {
		// (Reasonably) accurate t table values for range 100 - 5000 (95% test, two tailed)
		// This is a linear approximation from v = 100 to \infty
		double t_5000 = 1.960;
		double t_100  = 1.984;
		double t_value = t_5000 + ((5000 - m + 1) / 4900) * (t_100 - t_5000);
		return (input / Math.sqrt(m))*t_value;
	}
	
	
	private double[] getGroundTruth(Instance inst) {
		double[] retVal = new double[K];
		int i = 0;
		for (String labelName : labelNamesInOrder) {
			int idx = labelIndices.get(labelName);
			retVal[i] = inst.value(idx);
			++i;
		}
		return retVal;
	}
	
	private void recordStats(String[] prediction, String[] groundTruth, int documentNo) {
		boolean allLabelsAreCorrect = true;
		double[] tp = new double[C]; 
		double[] tn = new double[C];
		double[] fp = new double[C];
		double[] fn = new double[C];
		
		// For each label
		for (int i=0; i<K; ++i) {
			
			// For each class
			for (int j = 0; j<C; ++j) {
				// confusion matrix updates
				if (groundTruth[i].equals(classValues[j]) && prediction[i].equals(classValues[j]))
					tp[j] += 1;
				else if (!(groundTruth[i].equals(classValues[j])) && !(prediction[i].equals(classValues[j])))
					tn[j] += 1;
				else {
					allLabelsAreCorrect = false;
					if (groundTruth[i].equals(classValues[j]))
						fn[j] += 1;
					else
						fp[j] += 1;
				}
			} 
		}
		
		// Accuracy
		if (allLabelsAreCorrect) {
			exactMatch++;
			exactMatch_samples[documentNo] = 1;
		}
		
		// Std deviations
		calculateMetrics(tp, tn, fp, fn);
		microPrecision_samples[documentNo] = microPrecision;
		microRecall_samples[documentNo] = microRecall;
		microFMeasure_samples[documentNo] = microFMeasure;
		
		macroPrecision_samples[documentNo] = macroPrecision;
		macroRecall_samples[documentNo] = macroRecall;
		macroFMeasure_samples[documentNo] = macroFMeasure;
		
		
		
		// Record confusion matrix
		for (int j=0; j<C; ++j)
		{
			TP[j] += tp[j];
			TN[j] += tn[j];
			FP[j] += fp[j];
			FN[j] += fn[j];
		}
	}
	
	String[] convertToNominal(Instances dataset, double[] prediction) {
		String[] retVal = new String[K];
		for (int i=0; i<K; ++i) {
			dataset.setClassIndex(labelIndices.get(labelNamesInOrder.get(i)));
			retVal[i] = dataset.classAttribute().value((int)prediction[i]);
		}
		return retVal;
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();

		/*
		// PE Dataset
		String trainFileLoc = "/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/train.binary.arff";
		String testFileLoc = "/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/test.binary.arff";
		Evaluator eval = new Evaluator(Arrays.asList("positive"));
		getResults(trainFileLoc, testFileLoc, eval, true, true);
		
		
		// CMC Dataset
		String trainFileLoc = "/Users/abhishek/Workspaces/DBMI/TextVect/data/CMCChallenge2007/train.logtf.idf.arff";
		String testFileLoc = "/Users/abhishek/Workspaces/DBMI/TextVect/data/CMCChallenge2007/test.logtf.idf.arff";
		Evaluator eval = new Evaluator(Arrays.asList("1"));
		getResults(trainFileLoc, testFileLoc, eval, true, false);
		*/
		
		
		//i2b2 Obesity dataset
		String trainFileLoc = "/Users/abhishek/Workspaces/DBMI/TextVect/data/i2b2ObesityChallenge2008/train.binary.arff";
		String testFileLoc = "/Users/abhishek/Workspaces/DBMI/TextVect/data/i2b2ObesityChallenge2008/test.binary.arff";
		Evaluator eval = new Evaluator(Arrays.asList("Y","N","U","Q"));
		getResults(trainFileLoc, testFileLoc, eval, true, true);
		
		//String trainFileLoc = "/Users/abhishek/Workspaces/DBMI/TextVect/data/CMCChallenge2007/train.binary.arff";
		//String testFileLoc = "/Users/abhishek/Workspaces/DBMI/TextVect/data/CMCChallenge2007/test.binary.arff";
		
		
		
		//runWithoutFeatureSelection(trainFileLoc, testFileLoc);
		
	}
	
	public static void getResults(String trainFileLoc, String testFileLoc, 
			Evaluator eval, boolean useFeatureSelection, boolean useGreedyMethod) throws IOException, Exception {
		
		// read datasets
		BufferedReader trainreader = new BufferedReader(new FileReader(trainFileLoc));
		BufferedReader testreader = new BufferedReader(new FileReader(testFileLoc));
		Instances trainingSet = new Instances(trainreader);
		Instances testSet = new Instances(testreader);
		trainreader.close();
		testreader.close();
		
		// If first attribute is a string, it is likely the document name. Remove it.
		if (trainingSet.attribute(0).isString()) {
			Logger.getLogger(Evaluator.class).debug("Removing first attribute from dataset because it is a string type.");
			trainingSet = Evaluator.removeFirstAttibute(trainingSet);
			testSet = Evaluator.removeFirstAttibute(testSet);
		}
		
		
		BR cls = new BR();
		cls.setFeatureSelection(useFeatureSelection, useGreedyMethod);
		cls.buildClassifier(trainingSet);
		
		
		eval.evaluateModel(cls, testSet, cls.classify(trainingSet, testSet));
		//eval.evaluateModel(cls, testSet, readPredictions("/Users/abhishek/Workspaces/MLL/data/PulmonaryEmbolism/predictions.txt", cls.K, testSet, null));
		//eval.evaluateModel(cls, testSet, readPredictions("/Users/abhishek/Workspaces/MLL/data/CMCChallenge2007/predictions.txt", cls.K, testSet, null));
		// temp:
		//saveReducedDataset(trainingSet, cls.getSelectedLabels(), "/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/train.binary.reduced.arff");
		//saveReducedDataset(testSet, cls.getSelectedLabels(), "/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/test.binary.reduced.arff");
	}
	
	
	
	
	
	public static Instances removeFirstAttibute(Instances inst) throws Exception {
		
		Remove remove = new Remove();
		remove.setAttributeIndices("first");
		remove.setInvertSelection(false);
		remove.setInputFormat(inst);
		Instances newInst =  Filter.useFilter(inst, remove);
		Logger.getLogger(Evaluator.class.getName()).info("num attributes after removing first = " + newInst.numAttributes());
		return newInst;
	}
	
	private static double[][] readPredictions(String fileName, int K, Instances testSet, double[][] predictions1) throws NumberFormatException, IOException {
			BufferedReader br = new BufferedReader( new FileReader(fileName));
			String strLine = null;
			StringTokenizer st = null;
			int lineNumber = 0, tokenNumber = 0;
			int m = testSet.numInstances();
			double[][] pred = new double[m][K];
			
			while( (strLine = br.readLine()) != null)
			{
				//break comma separated line using ","
				st = new StringTokenizer(strLine, ",");
				while(st.hasMoreTokens())
				{
					
					pred[lineNumber][tokenNumber] = 1.0 - Double.parseDouble(st.nextToken()); // <-- if a prediction of 1 means positive, and positive's index is 0 in the list of class values {positive, negative} in the dataset
					
					/*
					double Y = testSet.attribute(labelIndices.get(labelNamesInOrder.get(tokenNumber))).indexOfValue("Y");
					if (1.0 == Double.parseDouble(st.nextToken()))
						pred[lineNumber][tokenNumber] = Y;
					else
						pred[lineNumber][tokenNumber] = predictions1[lineNumber][tokenNumber];;
						*/
					tokenNumber++;
				}
 
				//reset 
				lineNumber++;
				tokenNumber = 0;
			}
		return pred;
	}

}
