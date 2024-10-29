import numpy as np

# some activation functions
# we're using sigmoid for this project
# but you can use relu or tanh

def relu(x):
    return np.maximum(0, x)

def tanh(x):
    return np.tanh(x)

def sigmoid(x):
    return 1 / (1 + np.exp(-x))

class NeuralNetwork:
    def __init__(self, shape):
        self.weights = []

        self.shape = shape # shape of the neural network

        self.size = len(shape) # easy access for later

        for i in range(1, self.size): # just initialize the weights
            self.weights.append(np.zeros((self.shape[i - 1], self.shape[i])))

    def zero(self): # zero the weights
        for i in range(1, self.size):
            self.weights[i - 1] = np.zeros((self.shape[i - 1], self.shape[i]))

    def randomize(self): # randomize the weights
        for i in range(1, self.size):
            self.weights[i - 1] = np.random.rand(self.shape[i - 1], self.shape[i]) - 0.5

    def set_weights(self, weights):
        for i in range(0, len(self.weights)):
            self.weights[i] = np.array(self.weights[i])

    def predict(self,input):
        layer = input
        for i in range(self.size - 1):
            layer = np.dot(layer, self.weights[i])

        for j in range(len(layer)):
            layer[j] = sigmoid(layer[j])

        return layer # should be a (1,x) size array

    def clone(self):
        n = NeuralNetwork(self.shape)
        for i in range(self.size - 1):
            n.weights[i] = self.weights[i]
        return n
    
    def mutate_weights(self, mutation):
        new_net = NeuralNetwork(self.shape)
        for i in range(1, self.size):
            new_net.weights[i - 1] = self.weights[i - 1] + (
                (np.random.rand(self.shape[i - 1], self.shape[i]) - 0.5) * mutation
            ) # mutate it -0.5 to 0.5 times mutation (scalar)
        
        return new_net

    # save/load this model

    def save(self, filename):
        np.save(filename, np.array(self.weights, dtype=object), allow_pickle=True)

    def load(self, filename):
        self.weights = np.load(filename, allow_pickle=True)
    
if __name__ == "__main__":
    # test the neural network
    a = NeuralNetwork([3, 4, 2])
    print(a.predict(np.array([1, 2, 3])))
