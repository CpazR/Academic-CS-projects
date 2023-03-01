import math
from typing import Any

import pandas
import numpy
import matplotlib.pyplot as plt

numpy.random.seed(0)

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
    return 1 / (1 + math.pow(math.e, -x))


class Perceptron:
    # a lit of 2d vectors
    layers = list()
    featuresCount = 784
    nodesPerLayer = [featuresCount, 50, 1]
    weights: dict
    biases: dict

    # Initialize with given training data
    def __init__(self, data: pandas.DataFrame, learningRate: float, epochCount: int):

        trainingDataMap = self.normalizeData(data)

        # Setup initial weights
        hiddenLayerInitialWeights = []
        for i in range(self.nodesPerLayer[0]):
            hiddenLayerInitialWeights.append(list(numpy.random.uniform(-1, 1, self.nodesPerLayer[1])))

        outputLayerInitialWeights = []
        for i in range(self.nodesPerLayer[1]):
            outputLayerInitialWeights.append(numpy.random.uniform(-1, 1))

        # Setup initial biases
        hiddenLayerInitialBias = numpy.random.uniform(0, 1, self.nodesPerLayer[1]).tolist()
        outputLayerInitialBias = numpy.random.uniform(0, 1, self.nodesPerLayer[2]).tolist()

        # Run backpropagation
        newWeights, newBiases = self.backpropagation(epochCount, trainingDataMap, learningRate,
                                                     hiddenLayerInitialWeights, outputLayerInitialWeights,
                                                     hiddenLayerInitialBias, outputLayerInitialBias)

        # Store weights and biases in perceptron class
        for layerKey, weights in enumerate(newWeights):
            self.weights[layerKey] = weights
        for layerKey, biases in enumerate(newBiases):
            self.biases[layerKey] = biases

    # end init

    # Normalize a given dataframe into x and y dimensions
    def normalizeData(self, data: pandas.DataFrame) -> dict:
        return {
            'x': list(map(lambda x: x / 255, data.iloc[:, 1:].to_numpy())),
            'y': list(data.iloc[:, 0])
        }

    # end normalizeData

    def backpropagation(self, epochs: int, trainDataMap: dict, learningRate: float, hiddenLayerWeights: list,
                        outputLayerWeights: list, hiddenLayerBias: list, outputLayerBias: list):
        for epoch in range(epochs):
            for index, xData in enumerate(trainDataMap['x']):
                # Get error of output layer
                predictionOutputInput, hiddenLayerInput, outputGradiant, hiddenGradiant = \
                    self.forwardPass(xData, hiddenLayerWeights, outputLayerWeights, hiddenLayerBias, outputLayerBias)

                yData = trainDataMap['y'][index]

                error = yData - outputGradiant

                # Derivative of sigmoid(x)
                sigmoidPrime = lambda x: sigmoid(x) * (1 - sigmoid(x))

                outputDelta = error * sigmoidPrime(predictionOutputInput)
                sigmoidPrimeResults = list(map(sigmoidPrime, hiddenLayerInput))
                hiddenDelta = numpy.multiply(numpy.dot(outputLayerWeights, outputDelta), sigmoidPrimeResults)

                # Update weights and biases
                outputLayerWeights = outputLayerWeights + learningRate * numpy.dot(hiddenGradiant, outputDelta)
                outputLayerBias = outputLayerBias + numpy.array(learningRate * outputDelta)
                hiddenLayerWeights = hiddenLayerWeights + learningRate * \
                                     numpy.outer(xData, numpy.array(hiddenDelta).transpose())
                hiddenLayerBias = hiddenLayerBias + numpy.array(learningRate * hiddenDelta)
            # end inner for
        # end outer for
        return {'output': outputLayerWeights, 'hidden': hiddenLayerWeights}, \
            {'output': outputLayerBias, 'hidden': hiddenLayerBias}

    # end backPropagation

    #
    def forwardPass(self, batchInput: numpy.ndarray, hiddenLayerWeights: list, outputLayerWeights: list,
                    hiddenLayerBias: list, outputLayerBias: list):
        # TODO: Generalize forwardPass
        # hidden layer
        hiddenLayerInput = numpy.dot(numpy.array(hiddenLayerWeights).transpose(), batchInput)
        hiddenLayerInput = numpy.add(hiddenLayerBias, hiddenLayerInput)
        hiddenLayerGradiant = list(map(sigmoid, hiddenLayerInput))

        # output layer
        outputLayerInput = numpy.dot(numpy.array(outputLayerWeights).transpose(), hiddenLayerGradiant)
        outputLayerInput = numpy.add(outputLayerBias, outputLayerInput)
        outputLayerGradiant = sigmoid(outputLayerInput)

        return outputLayerInput, hiddenLayerInput, outputLayerGradiant, hiddenLayerGradiant

    # end forwardPass

    # Test neural net against given test data and produce a few statistics
    def test(self, testData: pandas.DataFrame):
        normalizedTestData = self.normalizeData(testData)
        correctPredictionCount = 0

        for index, xData in enumerate(normalizedTestData['x']):
            predictionOutputInput, hiddenLayerInput, predictionOutput, hiddenLayer = \
                self.forwardPass(xData, self.weights['hidden'], self.weights['output'],
                                 self.biases['hidden'], self.biases['output'])

            roundedPrediction = round(predictionOutput)

            if roundedPrediction == normalizedTestData['y'][index]:
                correctPredictionCount += 1

        # end for

        print('Correct predictions: ', correctPredictionCount)
        print('Accuracy: ', (correctPredictionCount / len(normalizedTestData['y'])) * 100, '%')
    # end test


# end Perceptron
print("Training neural net...")
multiLaterPerceptron = Perceptron(trainData[0], 0.5, 200)

print("Testing neural net...")
multiLaterPerceptron.test(testData[0])
