import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
	String[] classValues; // todo 
	
	// confusion matrix values
	double[] TP, TN, FP, FN; // counts per class
	double exactMatch;
	double microPrecision, microRecall, microFMeasure;
	double macroPrecision, macroRecall, macroFMeasure;
	private Logger logger = Logger.getLogger(getClass().getName());
	
	/**
	 * Deprecated, please delete.
	 */
	public Evaluator() {
		/*
		// custom values for i2b2 obesity
		C = 4;
		classValues = new String[C]; // todo
		classValues[0] =  "Y";
		classValues[1] =  "N";
		classValues[2] =  "U";
		classValues[3] =  "Q";
		*/
		
		// custom values for PE dataset
		C = 1;
		classValues = new String[C]; // todo
		classValues[0] =  "positive";
		//classValues[1] =  "1";
		
		
	}
	
	public Evaluator(List<String> classValuesList) throws Exception {
		if (classValuesList.isEmpty())
			throw new Exception("Cannot initialize evaluator without any class values!");
		C = classValuesList.size();
		classValues = new String[C];
		classValues = classValuesList.toArray(classValues);
	}
	
	public void evaluateModel(BR cls, Instances trainingSet, Instances testSet) throws Exception {
		labelNamesInOrder = cls.getLabelNames();
		labelIndices = cls.getLabelIndices();
		K = labelNamesInOrder.size();
		TP = new double[C]; TN = new double[C]; FP = new double[C]; FN = new double[C];
		exactMatch = 0; 
		microPrecision = 0.0; microRecall = 0.0; microFMeasure = 0.0;
		macroPrecision = 0.0; macroRecall = 0.0; macroFMeasure = 0.0;
		
		int m = testSet.numInstances();
		//double[][] predictions = cls.classifyMLInstances(testSet);
		double[][] predictions = cls.classify(trainingSet, testSet);
		for (int i=0; i<m; ++i) {
			String[] prediction = convertToNominal(testSet, predictions[i]);
			String[] groundTruth = convertToNominal(testSet, getGroundTruth(testSet.instance(i)));
			recordStats(prediction, groundTruth);
		}
		
		// Micro-averaging
		double tpSum = 0, tnSum = 0, fpSum = 0, fnSum = 0;
		for (int j=0; j<C; ++j) {
			tpSum += TP[j];
			tnSum += TN[j];
			fpSum += FP[j];
			fnSum += FN[j];
		}
		if (tpSum > 0) {
			microPrecision = tpSum / (tpSum + fpSum);
			microRecall = tpSum / (tpSum + fnSum);
			microFMeasure = (2*microPrecision*microRecall) / (microPrecision + microRecall);
		}
		
		// macro averaging
		for (int j=0; j<C; ++j) 
			if (TP[j] > 0.0) {
				double mp = TP[j] / (TP[j] + FP[j]); macroPrecision += mp;
				double mr = TP[j] / (TP[j] + FN[j]); macroRecall += mr;
				macroFMeasure += (2*mp*mr) / (mp + mr);
			}
		macroPrecision /= C; macroRecall /= C; macroFMeasure /= C;
		
		StringBuilder sb = new StringBuilder("Test set metrics: \n");
		sb.append("\tExact match accuracy: ").append(exactMatch / (double) testSet.numInstances()).append("\n");
		sb.append("\tmicro-precision: ").append(microPrecision).append("\n");
		sb.append("\tmicro-recall: ").append(microRecall).append("\n");
		sb.append("\tmicro-f-measure: ").append(microFMeasure).append("\n");
		sb.append("\tmacro-precision: ").append(macroPrecision).append("\n");
		sb.append("\tmacro-recall: ").append(macroRecall).append("\n");
		sb.append("\tmacro-f-measure: ").append(macroFMeasure).append("\n");
		logger.info(sb.toString());
	}
	
	public void evaluateResults(BR cls, Instances trainingSet, Instances testSet, String resultsFile) throws Exception {
		labelNamesInOrder = cls.getLabelNames();
		labelIndices = cls.getLabelIndices();
		K = labelNamesInOrder.size();
		TP = new double[C]; TN = new double[C]; FP = new double[C]; FN = new double[C];
		exactMatch = 0; 
		microPrecision = 0.0; microRecall = 0.0; microFMeasure = 0.0;
		macroPrecision = 0.0; macroRecall = 0.0; macroFMeasure = 0.0;
		
		int m = testSet.numInstances();
		double[][] predictions1 = null;// = cls.classify(trainingSet, testSet);
		double[][] predictions = readPredictions(resultsFile, m, K, testSet, predictions1);
		
		for (int i=0; i<m; ++i) {
			String[] prediction = convertToNominal(testSet, predictions[i]);
			String[] groundTruth = convertToNominal(testSet, getGroundTruth(testSet.instance(i)));
			recordStats(prediction, groundTruth);
		}
		
		// Micro-averaging
		double tpSum = 0, tnSum = 0, fpSum = 0, fnSum = 0;
		for (int j=0; j<C; ++j) {
			tpSum += TP[j];
			tnSum += TN[j];
			fpSum += FP[j];
			fnSum += FN[j];
		}
		if (tpSum > 0) {
			microPrecision = tpSum / (tpSum + fpSum);
			microRecall = tpSum / (tpSum + fnSum);
			microFMeasure = (2*microPrecision*microRecall) / (microPrecision + microRecall);
		}
		
		// macro averaging
		for (int j=0; j<C; ++j) 
			if (TP[j] > 0.0) {
				double mp = TP[j] / (TP[j] + FP[j]); macroPrecision += mp;
				double mr = TP[j] / (TP[j] + FN[j]); macroRecall += mr;
				macroFMeasure += (2*mp*mr) / (mp + mr);
			}
		macroPrecision /= C; macroRecall /= C; macroFMeasure /= C;
		
		StringBuilder sb = new StringBuilder("Test set metrics: \n");
		sb.append("\tExact match accuracy: ").append(exactMatch / (double) testSet.numInstances()).append("\n");
		sb.append("\tmicro-precision: ").append(microPrecision).append("\n");
		sb.append("\tmicro-recall: ").append(microRecall).append("\n");
		sb.append("\tmicro-f-measure: ").append(microFMeasure).append("\n");
		sb.append("\tmacro-precision: ").append(macroPrecision).append("\n");
		sb.append("\tmacro-recall: ").append(macroRecall).append("\n");
		sb.append("\tmacro-f-measure: ").append(macroFMeasure).append("\n");
		logger.info(sb.toString());
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
	
	private void recordStats(String[] prediction, String[] groundTruth) {
		boolean allLabelsAreCorrect = true;
		
		// For each label
		for (int i=0; i<K; ++i) {
			
			// For each class
			for (int j = 0; j<C; ++j) {
				// confusion matrix updates
				if (groundTruth[i].equals(classValues[j]) && prediction[i].equals(classValues[j]))
					TP[j] += 1;
				else if (!(groundTruth[i].equals(classValues[j])) && !(prediction[i].equals(classValues[j])))
					TN[j] += 1;
				else {
					allLabelsAreCorrect = false;
					if (groundTruth[i].equals(classValues[j]))
						FN[j] += 1;
					else
						FP[j] += 1;
				}
			} 
		}
		if (allLabelsAreCorrect)
			exactMatch++;
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

		
		// PE Dataset
		String trainFileLoc = "/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/train.binary.arff";
		String testFileLoc = "/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/test.binary.arff";
		Evaluator eval = new Evaluator(Arrays.asList("positive"));
		runWithFeatureSelection(trainFileLoc, testFileLoc);
		
		//String trainFileLoc = "/Users/abhishek/Workspaces/DBMI/TextVect/data/CMCChallenge2007/train.binary.arff";
		//String testFileLoc = "/Users/abhishek/Workspaces/DBMI/TextVect/data/CMCChallenge2007/test.binary.arff";
		
		
		
		//runWithoutFeatureSelection(trainFileLoc, testFileLoc);
		
	}
	
	public static void convertPulmonaryEmb(String inputFile, String outputFile) throws Exception {
		
		/*
		 * Usage in main():
		convertPulmonaryEmb("/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/temp/train.binary.arff", 
				"/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/train.binary.arff");
		convertPulmonaryEmb("/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/temp/train.count.arff", 
				"/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/train.count.arff");
		convertPulmonaryEmb("/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/temp/train.tf.arff", 
				"/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/train.tf.arff");
		convertPulmonaryEmb("/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/temp/train.tf.idf.arff", 
				"/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/train.tf.idf.arff");
		convertPulmonaryEmb("/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/temp/train.logtf.idf.arff", 
				"/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/train.logtf.idf.arff");
		
		convertPulmonaryEmb("/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/temp/test.binary.arff", 
				"/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/test.binary.arff");
		convertPulmonaryEmb("/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/temp/test.count.arff", 
				"/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/test.count.arff");
		convertPulmonaryEmb("/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/temp/test.tf.arff", 
				"/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/test.tf.arff");
		convertPulmonaryEmb("/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/temp/test.tf.idf.arff", 
				"/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/test.tf.idf.arff");
		convertPulmonaryEmb("/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/temp/test.logtf.idf.arff", 
				"/Users/abhishek/Workspaces/DBMI/TextVect/data/PulmonaryEmbolismDataset/test.logtf.idf.arff");
		*/
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		Instances trainingSet = new Instances(reader);
		reader.close();
		
		Instances newData = new Instances(trainingSet);
		List<String> values = Arrays.asList("positive", "negative");
		
		Attribute d = newData.attribute("Labels_diseaseState");
		Attribute t = newData.attribute("Labels_temporalState");
		Attribute q = newData.attribute("Labels_qualityState");
		
		Attribute D = new Attribute("Labels_DISEASE_STATE",values);
		Attribute T = new Attribute("Labels_TEMPORAL_STATE",values);
		Attribute Catt = new Attribute("Labels_CERTAINTY_STATE",values);
		Attribute Q = new Attribute("Labels_QUALITY_STATE",values);
		
		newData.insertAttributeAt(D, newData.numAttributes());
		newData.insertAttributeAt(T, newData.numAttributes());
		newData.insertAttributeAt(Catt, newData.numAttributes());
		newData.insertAttributeAt(Q, newData.numAttributes());
		
		
		for (int i = 0; i < newData.numInstances(); i++) {
			String dval = d.value((int)newData.instance(i).value(d));
			String tval = t.value((int)newData.instance(i).value(t));
			String qval = q.value((int)newData.instance(i).value(q));
			
			if (dval.equalsIgnoreCase("Prob. Pos") || dval.equalsIgnoreCase("Def. Pos"))
				newData.instance(i).setValue(newData.numAttributes() - 4, 0);
			else
				newData.instance(i).setValue(newData.numAttributes() - 4, 1);
			
			if (dval.equalsIgnoreCase("Prob. Pos") || dval.equalsIgnoreCase("Prob. Neg"))
				newData.instance(i).setValue(newData.numAttributes() - 2, 0);
			else
				newData.instance(i).setValue(newData.numAttributes() - 2, 1);
			
			if (tval.equalsIgnoreCase("New") || tval.equalsIgnoreCase("Mixed"))
				newData.instance(i).setValue(newData.numAttributes() - 3, 0);
			else
				newData.instance(i).setValue(newData.numAttributes() - 3, 1);
			
			if (qval.equalsIgnoreCase("Limited") || qval.equalsIgnoreCase("Non-diagnostic"))
				newData.instance(i).setValue(newData.numAttributes() - 1, 0);
			else
				newData.instance(i).setValue(newData.numAttributes() - 1, 1);
			
			for (int j=0; j < newData.numAttributes(); ++j)
				if (newData.instance(i).value(j) == -1)
					System.out.print("-1 found at i=" + i + " and j=" + j);
		}
		
		Remove remove = new Remove();
		remove.setAttributeIndices("1," + (q.index() + 1) + "," + (t.index() + 1) + "," + (d.index() + 1));
		remove.setInvertSelection(false);
		remove.setInputFormat(newData);
		newData =  Filter.useFilter(newData, remove);
		
		ArffSaver arffSaverInstance = new ArffSaver();
	  	arffSaverInstance.setInstances(newData);
	  	arffSaverInstance.setFile(new File(outputFile));
	  	arffSaverInstance.writeBatch();
	}
	public static void runWithoutFeatureSelection(String trainFileLoc, String testFileLoc) throws IOException, Exception {
		Evaluator eval = new Evaluator();
		
		// train
		BufferedReader reader = new BufferedReader(new FileReader(trainFileLoc));
		Instances trainingSet = Evaluator.removeFirstAttibute(new Instances(reader));
		reader.close();
		// Get rid of first column
		
		BR cls = new BR();
		cls.buildClassifier(trainingSet);
		
		// test
		reader = new BufferedReader(new FileReader(testFileLoc));
		Instances testSet = Evaluator.removeFirstAttibute(new Instances(reader));
		reader.close();
		
		eval.evaluateModel(cls, trainingSet, testSet);
	}
	
	public static void runWithFeatureSelection(String trainFileLoc, String testFileLoc) throws IOException, Exception {
		Evaluator eval = new Evaluator();
		
		// train
		BufferedReader reader = new BufferedReader(new FileReader(trainFileLoc));
		//Instances trainingSet = Evaluator.removeFirstAttibute(new Instances(reader));
		Instances trainingSet = new Instances(reader);
		reader.close();
		
		BR cls = new BR();
		cls.setFeatureSelection(true);
		cls.buildClassifier(trainingSet);
		
		// test
		reader = new BufferedReader(new FileReader(testFileLoc));
		//Instances testSet = Evaluator.removeFirstAttibute(new Instances(reader));
		Instances testSet = new Instances(reader);
		reader.close();
		
		//eval.evaluateModel(cls, trainingSet, testSet);
		eval.evaluateResults(cls, trainingSet, testSet, "/Users/abhishek/Workspaces/MLL/data/PulmonaryEmbolism/predictions.txt");
		
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
	
	private static void saveReducedDataset(Instances bigDataset, Map<String, Integer> selectedAttributes, String filename) throws Exception {
		StringBuilder sb = new StringBuilder();
		String comma = "";
		List<Integer> indexesToKeep = new ArrayList<>();
		for (String a : selectedAttributes.keySet())
			indexesToKeep.add(bigDataset.attribute(a).index());
		Collections.sort(indexesToKeep);
		for (int i : indexesToKeep) {
			sb.append(comma).append(i + 1);
			comma = ",";
		}
		
		// Remove
		Remove remove = new Remove();
		remove.setAttributeIndices(sb.toString());
		remove.setInvertSelection(true);
		remove.setInputFormat(bigDataset);
		Instances smallDataset = Filter.useFilter(bigDataset, remove);
		
		// save
		System.out.println("Saving reduced dataset with " + smallDataset.numAttributes() + 
				" attributes and " + smallDataset.numInstances() + " instances.");
		ArffSaver saver = new ArffSaver();
		saver.setInstances(smallDataset);
		saver.setFile(new File(filename));
		saver.writeBatch();
	}
	
	private double[][] readPredictions(String fileName, int m, int K, Instances testSet, double[][] predictions1) throws NumberFormatException, IOException {
			BufferedReader br = new BufferedReader( new FileReader(fileName));
			String strLine = null;
			StringTokenizer st = null;
			int lineNumber = 0, tokenNumber = 0;
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
