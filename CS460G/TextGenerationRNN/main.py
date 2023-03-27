import numpy as np
import torch
from torch import nn

device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')

shakespeareText = open('tiny-shakespeare.txt')

# Replace all newlines with plain whitespace then split based on sentence
lines = shakespeareText.readlines()
lines = [line.strip() for line in lines]

characters = set(''.join(lines))

integerCharacters = dict(enumerate(characters))

characterIntegers = {character: index for index, character in integerCharacters.items()}

inputSequence = []
outputSequence = []

for i in range(len(lines)):
    inputSequence.append(lines[i][:-1])
    outputSequence.append(lines[i][1:])

for i in range(len(lines)):
    inputSequence[i] = [characterIntegers[character] for character in inputSequence[i]]
    outputSequence[i] = [characterIntegers[character] for character in outputSequence[i]]


def oneHotGeneration(givenSequence, givenVocabularySize):
    encoding = np.zeros((1, len(givenSequence), givenVocabularySize), dtype=np.float32)
    for i in range(len(givenSequence)):
        encoding[0, i, givenSequence[i]] = 1

    return encoding


vocabularySize = len(characterIntegers)
oneHotGeneration(inputSequence[0], vocabularySize)


class RNNModel(nn.Module):
    def __init__(self, inputSize, outputSize, hiddenSize, layerCount):
        super(RNNModel, self).__init__()

        self.hiddenSize = hiddenSize
        self.layerCount = layerCount
        self.rnn = nn.RNN(inputSize, hiddenSize, layerCount, batch_first=True)
        self.fc = nn.Linear(hiddenSize, outputSize)

    def forward(self, x):
        hiddenState = torch.zeros(self.layerCount, 1, self.hiddenSize)
        hiddenState = hiddenState.to(device)

        output, hiddenState = self.rnn(x, hiddenState)
        output = output.contiguous().view(-1, self.hiddenSize)
        output = self.fc(output)

        return output, hiddenState

    def trainModel(self, input, output, epochs):
        loss = nn.CrossEntropyLoss()
        lossValue = 0

        optimizer = torch.optim.Adam(model.parameters())

        print('Training model over ', epochs, ' epochs:')
        for epoch in range(epochs):
            for i in range(len(input)):
                optimizer.zero_grad()

                # Not ideal way to do this, but works for this data set
                if len(input[i]) > 0:
                    x = torch.from_numpy(oneHotGeneration(input[i], vocabularySize))
                    y = torch.Tensor(output[i])

                    modelOutput, hidden = model(x)

                    yLoss = y.view(-1).long()

                    lossValue = loss(modelOutput, yLoss)
                    lossValue.backward()
                    optimizer.step()
        print("Loss: {:.4f}".format(lossValue.item()))

    def predict(self, character):
        characterInput = np.array([characterIntegers[c] for c in character])
        characterInput = oneHotGeneration(characterInput, vocabularySize)

        characterInput = torch.from_numpy(characterInput)
        characterInput = characterInput.to(device)

        out, hidden = model(characterInput)

        probability = nn.functional.softmax(out[-1], dim=0).data
        characterIndex = torch.max(probability, dim=0)[1].item()

        return integerCharacters[characterIndex], hidden

    def sample(self, outputLength, start='QUEEN'):
        characters = [ch for ch in start]
        currentSize = outputLength - len(characters)
        for i in range(currentSize):
            character, hiddenState = self.predict(characters)
            characters.append(character)

        return ''.join(characters)


# Define model
model = RNNModel(vocabularySize, vocabularySize, 100, 1)

# Train model
model.trainModel(inputSequence, outputSequence, 50)

# Test model through predictions
print("Model trained. Testing sample output:")
print(model.sample(100))
