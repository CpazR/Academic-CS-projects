import cv2
import os
import glob
from sklearn.utils import shuffle
import numpy as np
import tensorflow as tf


# A class containing various information about the training set
class DataSet(object):

    def __init__(self, images, labels, img_names, cls):
        self._num_examples = images.shape[0]

        self._images = images
        self._labels = labels
        self._img_names = img_names
        self._cls = cls
        self._epochs_done = 0
        self._index_in_epoch = 0

    # Return the set of images
    @property
    def images(self):
        return self._images

    # Return the set of 1-hot class vectors
    @property
    def labels(self):
        return self._labels

    # Return the set of image filenames
    @property
    def img_names(self):
        return self._img_names

    # Return the set of class labels
    @property
    def cls(self):
        return self._cls

    # Return the number of examples in the training set
    @property
    def num_examples(self):
        return self._num_examples

    # Return the number of epochs that have been completed
    @property
    def epochs_done(self):
        return self._epochs_done

    # Retrieve the next batch of data to pass to the neural network
    # Inputs:
    # batch_size: The number of training examples to return in a batch
    # Outputs:
    # the images in the next batch, the 1-hot class vectors for the next batch, the filenames in the next batch, and the class labels in the next batch
    def next_batch(self, batch_size):
        start = self._index_in_epoch
        self._index_in_epoch += batch_size

        if self._index_in_epoch > self._num_examples:
            self._epochs_done += 1
            start = 0
            self._index_in_epoch = batch_size
            assert batch_size <= self._num_examples
        end = self._index_in_epoch

        return self._images[start:end], self._labels[start:end], self._img_names[start:end], self._cls[start:end]


class DataSets(object):
    train: DataSet
    valid: DataSet


class CNNModel:
    def __init__(self, imageSize, imageChannelCount):
        self.optimizer = None
        self.session = None
        self.lossValues = []
        self.accuracyValues = []
        self.yTrue = None

        tf.compat.v1.disable_eager_execution()
        tf.compat.v1.disable_v2_behavior()
        # Convolutional layer properties
        self.convFilterSize01 = 3
        self.convFilterCount01 = 32

        self.convFilterSize02 = 3
        self.convFilterCount02 = 32

        self.convFilterSize03 = 3
        self.convFilterCount03 = 64

        # Fully connected layer properties
        self.connectedLayerSize = 128

        self.classCount = 2

        self.imageDimensions = imageSize ** 2 * imageChannelCount
        self.xVal = tf.compat.v1.placeholder(tf.float32, shape=[None, self.imageDimensions], name='xVal')
        initialImage = tf.reshape(self.xVal, [-1, imageSize, imageSize, imageChannelCount])

        # Generate layers
        self.convLayer01, self.convWeights01 = self.generateConvLayer(input=initialImage,
                                                                      channelCount=imageChannelCount,
                                                                      filterSize=self.convFilterSize01,
                                                                      filterCount=self.convFilterCount01)
        self.convLayer02, self.convWeights02 = self.generateConvLayer(input=self.convLayer01,
                                                                      channelCount=self.convFilterCount01,
                                                                      filterSize=self.convFilterSize02,
                                                                      filterCount=self.convFilterCount02)
        self.convLayer03, self.convWeights03 = self.generateConvLayer(input=self.convLayer02,
                                                                      channelCount=self.convFilterCount02,
                                                                      filterSize=self.convFilterSize03,
                                                                      filterCount=self.convFilterCount03)
        self.flattenedLayer, self.featureCount = self.flattenLayer(self.convLayer03)

        self.connectedLayer01 = self.generateConnectedLayer(input=self.flattenedLayer, inputCount=self.featureCount,
                                                            outputCount=self.connectedLayerSize, useRelu=True)
        self.connectedLayer02 = self.generateConnectedLayer(input=self.connectedLayer01,
                                                            inputCount=self.connectedLayerSize,
                                                            outputCount=self.classCount, useRelu=False)

    def trainModel(self, trainInput, iterations, batchSize):
        self.yTrue = tf.compat.v1.placeholder(tf.float32, shape=[None, self.classCount], name='yTrue')

        # Some optimizations
        crossEntropy = tf.nn.softmax_cross_entropy_with_logits(logits=self.connectedLayer02, labels=self.yTrue)
        cost = tf.reduce_mean(crossEntropy)
        optimizer = tf.compat.v1.train.AdamOptimizer(learning_rate=1e-4).minimize(cost)

        currentLoss = 0

        self.initSession()

        # Begin training
        for i in range(iterations):
            xBatch, yTrueBatch, _, clsBatch = trainInput.train.next_batch(batchSize)
            xValidBatch, yValidBatch, _, clsValidBatch = trainInput.valid.next_batch(batchSize)

            xBatch = xBatch.reshape(batchSize, self.imageDimensions)
            xValidBatch = xValidBatch.reshape(batchSize, self.imageDimensions)

            feedDictTrain = {self.xVal: xBatch, self.yTrue: yTrueBatch}
            feedDictValid = {self.xVal: xValidBatch, self.yTrue: yValidBatch}

            self.session.run(optimizer, feed_dict=feedDictTrain)

            # Calculate loss and add to list
            currentLoss = self.session.run(cost, feed_dict=feedDictValid)
            self.lossValues.append(currentLoss)

        print("Final loss: " + str(currentLoss))

    def testAgainstData(self, testInput: DataSet):
        testCount = len(testInput._images)

        yPrediction = tf.nn.softmax(self.connectedLayer02)
        yPredictionClass = tf.argmax(yPrediction, axis=1)

        classPredictions = np.zeros(shape=testCount, dtype=int)
        batchSize = 1

        # Test input data against existing model
        for i in range(testCount):
            image, label, _, imageClass = testInput.next_batch(batchSize)

            image = image.reshape(batchSize, self.imageDimensions)
            feedDictTest = {self.xVal: image, self.yTrue: label}

            # Make predictions of test data against model and add class preditctions to a predictions array
            classPrediction = self.session.run(yPredictionClass, feed_dict=feedDictTest)
            np.put(classPredictions, i, classPrediction)

        classTrue = np.array(testInput.cls)
        totalPredictions = np.array([classes[x] for x in classPredictions])

        correctPredictions = (classTrue == totalPredictions).sum()
        totalAccuracy = float(correctPredictions) / testCount

        print("Final accuracy: " + str(totalAccuracy))

    # High level helper methods

    def generateConvLayer(self, input, channelCount, filterSize, filterCount):
        shape = [filterSize, filterSize, channelCount, filterCount]

        weights = self.generateWeights(shape)
        biases = self.generateBiases(length=filterCount)

        newLayer = self.conv2d(input, weights) + biases
        newLayer = self.maxPool2d(newLayer)
        newLayer = tf.nn.relu(newLayer)

        return newLayer, weights

    def flattenLayer(self, layer):
        layerShape = layer.get_shape()
        featureCount = layerShape[1:4].num_elements()

        flattenedLayer = tf.reshape(layer, [-1, featureCount])

        return flattenedLayer, featureCount

    def generateConnectedLayer(self, input, inputCount, outputCount, useRelu=True):
        weights = self.generateWeights(shape=[inputCount, outputCount])
        biases = self.generateBiases(length=outputCount)

        newlyConnectedLayer = tf.matmul(input, weights) + biases

        if useRelu:
            newlyConnectedLayer = tf.nn.relu(newlyConnectedLayer)

        return newlyConnectedLayer

    # Low level helper methods for tensorflow operations

    def initSession(self):
        # Init session
        self.session = tf.compat.v1.Session()
        self.session.run(tf.compat.v1.global_variables_initializer())

    def endSession(self):
        self.session.close()

    def generateWeights(selfself, shape):
        return tf.Variable(tf.random.truncated_normal(shape, stddev=0.1))

    def generateBiases(self, length):
        return tf.Variable(tf.constant(0.1, shape=[length]))

    def conv2d(self, x, w):
        return tf.nn.conv2d(input=x, filters=w, strides=[1, 1, 1, 1], padding='SAME')

    def maxPool2d(self, x):
        return tf.nn.max_pool(input=x, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')


# Helper function to load in the training set of images and resize them all to the given size

def load_data(train_path, image_size, classes):
    images = []
    labels = []
    img_names = []
    cls = []

    print('Going to read training images')
    for fields in classes:
        index = classes.index(fields)
        print('Now going to read {} files (Index: {})'.format(fields, index))
        path = os.path.join(train_path, fields, '*g')
        files = glob.glob(path)
        for fl in files:
            image = cv2.imread(fl)
            image = cv2.resize(image, (image_size, image_size), 0, 0, cv2.INTER_LINEAR)
            image = image.astype(np.float32)
            image = np.multiply(image, 1.0 / 255.0)
            images.append(image)
            label = np.zeros(len(classes))
            label[index] = 1.0
            labels.append(label)
            flbase = os.path.basename(fl)
            img_names.append(flbase)
            cls.append(fields)
    images = np.array(images)
    labels = np.array(labels)
    img_names = np.array(img_names)
    cls = np.array(cls)

    return images, labels, img_names, cls


# Code to read in training data and put it in a decent format for learning
# Inputs:
# train_path: a string containing the path to the training data
# image_size: image size (in pixels) that each training image will be resized to. Resulting dimensions will be image_size x image_size
# classes: an array containing each of the classes. For this assignment, it would be ['pembroke', 'cardigan']
# validation_size: Float corresponding to the proportion of the training set to set aside for validation. This is different than the test set!
# Returns:
# data_sets: a DataSet object containing images, labels, 1-hot label vectors, filenames, as well as training and validation data.
def read_train_sets(train_path, image_size, classes, validation_size):
    data_sets = DataSets()

    images, labels, img_names, cls = load_data(train_path, image_size, classes)
    images, labels, img_names, cls = shuffle(images, labels, img_names, cls)

    if isinstance(validation_size, float):
        validation_size = int(validation_size * images.shape[0])

    validation_images = images[:validation_size]
    validation_labels = labels[:validation_size]
    validation_img_names = img_names[:validation_size]
    validation_cls = cls[:validation_size]

    train_images = images[validation_size:]
    train_labels = labels[validation_size:]
    train_img_names = img_names[validation_size:]
    train_cls = cls[validation_size:]

    data_sets.train = DataSet(train_images, train_labels, train_img_names, train_cls)
    data_sets.valid = DataSet(validation_images, validation_labels, validation_img_names, validation_cls)

    return data_sets


# Image properties
imageSize = 128
channelCount = 3

batchSize = 32
validationSize = 0.2

classes = ['pembroke', 'cardigan']

dataSets = read_train_sets("data/training_data", imageSize, classes, validationSize)
testingDataset = DataSet(*load_data("data/testing_data", imageSize, classes))

print("Initializing model...")
model = CNNModel(imageSize, channelCount)
print("Model initialized!")

epochs = 500
print('Training model over', epochs, 'epochs...')
model.trainModel(dataSets, epochs, batchSize)

print("Testing model...")
model.testAgainstData(testingDataset)

print("Ending session...")
model.endSession()
