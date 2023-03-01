import math
from typing import Any

import pandas
import numpy
import matplotlib.pyplot as plt

# numpy.random.seed(0)

# When taking in data, use matrices for data operations

trainData: list[pandas.DataFrame] = [
    pandas.read_csv('data/mnist_train_0_1.csv'),
    pandas.read_csv('data/mnist_train_0_4.csv')
]

# Only use to test trained perceptron
testData: list[pandas.DataFrame] = [
    pandas.read_csv('data/mnist_test_0_1.csv'),
    pandas.read_csv('data/mnist_test_0_4.csv')
]


def sigmoid(x):
    try:
        return 1 / (1 + math.pow(math.e, -x))
    except:
        # Account for potential overflow error
        return x > 0 if 1 else 0


class NeuralNetPerceptron:
    featuresCount = 784
    hiddenLayerNodes = 20
    # For the purposes of this assignment, only one node for output layer
    nodesPerLayer = [featuresCount, hiddenLayerNodes, 1]
    weights = {}
    biases = {}

    # Initialize with given training data
    def __init__(self, data: pandas.DataFrame, learningRate: float, epochCount: int):
        trainingDataMap = self.normalizeData(data)

        # Setup initial weights
        hiddenLayerInitialWeights = []
        outputNodeInitialWeights = []
        for i in range(self.nodesPerLayer[0]):
            hiddenLayerInitialWeights.append(list(numpy.random.uniform(-1, 1, self.nodesPerLayer[1])))
        for i in range(self.nodesPerLayer[1]):
            outputNodeInitialWeights.append(numpy.random.uniform(-1, 1))

        self.weights['hidden'] = hiddenLayerInitialWeights
        self.weights['output'] = outputNodeInitialWeights

        # Setup initial biases
        self.biases['hidden'] = numpy.random.uniform(0, 1, self.nodesPerLayer[1]).tolist()
        self.biases['output'] = numpy.random.uniform(0, 1, self.nodesPerLayer[2]).tolist()

        # Run backpropagation to train neural net
        self.backpropagation(epochCount, trainingDataMap, learningRate)

    # end init

    # Normalize a given dataframe into x and y dimensions
    def normalizeData(self, data: pandas.DataFrame) -> dict:
        return {
            'alphaValues': list(map(lambda x: x / 255, data.iloc[:, 1:].to_numpy())),
            'labels': list(data.iloc[:, 0])
        }

    # end normalizeData

    def backpropagation(self, epochs: int, trainDataMap: dict, learningRate: float):
        hiddenLayerWeights = self.weights['hidden']
        outputNodeWeights = self.weights['output']
        hiddenLayerBias = self.biases['hidden']
        outputNodeBias = self.biases['output']

        for epoch in range(epochs):
            for index, alphaBatch in enumerate(trainDataMap['alphaValues']):
                # Get error of output layer
                outputNodeInput, hiddenLayerInput, outputNodeOutput, hiddenLayerOutput = \
                    self.forwardPass(alphaBatch, hiddenLayerWeights, outputNodeWeights, hiddenLayerBias, outputNodeBias)

                yData = trainDataMap['labels'][index]

                error = yData - outputNodeOutput

                # Derivative of sigmoid(x)
                sigmoidPrime = lambda x: sigmoid(x) * (1 - sigmoid(x))

                outputDelta = error * sigmoidPrime(outputNodeInput)
                hiddenDelta = numpy.dot(outputNodeWeights, outputDelta)
                hiddenLayerGradients = numpy.outer(alphaBatch, numpy.array(hiddenDelta))

                # Update weights and biases
                outputNodeWeights = outputNodeWeights + learningRate * numpy.dot(hiddenLayerOutput, outputDelta)
                outputNodeBias = outputNodeBias + numpy.array(learningRate * outputDelta)
                hiddenLayerWeights = hiddenLayerWeights + learningRate * hiddenLayerGradients
                hiddenLayerBias = hiddenLayerBias + numpy.array(learningRate * hiddenDelta)
            # end inner for
        # end outer for

        # Apply updated weights and biases to neural net dictionaries
        self.weights['hidden'] = hiddenLayerWeights
        self.weights['output'] = outputNodeWeights
        self.biases['hidden'] = hiddenLayerBias
        self.biases['output'] = outputNodeBias

    # end backPropagation

    #
    def forwardPass(self, batchInput: numpy.ndarray, hiddenLayerWeights: list, outputNodeWeights: list,
                    hiddenLayerBias: list, outputNodeBias: list):
        # hidden layer
        hiddenLayerInput = numpy.dot(numpy.array(hiddenLayerWeights).transpose(), batchInput)
        hiddenLayerInput = numpy.add(hiddenLayerBias, hiddenLayerInput)
        hiddenLayerOutput = list(map(sigmoid, hiddenLayerInput))

        # output layer
        outputNodeInput = numpy.dot(numpy.array(outputNodeWeights).transpose(), hiddenLayerOutput)
        outputNodeInput = numpy.add(outputNodeBias, outputNodeInput)
        outputNodeOutput = sigmoid(outputNodeInput)

        return outputNodeInput, hiddenLayerInput, outputNodeOutput, hiddenLayerOutput

    # end forwardPass

    # Test neural net against given test data and produce a few statistics
    def test(self, testData: pandas.DataFrame):
        normalizedTestData = self.normalizeData(testData)
        correctPredictionCount = 0
        totalPredictions = len(normalizedTestData['labels'])

        for index, alphaBatch in enumerate(normalizedTestData['alphaValues']):
            predictionOutputInput, hiddenLayerInput, predictionOutput, hiddenLayer = \
                self.forwardPass(alphaBatch, self.weights['hidden'], self.weights['output'],
                                 self.biases['hidden'], self.biases['output'])

            roundedPrediction = round(predictionOutput)

            if roundedPrediction == normalizedTestData['labels'][index]:
                correctPredictionCount += 1

        # end for

        print('Total predictions: ', totalPredictions)
        print('Correct predictions: ', correctPredictionCount)
        print('Accuracy: ', (correctPredictionCount / totalPredictions) * 100, '%')
    # end test


# end Perceptron

print("Training neural net (0 - 1)...")
multiLaterPerceptron = NeuralNetPerceptron(trainData[0], 0.5, 50)

print("Testing neural net (0 - 1)...")
multiLaterPerceptron.test(testData[0])

print("\n----------------\n")

print("Training neural net (0 - 4)...")
multiLaterPerceptron = NeuralNetPerceptron(trainData[1], 0.5, 50)

print("Testing neural net (0 - 4)...")
multiLaterPerceptron.test(testData[1])
