import websocket
import time
import struct
import threading
import numpy as np

"""
public static final byte BYTE_TYPE = 0x01;
public static final byte SHORT_TYPE = 0x02;
public static final byte INT_TYPE = 0x03;
public static final byte LONG_TYPE = 0x04;
public static final byte FLOAT_TYPE = 0x05;
public static final byte DOUBLE_TYPE = 0x06;
public static final byte STRING_TYPE = 0x07;
public static final byte BOOLEAN_TYPE = 0x08;
public static final byte CUSTOM_TYPE = 0x09;
"""

"""
A connection to the Topica server hosted on the robot.
"""
class TopicaServer:
    GET_METHOD = 0b0001
    SET_METHOD = 0b0010
    SUBSCRIBE_METHOD = 0b0011

    """
    Initialize a connection to the Topica server.
    :param host: The host of the Topica server.
    :param port: The port of the Topica server.
    :param verbose: Whether to print connection status.
    """
    def __init__(self, host, port, verbose=True, reconnect=True, reconnectTimeout=5):
        self.host = host
        self.port = port
        self.url = f"ws://{host}:{port}"
        self.ws = websocket.WebSocketApp(
            self.url, 
            on_message=self.on_message,
            on_error=self.on_error,
            on_close=self.on_close,
            on_ping=self.on_message,
            on_open=self.on_open
        )

        self.verbose = verbose

        self.reconnect = reconnect
        self.reconnectionTimeout = reconnectTimeout

        self.get_callbacks = {}
        self.subscribe_callbacks = {}

        self.state = {}

        self.internalEvents = {
            "open": [],
            "close": [],
            "error": [],
            "reconnect": []
        }

        self.connected_once = False

        self.wst = threading.Thread(target=self.ws.run_forever)
        self.wst.daemon = True
        self.wst.start()

        threading.Thread(target=self.wst_update).start()

        self.open = False

    """
    Internal method to keep the websocket connection alive. Ignore this method.
    """
    def wst_update(self):
        while True:
            threading.Event().wait(0.1)

            if self.reconnect and not self.open:
                # do timeout
                threading.Event().wait(self.reconnectionTimeout)

                if self.open:
                    continue

                # figure out if the server is still alive
                self.ws.close()

                self.ws = websocket.WebSocketApp(
                    self.url, 
                    on_message=self.on_message,
                    on_error=self.on_error,
                    on_close=self.on_close,
                    on_ping=self.on_message,
                    on_open=self.on_open
                )

                self.wst = threading.Thread(target=self.ws.run_forever)
                self.wst.daemon = True
                self.wst.start()

                if self.verbose:
                    print("Reconnecting to host Topica server...")

    """
    Called when a message is received from the Topica server.
    :param ws: The websocket connection.
    :param message: The message received.
    """
    def on_message(self, ws, message):
        if (len(message) <= 2):
            return

        first_byte = message[0]
        method = first_byte >> 4
        path_len = (((first_byte & 0b00001111) << 8) | (message[1]))
        
        if method == 0b1000:
            path = message[2:2+path_len].decode('utf-8')

            data_type = message[2+path_len]
            data_len = struct.unpack('>I', message[3+path_len:7+path_len])[0]
            data = message[7+path_len:7+path_len+data_len]

            value = None

            if data_type == 0x01:
                value = data
            elif data_type == 0x02:
                value = struct.unpack('>h', data)[0]
            elif data_type == 0x03:
                value = struct.unpack('>i', data)[0]
            elif data_type == 0x04:
                value = struct.unpack('>q', data)[0]
            elif data_type == 0x05:
                value = struct.unpack('>f', data)[0]
            elif data_type == 0x06:
                value = struct.unpack('>d', data)[0]
            elif data_type == 0x07:
                value = data.decode('utf-8')
            elif data_type == 0x08:
                value = struct.unpack('>?', data)[0]
            else:
                raise ValueError("Unsupported data type")

            # check if a callback exists
            if path in self.get_callbacks:
                self.get_callbacks[path](value)
            
                # remove the callback
                del self.get_callbacks[path]

            # go through subscriptions
            for topic in self.subscribe_callbacks:
                if path.startswith(topic):
                    self.subscribe_callbacks[topic](value)

            self.state[path] = value

        pass
    
    """
    Called when an error occurs with the Topica server.
    :param ws: The websocket connection.
    :param error: The error that occurred
    """
    def on_error(self, ws, error):
        if self.verbose:
            print("Error occurred with host Topica server.")
            # output error
            print(error)
        
        for callback in self.internalEvents["error"]:
            callback()

        pass

    """
    Called when the connection to the Topica server is closed.
    :param ws: The websocket connection
    """
    def on_close(self, ws, close_status_code, close_msg):
        if self.verbose:
            print("Connection closed to host Topica server.")

        self.open = False

        for callback in self.internalEvents["close"]:
            callback()

        pass
    
    """
    Called when a ping is received from the Topica server.
    :param ws: The websocket connection.
    :param data: The data received from the ping.
    """
    def on_ping(self, ws, data):
        pass
    
    """
    Called when the connection to the Topica server is opened.
    :param ws: The websocket connection.
    """
    def on_open(self, ws):
        if self.verbose:
            print("Connection opened to host Topica server!")

        self.open = True

        if not self.connected_once:
            for callback in self.internalEvents["open"]:
                callback()
        else:
            for callback in self.internalEvents["reconnect"]:
                callback()

        self.connected_once = True

        pass

    """
    Encode a path to a topica readable.
    :param method: The method to use.
    :param topic: The topic to encode.
    """
    def encodePath(self, method, topic):
        # path length
        path = topic.encode('utf-8')
        path_len = len(path)

        # encode path_len to 2 bytes
        path_len = min(path_len, 0b0000111111111111)
        # make sure to leave the 4 msb of the first byte for the method
        path_len_first_byte = path_len >> 8
        path_len_second_byte = path_len & 0b11111111
        
        # msb encode the first byte with the method
        first_byte = (method << 4) | path_len_first_byte
        
        payload = [
            first_byte,
            path_len_second_byte
        ]

        for byte in path:
            payload.append(byte)

        return payload

    """
    Get a value from a topic.
    :param topic: The topic to get.
    :param onResponse: The callback to call when the response is received.
    """
    def get(self, topic, onResponse):
        if not self.open:
            return

        self.get_callbacks[topic] = onResponse
        self.ws.send(self.encodePath(self.GET_METHOD, topic))
        pass

    """
    Subscribe to a topic.
    :param topic: The topic to subscribe to.
    :param interval_ms: The interval in milliseconds to receive updates.
    :param callback: The callback to call when an update is received.
    """
    def subscribe(self, topic, interval_ms, callback):
        if not self.open:
            return

        self.subscribe_callbacks[topic] = callback

        prepayload = self.encodePath(self.SUBSCRIBE_METHOD, topic)

        # encode interval_ms to integer
        interval_bytes = struct.pack('>I', interval_ms)

        payload = prepayload + list(interval_bytes)
        self.ws.send(payload)

        pass
    
    """
    Set a value to a topic.
    :param topic: The topic to set.
    :param value: The value to set.
    """
    def set(self, topic, value):
        if not self.open:
            return

        prepayload = self.encodePath(self.SET_METHOD, topic)

        # get type of topic
        type_byte = 0x00

        raw_data = []

        if type(value) == bytearray:
            type_byte = 0x01
            raw_data = value
        elif type(value) == np.int16:
            type_byte = 0x02
            raw_data = struct.pack('>h', value)
        elif type(value) == np.int32 or type(value) == int:
            type_byte = 0x03
            raw_data = struct.pack('>i', value)
        elif type(value) == np.int64:
            type_byte = 0x04
            raw_data = struct.pack('>q', value)
        elif type(value) == np.float32:
            type_byte = 0x05
            raw_data = struct.pack('>f', value)
        elif type(value) == np.float64 or type(value) == float:
            type_byte = 0x06
            raw_data = struct.pack('>d', value)
        elif type(value) == str:
            type_byte = 0x07
            raw_data = value.encode('utf-8')
        elif type(value) == bool:
            type_byte = 0x08
            raw_data = struct.pack('>?', value)
        else:
            raise ValueError("Unsupported type for value")

        payload_length = struct.pack('>I', len(raw_data))

        payload = prepayload + [type_byte] + list(payload_length) + list(raw_data)

        self.ws.send(payload)
        pass

    """
    Add a callback to an (internal) event.
    :param event: The event to add the callback to. ["open", "close", "error", "reconnect"]. note: open is called only on the first connection, and reconnect is called on every reconnection.
    :param callback: The callback to add.
    """
    def onEvent(self, event, callback):
        if (self.internalEvents.get(event) == None):
            self.internalEvents[event] = []

        self.internalEvents[event].append(callback)

        if event == "open" and self.open:
            callback()


