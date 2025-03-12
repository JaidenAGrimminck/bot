import sys
sys.path.append('../topica')

from topica import TopicaServer

import os
import threading
import struct
import ydlidar
from time import sleep

PORT = "/dev/ttyUSB0"
laser = None
ports = None


server = TopicaServer("host", 5443)

def set_options():
    laser.setlidaropt(ydlidar.LidarPropSerialPort, port)
    laser.setlidaropt(ydlidar.LidarPropSerialBaudrate, 128000)
    laser.setlidaropt(ydlidar.LidarPropLidarType, ydlidar.TYPE_TOF)
    laser.setlidaropt(ydlidar.LidarPropDeviceType, ydlidar.YDLIDAR_TYPE_SERIAL)
    laser.setlidaropt(ydlidar.LidarPropScanFrequency, 10.0)
    laser.setlidaropt(ydlidar.LidarPropSampleRate, 20)
    laser.setlidaropt(ydlidar.LidarPropSingleChannel, True)

    laser.setlidaropt(ydlidar.LidarPropMaxAngle, 180.0)
    laser.setlidaropt(ydlidar.LidarPropMinAngle, -180.0)
    laser.setlidaropt(ydlidar.LidarPropMaxRange, 32.0)
    laser.setlidaropt(ydlidar.LidarPropMinRange, 0.01)

def run_laser():
    ret = laser.initialize()

    last_scan_recieved = 0

    if ret:
        ret = laser.turnOn()
        scan = ydlidar.LaserScan()
        while ret and ydlidar.os_isOk():
            r = laser.doProcessSimple(scan)
            if r:
                scan_time = scan.config.scan_time
                if scan_time == 0:
                    scan_time = 1.0
                
                last_scan_recieved = scan.stamp
                points = scan.points.size()
                ranges = 1.0 / scan_time
                
                payload = []

                payload.extend(struct.pack("i", int(0)))
                payload.extend(struct.pack("f", float(ranges)))
                payload.extend(struct.pack("i", int(points))) # should be I, but this makes it easier on me lol

                for point in scan.points:
                    payload.extend(struct.pack("f", float(point.angle)))
                    payload.extend(struct.pack("f", float(point.range * 25)))
                    payload.extend(struct.pack("f", float(point.intensity)))

                    #average += point.range
                
                if server.open:
                    server.set("/lidar/scan", payload)
            else:
                sleep(0.1)
        laser.turnOff()
    laser.disconnecting()

if __name__ == "__main__":
    ydlidar.os_init()
    laser = ydlidar.CYdLidar()
    ports = ydlidar.lidarPortList()

    port = "/dev/ydlidar"
    foundPort = False
    for key, value in ports.items():
        port = value
        if port == PORT:
            foundPort = True
            break

    if not foundPort:
        print("Port not found, attempting port: ", port)
    else:
        print("Connecting to port: ", port)
    
    set_options()

    server.onEvent("open", lambda: threading.Thread(target=run_laser).start())
    
    
    