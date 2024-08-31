import asyncio
import threading
from websockets.asyncio.client import connect

class ConnectionService:
    def __init__(self, ip, port):
        self.ip = ip
        self.port = port

        self.websocket = None

        self.loop = asyncio.get_event_loop()
        self.read_loop = asyncio.get_event_loop()
        # start the connection
        self.loop.run_until_complete(self.init_connection())

    """
    This function is used to continue reading from the websocket
    """
    async def continue_reading(self, websocket):
        while True:
            response = await websocket.recv()

    """
    This function is used to initialize the connection
    """
    async def init_connection(self):
        uri = f"ws://{self.ip}:{self.port}"
        async with connect(uri) as websocket:
            msg = [
                0xFF,
                0x03,
                0x00
            ]

            await websocket.send(bytes(msg))
            response = await websocket.recv()

            if response == bytes([0xFF, 0x00]):
                print("successfully connected!")
            else:
                print("connection failed!")
            
            self.websocket = websocket
            
            # create new thread to continue reading from the websocket
            