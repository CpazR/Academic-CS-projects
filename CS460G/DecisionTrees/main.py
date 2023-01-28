import os
import pandas

statFeatures = pandas.read_csv(os.getcwd() + '/data/pokemonStats.csv')
legendaryClassLabel = pandas.read_csv(os.getcwd() + '/data/pokemonLegendary.csv')

syntheticData = pandas.read_csv(os.getcwd() + '/data/synthetic-1.csv')

class DecisionTree:
    class Node:
        feature = 0
        predition = 0

        # feature val: child node
        children = dict()

    # end Node

    root = Node()

    def __init__(self):
        print('todo')

    # Equidistant
    def discretize(self, thresholdCount):
        print('todo')
        # In some range, define thresholds
        # Decide how many values you want to express in range
        # k values. Find thresholds so that distance between each thresh is equal.
        # So: thresholdOffset = (upperRange - lowerRange) / k
        # threshValues = list of size k - 1
        # EX: k = 5. threshValues = [2, 4, 6, 8]
    # end discretize

    def entropy(self, x):
        print('todo')
        # Sum of - (# of i examples / total examples) * log2(# of i examples / total examples)
        return 0
    # end entropy

    def informationGain(self, pCurrent):
        entropyPCurrent = self.entropy(pCurrent)
        # Weighted average entropy of children
        entropyChildren = 0

        return entropyPCurrent - entropyChildren
    # end informationGain

    def plot(self):
        print('todo')
    # end plot

# end DecisionTree
