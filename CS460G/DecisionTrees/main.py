import math
import os
import pandas
import numpy
import matplotlib.pyplot

sourceData = pandas.read_csv(os.getcwd() + '/data/pokemonStats.csv').iloc[0]

# Used for testing against results
legendaryActualData = pandas.read_csv(os.getcwd() + '/data/pokemonLegendary.csv')

# Synthetic data sets
syntheticDatasets: list[pandas.DataFrame] = [
    pandas.read_csv('data/synthetic-1.csv', names=['a', 'b', 'c']),
    pandas.read_csv('data/synthetic-2.csv', names=['a', 'b', 'c']),
    pandas.read_csv('data/synthetic-3.csv', names=['a', 'b', 'c']),
    pandas.read_csv('data/synthetic-4.csv', names=['a', 'b', 'c'])
]

maxTreeDepth = 3


class DecisionTree:
    class Node:
        # If level == 0, then root node. If level == maxDepth, cannot split any deeper.
        level = 0

        def __str__(self):
            returnedString: str = ""

            if self.level > 0:
                for i in range(self.level):
                    returnedString += '\t'

            returnedString += 'Feature: ' + str(self.feature) + '\t Interval: ' + str(self.prediction) + '\n'

            for child in self.children:
                returnedString += str(child)

            return returnedString

        ### Functions for training

        def entropy(self, classLabelSubset: pandas.Series):
            # Sum of - (# of i examples / total examples) * log2(# of i examples / total examples)
            # if (# of i examples / total examples)tree is 50/50, then skip calculation and default to 1
            # Calculate frequency of all values
            positiveCount = 0
            negativeCount = 0

            for index, cell in enumerate(classLabelSubset):
                if cell == 1:
                    positiveCount += 1
                if cell == 0:
                    negativeCount += 1

            entropyValue = int(positiveCount == negativeCount)

            # Skip entropy calculation if data is homogenous
            if (positiveCount != 0 and negativeCount != 0) and entropyValue != 1:
                positiveFrequency = positiveCount / len(classLabelSubset)
                positiveLog = math.log2(positiveFrequency)
                negativeFrequency = negativeCount / len(classLabelSubset)
                negativeLog = math.log2(negativeFrequency)
                positiveEntropy = (-positiveFrequency * positiveLog)
                negativeEntropy = (-negativeFrequency * negativeLog)

                entropyValue = positiveEntropy + negativeEntropy

            return entropyValue

        # end entropy

        # Given a target column and a class label column to split on, run information gain
        # Note that the target feature column has the class label included to process the category more easily
        def informationGain(self, data: pandas.DataFrame, targetFeature: str, classLabel: str):
            # Calculate parent entropy
            classLabelColumn = data[classLabel]
            parentEntropy = self.entropy(data[classLabel])
            # print("Parent entropy: ", parentEntropy)

            # Store the frequency of each bin for the target feature
            binDataCount = data[targetFeature].value_counts()

            # Calculate the wighted average entropy of children
            childrenEntropy = 0

            # For each bin (or "child"), calculate the entropy and determine the average
            for interval, count in binDataCount.items():
                # Get data within given intervals
                subset = data.loc[data[targetFeature] == interval]
                subsetEntropy = self.entropy(subset[classLabel])

                childProbability = count / len(data)
                childEntropy = childProbability * subsetEntropy
                childrenEntropy += childEntropy

            # print("Children entropy: ", childrenEntropy)
            #  Finish information gain calculation
            return parentEntropy - childrenEntropy

        # end informationGain

        # Run recursive ID3 algorithm on constructing
        def __init__(self, prediction, level: int, data: pandas.DataFrame, classLabels: list, dataSetFeatures: list):
            # Create instance level child set
            self.children = set()
            self.prediction = prediction
            self.level = level

            if len(dataSetFeatures) > 0 and level < maxTreeDepth:

                # Store information gains of each feature in a dictionary. This is primarily for debugging purposes.
                informationGains = dict()
                greatestGainFeature = dataSetFeatures[0]

                # Determine feature with most discriminatory power
                for feature in dataSetFeatures:
                    informationGains[feature] = self.informationGain(data, feature, classLabels[0])

                    if informationGains[greatestGainFeature] < informationGains[feature]:
                        greatestGainFeature = feature

                # Debugging printing
                # print(informationGains)
                # print("Feature with greatest information gain: ", greatestGainFeature)

                self.feature = greatestGainFeature
                featureIntervals = data[greatestGainFeature].unique()

                for interval in featureIntervals:
                    featureSubset = data.loc[data[greatestGainFeature] == interval]

                    featuresForChild = list()
                    if len(featureSubset) == 0:
                        # Child feature
                        featuresForChild.append(data[classLabels[0]].value_counts().idxmax())
                        print(featuresForChild)
                    else:
                        for feature in dataSetFeatures:
                            if feature != self.feature:
                                featuresForChild.append(feature)

                    self.children.add(DecisionTree.Node(interval, level + 1, data, classLabels, featuresForChild))
            else:
                self.feature = data[classLabels[0]].value_counts().idxmax()
        # end Node

    root: Node
    sourceDataSet: pandas.DataFrame

    def __init__(self, data: pandas.DataFrame, classLabels: list, dataSetFeatures: list):
        self.sourceDataSet = data
        self.root = self.Node('root', 0, data, classLabels, dataSetFeatures)

    def print(self):
        print(self.root)

    # Returns the % accuracy of tree based on test data
    def test(self, testData: pandas.DataFrame, classLabel: str):
        correctGuessCount = 0

        for index, data in testData.iterrows():
            if data[classLabel] == self.predictFeature(data, self.root):
                correctGuessCount += 1

        return correctGuessCount / len(testData)

    # Recursively predict a node's feature based on a given dataset
    def predictFeature(self, data, node: Node):
        prediction = ''
        if not node.children:
            prediction = node.feature

        for child in node.children:
            if data[node.feature] in child.prediction or data[node.feature] == child.prediction:
                prediction = self.predictFeature(data, child)

        return prediction

    # End predictFeature

    def plot(self, graphTitle: str, position: int, srcData: pandas.DataFrame, classLabel: str, xFeature: str,
             yFeature: str):
        ax: matplotlib.pyplot.Axes = matplotlib.pyplot.subplot(position)

        # Plot the decision boundary
        xMin = srcData[xFeature].min() - 1
        xMax = srcData[xFeature].max() + 1
        yMin = srcData[yFeature].min() - 1
        yMax = srcData[yFeature].max() + 1

        x = srcData[xFeature]
        y = srcData[yFeature]

        step = 0.1

        xMesh, yMesh = numpy.meshgrid(numpy.arange(xMin, xMax, step), numpy.arange(yMin, yMax, step))

        meshData = pandas.DataFrame(numpy.c_[xMesh.ravel(), yMesh.ravel()], columns=[xFeature, yFeature])

        # Establish plotted data shape
        zShape = []
        for index, data in meshData.iterrows():
            currentPrediction = self.predictFeature(data, self.root)

            if currentPrediction:
                zShape.append(currentPrediction)
            else:
                zShape.append(0)

        zShape = numpy.reshape(zShape, xMesh.shape)

        ax.contour(xMesh, yMesh, zShape)

        matplotlib.pyplot.xlabel(xFeature, fontsize=20)
        matplotlib.pyplot.ylabel(yFeature, fontsize=20)

        classLabelFalsyData = srcData.loc[srcData[classLabel] == 0]
        classLabelTruthyData = srcData.loc[srcData[classLabel] == 1]
        ax.scatter(classLabelFalsyData[xFeature], classLabelFalsyData[yFeature], label='0', edgecolor='black', c='r')
        ax.scatter(classLabelTruthyData[xFeature], classLabelTruthyData[yFeature], label='1', edgecolor='black', c='b')

        ax.axis('tight')
        ax.set_title(graphTitle, fontsize=25)
        ax.legend(fontsize=10)

    # end plot


# end DecisionTree

# Preprocess data
def discretize(column: pandas.Series):
    # Array of min and max values for thresholds
    thresholdRange = [min(column), max(column)]

    # Default to 3 bins. Downsize depending on datatype.
    if thresholdRange[1] - thresholdRange[0] == 1:
        binCount = 2
    else:
        binCount = 3

    return pandas.qcut(column, binCount)


# end discretize

# Run decision trees for each iteration of synthetic data
figure: matplotlib.pyplot.Figure = matplotlib.pyplot.figure(figsize=(30, 15))
for currentDataset in range(len(syntheticDatasets)):
    discretizedData = syntheticDatasets[currentDataset].copy()

    discretizedData['a'] = discretize(syntheticDatasets[currentDataset]['a'])
    discretizedData['b'] = discretize(syntheticDatasets[currentDataset]['b'])

    syntheticTree = DecisionTree(discretizedData, ['c'], ['a', 'b'])

    # Print tree for debugging purposes
    # syntheticTree.print()

    treeAccuracy = syntheticTree.test(syntheticDatasets[currentDataset], 'c')
    print('Synthetic Tree #' + str(currentDataset) + ' accuracy: ' + str(treeAccuracy))

    syntheticTree.plot('Synthetic Data ' + str(currentDataset), 220 + (currentDataset + 1),
                       syntheticDatasets[currentDataset], 'c', 'a', 'b')

matplotlib.pyplot.show()
matplotlib.pyplot.suptitle('Decision Tree Plots', fontsize=30)
figure.savefig('decisionTreePlots.png')
