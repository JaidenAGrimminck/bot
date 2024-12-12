import websocket
import time
import struct
import threading
from wssensor import WSSensor
from neural_network import NeuralNetwork
import numpy as np
import robot

neural_network = None

sensors = {}
listeners = {}

socket = None

WEBSOCKET_URL = "ws://192.168.6.233:8080"

def on_message(ws, message):
    global timeSinceLastHeartbeat

    # successful connection
    if message[0] == 0xFF and message[1] == 0xFF:
        print('successful connection')
        onConnection()
        return

    # heartbeat
    if message[0] == 0xFF and message[1] == 0x00:
        timeSinceLastHeartbeat = time.time()
        ws.send([0xFF, 0x01])
        return

    try:
        if ''.join(message).startswith("-18"):
            print("Stack trace: " + message[4:])
            return
        else: return
    except:
        pass

    key = message[0]

    # update
    if key == 0xC0:
        datatype = message[1]
        if datatype == 0x01:
            robotAddress = message[2]
            sensorAddress = message[3]
            n_values = message[4]

            initial = 5 # constant. How many bytes to skip to get to the first value

            for i in range(0, n_values, 1):
                value = []
                for j in range(0, 8, 1):
                    value.append(message[initial + j + (i * 8)])

                # value is a double from java, so we need to convert it to a python float
                value = struct.unpack('>d', bytes(value))[0]

                if robotAddress not in sensors:
                    sensors[robotAddress] = {}
                    print("Robot address added: " + str(robotAddress))

                if sensorAddress in sensors[robotAddress]:
                    sensors[robotAddress][sensorAddress].update_value(i, value)
                else:
                    sensors[robotAddress][sensorAddress] = WSSensor(sensorAddress, n_values)
                    sensors[robotAddress][sensorAddress].update_value(i, value)
                    print("Sensor address added: " + str(sensorAddress))

                if robotAddress in listeners:
                    if sensorAddress in listeners[robotAddress]:
                        for listener in listeners[robotAddress][sensorAddress]:
                            # get how many pluggable arguments the listener has
                            n_args = listener.__code__.co_argcount

                            if n_args == 0:
                                listener()
                            elif n_args == 1:
                                listener(sensors[robotAddress][sensorAddress].get_values())
                            elif n_args == 2:
                                listener(sensors[robotAddress][sensorAddress].get_values(), robotAddress)
                            elif n_args == 3:
                                listener(sensors[robotAddress][sensorAddress].get_values(), robotAddress, sensorAddress)
    elif key == 0xA6:
        # first four bytes are ignored

        points = []

        for i in range(3):
            point = message[i*12 + 4:i*12 + 12 + 4]
            distance = struct.unpack('>f', bytes(point[0:4]))[0]
            angle = struct.unpack('>f', bytes(point[4:8]))[0]
            intensity = struct.unpack('>f', bytes(point[8:12]))[0]

            points.append([distance, angle, intensity])
        
            if str(0xA6) in listeners:
                if str(0xA6) in listeners[str(0xA6)]:
                    for listener in listeners[str(0xA6)][str(0xA6)]:
                        listener(points)



sensor_enum = {
    "FRONT": 0x01,
    "TOP_LEFT": 0x02,
    "TOP_RIGHT": 0x03
}

sensor_values = {
    sensor_enum["FRONT"]: [0],
    sensor_enum["TOP_LEFT"]: [0],
    sensor_enum["TOP_RIGHT"]: [0]
}

"""
Called when the connection is successful
"""
def onConnection():
    print('Successfully registered and connected.')

    def handleDist(sensor, dist):
        sensor_values[sensor] = dist
        pass

    # subscribe to the robot
    # listen(0x00, 0x01, lambda a: handleDist(sensor_enum["FRONT"], a)) #front side
    # listen(0x00, 0x02, lambda a: handleDist(sensor_enum["TOP_LEFT"], a)) #top left
    # listen(0x00, 0x03, lambda a: handleDist(sensor_enum["TOP_RIGHT"], a)) #right side
    
    threading.Thread(target=subscribeToLIDARSpecifics).start()

    pass

def onDisconnect():
    print('disconnected')
    pass

def on_error(ws, error):
    print(f"Websocket Error: {error}")

def on_close(ws, close_status_code, close_msg):
    print(f"WebSocket closed with message: {close_msg}, code: {close_status_code}")

    if close_status_code == 1006:
        print("Attempting to reconnect...")
        start_websocket()

    if close_status_code == 1000:
        onDisconnect()


def on_open(ws):
    global socket

    def run(*args):
        global socket

        initial_message = [0xFF,
                           0x03,
                           0x00]

        ws.send(initial_message)

        print('Sent initial handshake message to connect.')

        socket = ws

    threading.Thread(target=run).start()

"""
Sends a payload to the websocket
:param payload: The payload to send
"""
def sendPayload(payload=[]):
    if socket == None:
        print("Websocket not connected")
        return

    socket.send(payload)

def send_move(speed, rotation):
    payload = [
        0x01,
        0x02,
        0x09,
    ]
    
    first_double = struct.pack('>d', rotation)
    second_double = struct.pack('>d', speed)

    payload.extend(first_double)
    payload.extend(second_double)

    sendPayload(payload)

"""
Subscribe to a sensor
:param robotAddress: The address of the robot
:param sensorAddress: The address of the sensor
:param unsubscribe: Whether to subscribe or unsubscribe
"""
def subscribe(robotAddress, sensorAddress, unsubscribe=False):
    subscribe_bit = 0x01

    if unsubscribe:
        subscribe_bit = 0x00

    payload = [
        0x02, # listen
        0x11, # subscribe
        robotAddress, # address of robot
        sensorAddress, # address of sensor
        subscribe_bit # subscribe
    ]

    sendPayload(payload)

def subscribeToLIDARSpecifics(unsubscribe=False):
    time.sleep(0.1)

    payload = [
        0x01,
        0x02,
        0xA5,
        0x00,
        0x00 if unsubscribe else 0x01
    ]

    sendPayload(payload)

"""
Listens for sensor data from a robot and adds a method to be called when data is received
:param robotAddress: The address of the robot
:param sensorAddress: The address of the sensor
:param method: The method to call when data is received
"""
def listen(robotAddress, sensorAddress, method):
    subscribe(robotAddress, sensorAddress)

    listenWithoutSubscribing(robotAddress, sensorAddress, method)

"""
Adds a method to be called when sensor data is received without subscribing to the sensor
:param robotAddress: The address of the robot
:param sensorAddress: The address of the sensor
:param method: The method to call when data is received
"""
def listenWithoutSubscribing(robotAddress, sensorAddress, method):
    global listeners

    if robotAddress not in listeners:
        listeners[robotAddress] = {}
    if sensorAddress not in listeners[robotAddress]:
        listeners[robotAddress][sensorAddress] = []
    listeners[robotAddress][sensorAddress].append(method)

def start_websocket():
    ws = websocket.WebSocketApp(
        WEBSOCKET_URL,
        on_message=on_message,
        on_error=on_error,
        on_close=on_close,
        on_ping=on_message
    )

    ws.on_open = on_open

    # Run the WebSocket connection in a separate thread
    wst = threading.Thread(target=ws.run_forever)
    wst.daemon = True  # This ensures the thread doesn't block the main program from exiting
    wst.start()

"""
Update loop
"""
def update():
    # normalize sensors
    normal_sensors = []
    for sensor in sensor_values:
        dist = sensor_values[sensor][0]
        normal_sensors.append(dist / 255)

    # use model to predict action
    values = neural_network.predict(np.array(normal_sensors).reshape(1, -1))[0]

    values[0] = (values[0] - 0.5) * robot.ROTATION_SPEED
    values[1] = values[1] * robot.SPEED * 2

    # convert first double to a byte
    first_double = struct.pack('>d', values[0])
    second_double = struct.pack('>d', values[1])

    # send the action to the server
    payload = [
        0x01,
        0x02,
        0xB6,
    ]

    for i in range(8):
        payload.append(first_double[i])
    for i in range(8):
        payload.append(second_double[i])

    sendPayload(payload)

    pass


"""
Main loop.
"""
def loop():
    while True:
        update()
        threading.Event().wait(0.1)

def start():
    global neural_network
    
    neural_network = NeuralNetwork([3, 4, 3, 2])
    neural_network.load("model483.npy")

    start_websocket()

    threading.Thread(target=loop).start()

    while True:
        time.sleep(1)
        pass

if __name__ == "__main__":
    start()