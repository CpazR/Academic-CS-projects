import math
import os
import pandas

sourceData = pandas.read_csv(os.getcwd() + '/data/pokemonStats.csv').iloc[0]

# Used for testing against results
legendaryActualData = pandas.read_csv(os.getcwd() + '/data/pokemonLegendary.csv')

# Use to train decision tree.
syntheticDatasets: list = [pandas.read_csv(os.getcwd() + '/data/synthetic-1.csv'),
                           pandas.read_csv(os.getcwd() + '/data/synthetic-2.csv'),
                           pandas.read_csv(os.getcwd() + '/data/synthetic-3.csv'),
                           pandas.read_csv(os.getcwd() + '/data/synthetic-4.csv')]

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

        # Equidistant
        def discretize(self, column: list):
            thresholdRange = [min(column), max(column)]  # array of min and max values for thresholds
            # In some range, define thresholds
            # Decide how many values you want to express in range
            # k values. Find thresholds so that distance between each thresh is equal.
            # So: thresholdOffset = (upperRange - lowerRange) / k
            # threshValues = list of size k - 1
            # EX: k = 5. threshValues = [2, 4, 6, 8]

            # Default to 10 bins. Downsize depending on datatype.

            if thresholdRange[1] - thresholdRange[0] == 0:
                binCount = 2
            else:
                binCount = 10

            thresholdDelta = (thresholdRange[1] - thresholdRange[0]) / binCount

            bins: list = [thresholdRange[0]]

            for dx in range(binCount - 1):
                bins.append(thresholdDelta * dx)

            bins.append(thresholdRange[1])

            return pandas.cut(x=column, bins=bins)

        # end discretize

        def entropy(self, column: list):
            print('todo')
            # Sum of - (# of i examples / total examples) * log2(# of i examples / total examples)
            # if (# of i examples / total examples)tree is 50/50, then skip calculation and default to 1

            dataQuantity = len(column)

            # Calculate frequency of all values
            dataFrequency = dict()

            for dataIndex in column:
                # Add to frequency dictionary if not yet seen
                if dataIndex not in dataFrequency:
                    dataFrequency[dataIndex] = 0
                # Increment 1 to the frequency dictionary
                dataFrequency[dataIndex] += 1

            entropyValue = 0

            # Skip entropy calculation if data is homogenous
            if len(dataFrequency) > 1 and self.level >= maxTreeDepth:
                for key, value in dataFrequency.items():
                    # Frequency of specific value (key)
                    frequency = value / len(column)
                    # If value does not appear, skip to next iteration
                    if frequency == 0:
                        continue
                    log = math.log(frequency, len(dataFrequency))
                    entropyValue -= log * frequency

            return entropyValue

        # end entropy

        # Given a target column and a column to split on, run information gain
        def informationGain(self, targetColumn: list, splitColumn: list):
            # Calculate parent entropy
            parentEntropy = self.entropy(targetColumn)

            # Store the frequency of both positive and negative class labels
            positiveDataFrequency = dict()
            negativeDataFrequency = dict()

            for index, cell in enumerate(splitColumn):
                if cell not in positiveDataFrequency:
                    positiveDataFrequency[cell] = 0
                if cell not in negativeDataFrequency:
                    negativeDataFrequency[cell] = 0

                # This is assuming that the splitColumn is a numerical boolean flag
                if targetColumn[index] == 1:
                    positiveDataFrequency[cell] += 1
                if targetColumn[index] == 0:
                    negativeDataFrequency[cell] += 1

            # Calculate the wighted average entropy of children
            childrenEntropy = 0

            # NOTE: Since both positive and negative frequencies share keys, we can iterate over one of the
            # dictionaries and use them on both
            for key in positiveDataFrequency.keys():
                dataSubset = list()
                if positiveDataFrequency[key] != 0:
                    dataSubset = list(positiveDataFrequency.values())
                if negativeDataFrequency[key] != 0:
                    dataSubset = list(negativeDataFrequency.values())

                childProbability = len(dataSubset) / len(splitColumn)
                childrenEntropy += childProbability * self.entropy(dataSubset)

            #  Finish information gain calculation
            return parentEntropy - childrenEntropy

        # end informationGain

    # end Node

    root = Node()

    def __init__(self):
        print('todo')
        dataColumn = syntheticDatasets[0].iloc[:, 0].values
        self.root.discretize(dataColumn)

    def plot(self):
        print('todo')

    # end plot


# end DecisionTree

tree = DecisionTree()
