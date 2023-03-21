import numpy
import tensorflow as rnn

shakesphereText = open('tiny-shakespeare.txt')

# Replace all newlines with plain whitespace then split based on sentence
sentences = shakesphereText.read().replace('\n', ' ').split('.')
characters = set(''.join(sentences))
characterCount = len(characters)

class RNNModel():
    def __init__(self):
        print('todo: implement model initialization')

    def forward(self):
        print('todo: implement forward pass')

    def hidden(self):
        print('todo: hidden state calculation')