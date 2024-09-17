import websocket
import time
import struct
import threading
import sensor

WEBSOCKET_URL = "ws://localhost:8080"

timeSinceLastHeartbeat = 0

wscon = None

sensors = {}

listeners = {}

onFinishMethod = lambda: None

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
        print("heartbeat")
        # pong
        ws.send([0xFF, 0x01])
        return

    try:
        if ''.join(message).startswith("-18"):
            print("Stack trace: " + message[4:])
            return
    except:
        pass

    key = message[0]

    if key == 0xC0:
        datatype = message[1]
        if datatype == 0x01:
            address = message[2]
            n_values = message[3]

            for i in range(0, n_values, 1):
                value = []
                for j in range(0, 8, 1):
                    value.append(message[4 + j + (i * 8)])

                # value is a double from java, so we need to convert it to a python float
                value = struct.unpack('>d', bytes(value))[0]

                if address in sensors:
                    sensors[address].update_value(i, value)
                else:
                    sensors[address] = sensor.Sensor(address, n_values)
                    sensors[address].update_value(i, value)

                if address in listeners:
                    for listener in listeners[address]:
                        listener(sensors[address].get_values())

def on_error(ws, error):
    print(f"Error: {error}")

def on_close(ws, close_status_code, close_msg):
    print(f"WebSocket closed with message: {close_msg}, code: {close_status_code}")

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


# sends a payload of bytes to the websocket
def sendPayload(payload: []):
    if wscon == None:
        print("Websocket not connected")
        return

    wscon.send(payload)


# sends a one-time request to get sensor info from the server
def getSensorInfo(address, raw):
    raw_bt = 0x00

    if raw:
        raw_bt = 0x01

    payload = [
        0x02,
        0x01,
        address,
        raw_bt
    ]

    sendPayload(payload)

# sends a request to subscribe to a sensor
def subscribe(address, unsubscribe=False):
    subscribe_bit = 0x01

    if unsubscribe:
        subscribe_bit = 0x00

    payload = [
        0x02, # listen
        0x11, # subscribe
        address, # address of sensor
        subscribe_bit # subscribe
    ]

    sendPayload(payload)

def listen(address, method):
    subscribe(address)

    if address not in listeners:
        listeners[address] = []

    listeners[address].append(method)


ran = False

def onConfirmedConnection():
    global ran
    if ran:
        return

    ran = True

    print("subscribing to all sensors")

    # subscribe to the distance sensors
    for i in range(0x01, 0x09):
        subscribe(i)
