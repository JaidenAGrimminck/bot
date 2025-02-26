import websocket
import time
import struct
import threading
import numpy as np

"""
A connection to the Topica server hosted on the robot.
"""
class TopicaServer:
    def __init__(self, host, port):
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

        self.wst = threading.Thread(target=self.ws.run_forever)
        self.wst.daemon = True
        self.wst.start()

    def on_message(self):
        pass

    def on_error(self):
        pass

    def on_close(self):
        pass

    def on_ping(self):
        pass
    
    def on_open(self):
        pass

    def get(topic, onResponse):
        pass

    def subscribe(topic, callback):
        pass
    
    def set(topic, value):
        pass


