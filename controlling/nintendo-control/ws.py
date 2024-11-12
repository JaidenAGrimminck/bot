import websocket
import time
import struct
import threading
import numpy as np

listeners = {}

socket = None

WEBSOCKET_URL = "ws://localhost:8080"

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

    # don't need messages, since we're a writer.


"""
Called when the connection is successful
"""
def onConnection():
    print('Successfully registered and connected.')

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
                           0x01, # speaker byte
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

"""
Listens for sensor data from a robot and adds a method to be called when data is received
:param robotAddress: The address of the robot
:param sensorAddress: The address of the sensor
:param method: The method to call when data is received
"""
def listen(robotAddress, sensorAddress, method):
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
def sendJoystickValues(x, y):
    # convert first double to a byte
    first_double = struct.pack('>d', x)
    second_double = struct.pack('>d', y)

    # send the action to the server
    payload = [
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
        threading.Event().wait(0.1)

def start():
    start_websocket()

    threading.Thread(target=loop).start()

    while True:
        time.sleep(1)
        pass

if __name__ == "__main__":
    start()