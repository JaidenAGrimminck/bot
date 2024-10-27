import tensorflow as tf
import numpy as np
import copy

# RUN: pip3.12 install --upgrade numpy=1.10.1

class Agent:
    """
    A class representing an agent in the simulation. The agent has a model that is used to make decisions.
    input_size: The size of the input vector
    output_size: The size of the output vector
    model: The model to use for the agent. If None, a new model will be created
    """
    def __init__(self, input_size, output_size, model=None):

        self.input_size = input_size
        self.output_size = output_size

        if model is None:
            self.model = create_model(input_size, output_size)
        else:
            self.model = model

        self.inputs = {}

    """
    Mutates the weights of the model. The mutation rate is the probability of a weight being mutated.
    mutation_rate: The probability of a weight being mutated
    mutation_strength: The strength of the mutation
    """
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

    """
    Returns the action to take given the current state
    state: The current state
    """
    def act(self, state):
        return np.argmax(self.model.predict(state)[0])

    """
    Predicts the output given the current state
    """
    def predict(self, state):
        predictions = self.model.predict(state, verbose = 0)
        return predictions

    """
    Clones the agent
    """
    def clone(self):
        return copy.deepcopy(self)

    """
    Randomizes the agent
    mutation_rate: The probability of a weight being mutated
    mutation_strength: The strength of the mutation
    """
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

    """
    Randomizes the agent (creates a new model)
    """
    def randomize(self):
        self.model = create_model(self.input_size, self.output_size)
        return self

    """
    Updates the sensor value
    """
    def update_sensor(self, address, n):
        self.inputs[address] = n
        pass

"""
Creates a new model
"""
def create_model(input_size=3, output_size=3):
    model = tf.keras.Sequential([
        tf.keras.layers.InputLayer(shape=(input_size,)),
        tf.keras.layers.Dense(8, activation='relu'),
        tf.keras.layers.Dense(5, activation='relu'),
        tf.keras.layers.Dense(output_size, activation='softmax')  # Softmax for probability distribution
    ])
    return model

create_model()