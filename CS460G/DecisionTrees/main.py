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

            discretizeBin = pandas.cut(x=column, bins=bins)
            print(discretizeBin)

            # In some range, define thresholds
            # Decide how many values you want to express in range
            # k values. Find thresholds so that distance between each thresh is equal.
            # So: thresholdOffset = (upperRange - lowerRange) / k
            # threshValues = list of size k - 1
            # EX: k = 5. threshValues = [2, 4, 6, 8]

        # end discretize

        def entropy(self, dataset: list):
            print('todo')
            # Sum of - (# of i examples / total examples) * log2(# of i examples / total examples)
            # if (# of i examples / total examples)tree is 50/50, then skip calculation and default to 1

            dataQuantity = len(dataset)

            entropyValue = 0

            for i in range(dataQuantity):
                print(dataset[i])
                numberOfPossibilities = 0
                probability = (numberOfPossibilities / dataQuantity)
                entropyValue += -probability * math.log2(probability)

            return 0

        # end entropy

        def informationGain(self, pCurrent, pChilren: list):
            # difference of parent entropy and the wighted average entropy of children
            entropyPCurrent = self.entropy(pCurrent)

            entropyChildren = 0

            for child in pChilren:
                currentEquiv: list = []  # set where current = child
                entropyChildren += len(pCurrent) / len(currentEquiv) * self.entropy(currentEquiv)

            return entropyPCurrent - entropyChildren

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
