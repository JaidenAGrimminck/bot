import websocket
import time
import struct
import threading
import sensor
import constants

WEBSOCKET_URL = "ws://localhost:8080"

timeSinceLastHeartbeat = 0

wscon = None

sensors = {}

listeners = {}

onFinishMethod = lambda: None
onDisconnectMethod = lambda: None
onGenerationFinishMethod = lambda scores: None

# This function will be called when a message is received from the server
def on_message(ws, message):
    global timeSinceLastHeartbeat

    if message[0] == 0xFF and message[1] == 0xFF:
        print('successful connection')
        onFinishMethod()
        onConfirmedConnection()
        return

    if message[0] == 0xFF and message[1] == 0x00:
        timeSinceLastHeartbeat = time.time()
        #print("heartbeat")
        # pong
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

    if key == 0xC0:
        datatype = message[1]
        if datatype == 0x01:
            robotAddress = message[2]
            sensorAddress = message[3]
            n_values = message[4]

            initial = 5

            for i in range(0, n_values, 1):
                value = []
                for j in range(0, 8, 1):
                    value.append(message[initial + j + (i * 8)])

                # value is a double from java, so we need to convert it to a python float
                value = struct.unpack('>d', bytes(value))[0]

                if robotAddress not in sensors:
                    sensors[robotAddress] = {}

                if sensorAddress in sensors[robotAddress]:
                    sensors[robotAddress][sensorAddress].update_value(i, value)
                else:
                    sensors[robotAddress][sensorAddress] = sensor.Sensor(sensorAddress, n_values)
                    sensors[robotAddress][sensorAddress].update_value(i, value)

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
    elif key == 0xC1:
        type = message[1]
        if type == 0x01: # received reset, got scores
            scores = []

            for i in range(constants.robotN):
                for j in range(8):
                    scores.append(message[2 + (i * 8) + j])

            onGenerationFinishMethod(scores)
    elif key == 0xC3: # generation end
        payload = message[1:]

        generation = payload[0:8]
        rawScores = payload[8:]

        generation = struct.unpack('>d', bytes(generation))[0]
        scores = []

        for i in range(0, len(rawScores), 8):
            score = struct.unpack('>d', bytes(rawScores[i:i+8]))[0]
            scores.append(score)

        print("finished generation " + str(generation))

        onGenerationFinishMethod(scores)




def setDisconnectMethod(method):
    global onDisconnectMethod
    onDisconnectMethod = method

def setGenerationFinishMethod(method=lambda scores: None):
    global onGenerationFinishMethod
    onGenerationFinishMethod = method

def on_error(ws, error):
    print(f"Error ?? : {error}")
    print(f"Last payload: {lastPayload}")

def on_close(ws, close_status_code, close_msg):
    print(f"WebSocket closed with message: {close_msg}, code: {close_status_code}")

    if close_status_code == 1006:
        print("Attempting to reconnect...")
        start_websocket()

    if close_status_code == 1000:
        onDisconnectMethod()

def on_open(ws):
    global wscon

    def run(*args):
        global wscon

        initial_message = [0xFF,
                           0x03,
                           0x00]

        ws.send(initial_message)
        print('Sent initial message to connect.')

        wscon = ws

    threading.Thread(target=run).start()

# Main function to initiate the WebSocket connection
def start_websocket(onFinish=lambda: None):
    global onFinishMethod

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

    onFinishMethod = onFinish

    # Keep the main thread alive
    while True:
        time.sleep(1)


lastPayload = []
# sends a payload of bytes to the websocket
def sendPayload(payload=[]):
    global lastPayload

    if wscon == None:
        print("Websocket not connected")
        return

    lastPayload = payload

    wscon.send(payload)


# sends a one-time request to get sensor info from the server
def getSensorInfo(robotAddress, sensorAddress, raw):
    raw_bt = 0x00

    if raw:
        raw_bt = 0x01

    payload = [
        0x02, # listen
        0x01, # get sensor info
        robotAddress,
        sensorAddress,
        raw_bt
    ]

    sendPayload(payload)

# sends a request to subscribe to a sensor
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

def listen(robotAddress, sensorAddress, method):
    subscribe(robotAddress, sensorAddress)

    listenWithoutSubscribing(robotAddress, sensorAddress, method)

def listenWithoutSubscribing(robotAddress, sensorAddress, method):
    global listeners

    if robotAddress not in listeners:
        listeners[robotAddress] = {}
    if sensorAddress not in listeners[robotAddress]:
        listeners[robotAddress][sensorAddress] = []
    listeners[robotAddress][sensorAddress].append(method)

# sends a message to the server to call a callable/listener
def send(listenerAddress, payload=None):
    if payload is None:
        payload = []

    payload = [
        0x01, # is a speaker
        0x02, # to a callable
        listenerAddress # address of the listener
    ] + payload

    sendPayload(payload)

ran = False

def onConfirmedConnection():
    global ran
    if ran:
        return

    ran = True
