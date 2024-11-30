import websocket
import time
import struct
import threading
import numpy as np

neural_network = None

sensors = {}
listeners = {}

events = {
    "onConnection": [],
    "onDisconnect": []
}

subscriber = {
    "speaker": 0x01,
    "listener": 0x02,
    "passive": 0x03
}

this_type = 0x03

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

"""
Called when the connection is successful
"""
def onConnection():
    print('Successfully registered and connected.')

    for event in events["onConnection"]:
        event()

    pass

"""
Called when the connection is lost
"""
def onDisconnect():
    print('disconnected')

    for event in events["onDisconnect"]:
        event()

    pass

"""
Called when an error occurs
:param ws: The websocket
:param error: The error
"""
def on_error(ws, error):
    print(f"Websocket Error: {error}")

"""
Called when the connection is closed
:param ws: The websocket
:param close_status_code: The status code of the close
"""
def on_close(ws, close_status_code, close_msg):
    print(f"WebSocket closed with message: {close_msg}, code: {close_status_code}")

    if close_status_code == 1006:
        print("Attempting to reconnect...")
        start_websocket()

    if close_status_code == 1000:
        onDisconnect()

"""
Called when the websocket is opened
:param ws: The websocket
"""
def on_open(ws):
    global socket, this_type

    def run(*args):
        global socket, this_type

        initial_message = [0xFF,
                           this_type,
                           0x00]

        ws.send(initial_message)

        print('Sent initial handshake message to connect.')

        if this_type == subscriber["listener"]:
            print('Connecting as listener...')
        elif this_type == subscriber["speaker"]:
            print('Connecting as speaker...')
        elif this_type == subscriber["passive"]:
            print('Connecting as passive...')

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
Sends a data update to a sensor.
:param payload
"""
def updateWSSensor(sensor_addr, payload):
    pre = [
        subscriber["speaker"], # speaker
        0x01, # ws sensor update
        sensor_addr, # sensor address
    ]

    if this_type == subscriber["speaker"]:
        # remove first byte
        pre.pop(0)
    elif this_type == subscriber["listener"]:
        # throw error
        print("Cannot update sensor as listener!")
        return

    pre.extend(payload)

    sendPayload(pre)

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

    if this_type == subscriber["listener"]:
        # remove first byte
        payload.pop(0)
    elif this_type == subscriber["speaker"]:
        # throw error
        print("Cannot subscribe as speaker")
        return

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


"""
Start the websocket
"""
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
Adds a listener to an event
:param event: The event to listen to, e.g. "onConnection"/"onDisconnect"
:param listener: The listener to add
"""
def addListener(event, listener):
    if event not in events:
        print(f"Event {event} does not exist.")
        return
    
    events[event].append(listener)

"""
Main loop.
"""
def loop():
    while True:
        threading.Event().wait(0.1)

""""
Starts the websocket and keeps the program running.
"""
def start(gtype=subscriber["passive"]):
    global this_type
    this_type = gtype
    start_websocket()

    threading.Thread(target=loop).start()

    while True:
        time.sleep(1)
        pass

if __name__ == "__main__":
    start()