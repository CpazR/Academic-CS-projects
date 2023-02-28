import math
from typing import Any

import pandas
import numpy
import matplotlib.pyplot as plt

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
    return 1 / (1 + math.exp(-x))


class Perceptron:
    # a lit of 2d vectors
    layers = list()
    featuresCount = 784
    nodesPerLayer = [featuresCount, 50, 1]
    weights: dict
    biases: dict

    # Initialize with given training data
    def __init__(self, data: pandas.DataFrame, layerCount):
        # TODO: Start with single test data set
        trainingDataMap = self.normalizeData(data)

        learningRate = 0.5
        epochCount = 200

        # Setup initial weights and biases
        hiddenLayerInitialWeights = []
        for i in range(self.nodesPerLayer[0]):
            hiddenLayerInitialWeights.append(list(numpy.random.uniform(-1, 1, self.nodesPerLayer[1])))
        hiddenLayerInitialBias = numpy.random.uniform(0, 1, self.nodesPerLayer[1]).tolist()

        outputLayerInitialWeights = []
        for i in range(self.nodesPerLayer[1]):
            outputLayerInitialWeights.append(numpy.random.uniform(-1, 1))
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
                        outputLayerWeights: list, hiddenLayerBias: list, outputLayerBias: list) -> tuple[
        dict[str, list | list[Any] | Any], dict[str, list | Any]]:  # Weights, Biases
        for epoch in range(epochs):
            for index, xData in enumerate(trainDataMap['x']):
                # Get error of output layer
                predictionOutputInput, hiddenLayerInput, predictionOutput, hiddenLayer = \
                    self.forwardPass(xData, hiddenLayerWeights, outputLayerWeights, hiddenLayerBias, outputLayerBias)

                yData = trainDataMap['y'][index]

                error = yData - predictionOutput

                # Derivative of sigmoid(x)
                sigmoidPrime = lambda x: sigmoid(x) * (1 - sigmoid(x))

                outputDelta = error * sigmoidPrime(predictionOutputInput)
                hiddenDelta = numpy.multiply(numpy.dot(outputLayerWeights, outputDelta),
                                             list(map(sigmoidPrime, hiddenLayerInput)))

                # Update weights and biases
                outputLayerWeights = outputLayerWeights + learningRate * numpy.dot(hiddenLayer, outputDelta)
                outputLayerBias = outputLayerBias + numpy.array(learningRate * outputDelta)
                hiddenLayerWeights = hiddenLayerWeights + learningRate * numpy.dot(hiddenLayer, hiddenDelta)
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
        hiddenLayerInput = numpy.dot(numpy.array(hiddenLayerWeights).transpose(), batchInput) + hiddenLayerBias
        hiddenLayer = list(map(sigmoid, hiddenLayerInput))

        # output layer
        outputLayerInput = numpy.dot(numpy.array(outputLayerWeights).transpose(), hiddenLayer) + outputLayerBias
        outputLayer = sigmoid(outputLayerInput)

        return outputLayerInput, hiddenLayerInput, outputLayer, hiddenLayer

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

multiLaterPerceptron = Perceptron(trainData[0], 3)

multiLaterPerceptron.test(testData[0])
