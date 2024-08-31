import connect as service
import asyncio
import tensorflow as tf

# create dummy model for deep learning
def create_model():
    model = tf.keras.models.Sequential([
        tf.keras.layers.Dense(128, activation='relu'),
        tf.keras.layers.Dense(128, activation='relu'),
        tf.keras.layers.Dense(10, activation='softmax')
    ])
    model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])
    return model

# create a connection to the server
if __name__ == "__main__":
    conn = service.ConnectionService("localhost", 8080)
    print("Connection established!")