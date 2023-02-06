import math
import os
import pandas

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
        feature = 0
        prediction = 0

        # If level == 0, then root node. If level == maxDepth, cannot split any deeper.
        level = 0

        # feature val: child node
        children = dict()

        ### Functions for training

        def entropy(self, classLabelSubset: pandas.Series, classLabelColumn: pandas.Series):
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
        # Note that the target attribute column has the class label included to process the category more easily
        def informationGain(self, data: pandas.DataFrame, targetAttribute: str, classLabel: str):
            # Calculate parent entropy
            classLabelColumn = data[classLabel]
            parentEntropy = self.entropy(data[classLabel], classLabelColumn)
            print("Parent entropy: ", parentEntropy)

            # Store the frequency of each bin for the target attribute
            binDataCount = data[targetAttribute].value_counts()

            # Calculate the wighted average entropy of children
            childrenEntropy = 0

            # For each bin (or "child"), calculate the entropy and determine the average
            for interval, count in binDataCount.items():
                # Get data within given intervals
                subset = data.loc[data[targetAttribute] == interval]
                subsetEntropy = self.entropy(subset[classLabel], classLabelColumn)

                childProbability = count / len(data)
                childEntropy = childProbability * subsetEntropy
                childrenEntropy += childEntropy

            print("Children entropy: ", childrenEntropy)
            #  Finish information gain calculation
            return parentEntropy - childrenEntropy

        # end informationGain

        def __init__(self, level: int, data: pandas.DataFrame, classLabels: list, dataSetAttributes: list):
            self.level = level
            # Store information gains of each attribute in a dictionary. This is primarily for debugging purposes.
            informationGains = dict()
            greatestGainAttribute = dataSetAttributes[0]

            # Determine feature with most discriminatory power
            for attribute in dataSetAttributes:
                targetAndClassColumn = data[[attribute, classLabels[0]]]
                informationGains[attribute] = self.informationGain(data, attribute, classLabels[0])

                if informationGains[greatestGainAttribute] < informationGains[attribute]:
                    greatestGainAttribute = attribute

            print(informationGains)
            print("Greatest information gain: ", informationGains[greatestGainAttribute])

    # end Node

    root: Node

    def __init__(self, data: pandas.DataFrame, classLabels: list, dataSetAttributes: list):
        root = self.Node(0, data, classLabels, dataSetAttributes)

    # Using ID3 algorithm
    def trainTree(self):
        print("TODO")

    def plot(self):
        print('todo')

    # end plot


# end DecisionTree

# Preprocess data
def discretize(column: pandas.Series):
    # Array of min and max values for thresholds
    thresholdRange = [min(column), max(column)]
    # In some range, define thresholds
    # Decide how many values you want to express in range
    # k values. Find thresholds so that distance between each thresh is equal.
    # So: thresholdOffset = (upperRange - lowerRange) / k
    # threshValues = list of size k - 1
    # EX: k = 5. threshValues = [2, 4, 6, 8]

    # Default to 3 bins. Downsize depending on datatype.

    if thresholdRange[1] - thresholdRange[0] == 1:
        binCount = 2
    else:
        binCount = 3

    """
    thresholdRange = [0, 10]  # array of min and max values for thresholds
    thresholdDelta = (thresholdRange[1] - thresholdRange[0]) / binCount

    for dx in range(binCount):
        lowerRange = thresholdDelta * dx
        upperRange = (thresholdDelta * dx) + thresholdDelta
        someVal = 0
        if lowerRange < someVal < upperRange:
            print("In range")
    """
    return pandas.qcut(column, binCount)


# end discretize

# Run decision trees for each iteration of synthetic data
currentDataset = 1

syntheticDatasets[currentDataset]['a'] = discretize(syntheticDatasets[currentDataset]['a'])
syntheticDatasets[currentDataset]['b'] = discretize(syntheticDatasets[currentDataset]['b'])

syntheticTree = DecisionTree(syntheticDatasets[currentDataset], ['c'], ['a', 'b'])
