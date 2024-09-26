import tensorflow as tf
import numpy as np
import copy

class Agent:
    def __init__(self, input_size, output_size, model=None):

        self.input_size = input_size
        self.output_size = output_size

        if model is None:
            self.model = create_model(input_size, output_size)
        else:
            self.model = model

        self.crashed = False

        self.inputs = {}

    def mutate_weights(self, model, mutation_rate=0.1, mutation_strength=0.05):
        new_model = tf.keras.models.clone_model(model)
        new_model.set_weights(model.get_weights())

        weights = new_model.get_weights()
        for i in range(len(weights)):
            if np.random.rand() < mutation_rate:
                mutation = np.random.randn(*weights[i].shape) * mutation_strength
                weights[i] += mutation
        new_model.set_weights(weights)

        return new_model

    def act(self, state):
        return np.argmax(self.model.predict(state)[0])

    def predict(self, state):
        predictions = self.model.predict(state, verbose = 0)
        return predictions

    def clone(self):
        return copy.deepcopy(self)

    def mutate_weights(self, mutation_rate=0.1, mutation_strength=0.05):
        new_model = tf.keras.models.clone_model(self.model)
        new_model.set_weights(self.model.get_weights())

        weights = new_model.get_weights()
        for i in range(len(weights)):
            if np.random.rand() < mutation_rate:
                mutation = np.random.randn(*weights[i].shape) * mutation_strength
                weights[i] += mutation
        new_model.set_weights(weights)

        self.model = new_model

    def randomize(self):
        self.model = create_model(self.input_size, self.output_size)
        return self

    def update_sensor(self, address, n):
        self.inputs[address] = n
        pass


def create_model(input_size=5, output_size=2):
    model = tf.keras.Sequential([
        tf.keras.layers.InputLayer(shape=(input_size,)),
        tf.keras.layers.Dense(4, activation='sigmoid'),
        tf.keras.layers.Dense(2, activation='sigmoid'),
        tf.keras.layers.Dense(output_size, activation='softmax')  # Softmax for probability distribution
    ])
    return model

# Create the initial model
# model = create_model()
#
# # Example input (8 distance values)
# input_data = np.random.rand(1, 8)
#
# # Get the action probabilities
# action_probs = model.predict(input_data)
# print("Action Probabilities:", action_probs)