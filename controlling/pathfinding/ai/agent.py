import tensorflow as tf
import numpy as np
import copy

class Agent:
    def __init__(self, model, state_size, action_size):
        self.model = model
        self.state_size = state_size
        self.action_size = action_size

        self.crashed = False

        self.inputs = {}

    def act(self, state):
        return np.argmax(self.model.predict(state)[0])

    def train(self, state, target):
        self.model.fit(state, target, epochs=1, verbose=0)

    def predict(self, state):
        return self.model.predict(state)

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

    def update_sensor(self, address, n):
        self.inputs[address] = n / 256

    def evaluate_model(model, environment):
        total_reward = 0
        state = environment.reset()
        done = False
        while not done:
            action_probs = model.predict(state.reshape(1, -1))
            action = np.argmax(action_probs)
            state, reward, done, _ = environment.step(action)
            total_reward += reward
        return total_reward


def create_model(input_size=8, output_size=8):
    model = tf.keras.Sequential([
        tf.keras.layers.InputLayer(shape=(input_size,)),
        tf.keras.layers.Dense(16, activation='relu'),
        tf.keras.layers.Dense(16, activation='relu'),
        tf.keras.layers.Dense(output_size, activation='softmax')  # Softmax for probability distribution
    ])
    return model

model = create_model()

# Example input (8 distance values)
input_data = np.random.rand(1, 8)

# Get the action probabilities
action_probs = model.predict(input_data)
print("Action Probabilities:", action_probs)