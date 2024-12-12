import ws
from neural_network import NeuralNetwork
import numpy as np

model = None

SPEED = 2
ROTATION_SPEED = 1

def act(points):
    front = points[0]
    left = points[1]
    right = points[2]

    # straight, right, left
    distances = [
        front[0] - 20,
        left[0] - 20,
        right[0] - 20
    ]

    for i in range(len(distances)):
        if (distances[i] == None):
            distances[i] = None # redundant. but it skips some stuff.
        elif (distances[i] <= 0):
            distances[i] = None
        elif distances[i] > 300:
            distances[i] = None

        if distances[i] == None:
            distances[i] = 1
        else:
            distances[i] = distances[i] / 300
    
    values = model.predict(np.array(distances).reshape(1, -1))[0]

    speed = values[1] * SPEED
    rotation = (values[0] - 0.5) * ROTATION_SPEED * 10

    print(speed, rotation)

    ws.send_move(speed, rotation)

if __name__ == "__main__":
    ws.start_websocket()

    model = NeuralNetwork([3, 4, 3, 2])
    model.load("model483.npy")

    ws.listenWithoutSubscribing(0xA6, 0xA6, act)

    while True:
        pass

