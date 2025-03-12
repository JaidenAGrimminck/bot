import sys
sys.path.append('../../../topica')

from topica import TopicaServer
from gamepad import Gamepad
import numpy as np
import time

pad = Gamepad()
server = TopicaServer("172.16.130.160", 5443, verbose=True)

tuning = { # neutral values
    'left_x': {
        'min': 0,
        'neutral': 128,
        'max': 255
    },
    'left_y': {
        'min': 0,
        'neutral': 128,
        'max': 255
    },
    'right_x': {
        'min': 0,
        'neutral': 128,
        'max': 255
    },
    'right_y': {
        'min': 0,
        'neutral': 128,
        'max': 255
    }
}

while True:
    pad.read_gamepad()

    if pad.changed:
    #if server.open:
        raw_left_x = pad.get_analogL_x()
        raw_left_y = pad.get_analogL_y()

        raw_right_x = pad.get_analogR_x()
        raw_right_y = pad.get_analogR_y()

        # raw_left_x = 128
        # raw_left_y = 128
        
        # raw_right_x = 128
        # raw_right_y = 128

        left_x = (raw_left_x - tuning['left_x']['neutral']) / (tuning['left_x']['neutral'] - tuning['left_x']['min'])
        left_y = (raw_left_y - tuning['left_y']['neutral']) / (tuning['left_y']['neutral'] - tuning['left_y']['min'])

        right_x = (raw_right_x - tuning['right_x']['neutral']) / (tuning['right_x']['neutral'] - tuning['right_x']['min'])
        right_y = (raw_right_y - tuning['right_y']['neutral']) / (tuning['right_y']['neutral'] - tuning['right_y']['min'])

        #print("left_x: {0:3} left_y: {1:3} right_x: {2:3} right_y: {3:3}".format(left_x, left_y, right_x, right_y))
        server.set("/gamepad1/leftX", left_x)
        server.set("/gamepad1/leftY", left_y)
        server.set("/gamepad1/rightX", right_x)
        server.set("/gamepad1/rightY", right_y)
        server.set("/gamepad1/timestamp", np.int64(round(time.time() * 1000)))

        time.sleep(0.05)