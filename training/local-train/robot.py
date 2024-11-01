from math import cos, sin, pi, floor
from environment import scale, view_position
import environment
import sensor
import gui
import utils
import numpy as np
import time
import run
import ws
import struct
from neural_network import NeuralNetwork

starting_pos = (80,938)
starting_rot = pi

starting_checkpoint = -1

times = []

actions = [
    "LEFT",
    "STRAIGHT",
    "RIGHT"
]

ROTATION_SPEED = 1# pi / 15
SPEED = 2

class Robot:
    def __init__(self, x, y, r, model=None, send_moves=False):
        self.agent = NeuralNetwork([3, 4, 3, 2])
        self.preloaded = False
        self.send_moves = send_moves
        if model is not None:
            self.agent.load(model)
            print("loaded model " + model)
            self.preloaded = True
        else:
            self.agent.randomize()

        if send_moves:
            ws.start_websocket()

        self.position = (x, y)

        self.size = (4 * 5, 6 * 5) # 5 = 10cm, so width of 40 cm and length of 60 cm

        self.rotation = r # in radians

        self.manual_control = True

        self.sensors = [
            sensor.Sensor(
                (0, self.size[1] / 2),
                0,
                self
            ),
            sensor.Sensor(
                (self.size[0] / 2, self.size[1] / 2),
                -pi / 4,
                self
            ),
            sensor.Sensor(
                (self.size[0] / -2, self.size[1] / 2),
                pi / 4,
                self
            ),
        ]

        self.sensor_values = []
        self.inCollision = False
        self.hasCrashed = False
        self.finished = False

        for i in range(len(self.sensors)):
            self.sensor_values.append(0)

        self.points = 0
        self.checkpoint = 0

    def getRelativeVerticies(self):
        # we're pointing DOWNWARDS for this
        return [
            (self.size[0] / 2, self.size[1] / 2), # top left
            (self.size[0] / -2, self.size[1] / 2), # top right
            (self.size[0] / -2, self.size[1] / -2), # bot right
            (self.size[0] / 2, self.size[1] / -2), # bot left
        ]
    
    def getVerticies(self):
        verts = self.getRelativeVerticies()

        for i in range(len(verts)):
            vert = verts[i]

            verts[i] = (
                vert[0] * cos(self.rotation) - vert[1] * sin(self.rotation),
                vert[1] * cos(self.rotation) + vert[0] * sin(self.rotation)
            )
        
        return verts
    
    def getAbsoluteVerticies(self):
        verts = self.getVerticies()

        for i in range(len(verts)):
            vert = verts[i]

            verts[i] = (
                vert[0] + self.position[0],
                vert[1] + self.position[1]
            )
        
        return verts
    
    def colliding(self):
        for box in environment.env:
            av = self.getAbsoluteVerticies()
            bv = box.getVerts()
            if utils.separatingAxisTheorem(av, bv) and utils.separatingAxisTheorem(bv, av):
                return True
        return False
            
    def update_sensors(self):
        self.inCollision = self.colliding()
        
        self.hasCrashed = self.inCollision
        for i in range(len(self.sensors)):
            self.sensor_values[i] = self.sensors[i].getDistance()

    def step(self):
        a= time.time()

        if self.inCollision:
            return

        # update sensors
        self.update_sensors()
        b = time.time()

        times.append(b - a)

        if self.inCollision:
            return

        self.checkAndUpdateScore()

        # normalize sensors
        normal_sensors = []
        for i in range(len(self.sensor_values)):
            normal_sensors.append(
                self.sensor_values[i] / sensor.Sensor.MAX_DIST
            )

        # use model to predict action
        values = self.agent.predict(np.array(normal_sensors).reshape(1, -1))[0]

        # print(values)
        # # do probabilistic check of the action
        # action = np.random.choice(actions, p=values[0])
        
        # # perform action
        # if action == "LEFT":
        #     self.rotation -= ROTATION_SPEED
        # elif action == "RIGHT":
        #     self.rotation += ROTATION_SPEED

        speed = values[1] * SPEED
        self.rotation += (values[0] - 0.5) * ROTATION_SPEED


        if (self.send_moves):
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

            ws.sendPayload(payload)

        # move forward
        newPosX, newPosY = rotatePoint(0, speed, self.rotation)
        newPosX += self.position[0]
        newPosY += self.position[1]
        
        # update position
        self.position = (newPosX, newPosY)

    def checkAndUpdateScore(self):
        nextCheckpoint = environment.rewards[self.checkpoint]

        if utils.distance(self.position, nextCheckpoint) < environment.reward_thresh:
            self.points += 100
            self.checkpoint += 1
            print("i reached a checkpoint! " + str(self.checkpoint))
        
        if self.checkpoint == len(environment.rewards):
            self.points += 10000000
            self.inCollision = True
            self.finished = True

            random_num = floor(np.random.rand() * 1000)

            print("I made it!!!")

            # save model
            if run.do_evolution:
                self.agent.save("model" + str(random_num) + ".npy")
                print("saved my model as " + "model" + str(random_num) + ".npy")

    def getScore(self):
        if (self.finished):
            return 10000000
        
        p1 = None
        if self.checkpoint == 0:
            p1 = starting_pos
        else:
            p1 = environment.rewards[self.checkpoint - 1]
        
        p2 = environment.rewards[self.checkpoint]

        maxDist = utils.distance(p1, p2)
        currentDist = utils.distance(self.position, p2) - (environment.reward_thresh / 2)

        discount = 0

        if self.hasCrashed:
            discount = 20

        return ((self.checkpoint - utils.clamp(starting_checkpoint, 0, 10000) + (1 - (currentDist / maxDist))) * 100) - discount

    def reset(self):
        p = starting_pos
        if starting_checkpoint > -1:
            p = environment.rewards[starting_checkpoint]

        self.position = (int(p[0]), int(p[1]))
        self.rotation = float(starting_rot) + 0.000001
        self.inCollision = False
        self.finished = False
        self.checkpoint = int(starting_checkpoint)

    def draw(self, pygame, canvas):
        speed = 5
        if self.manual_control:
            if (gui.keys["LEFT"]):
                self.position = (self.position[0] - speed, self.position[1])
            if (gui.keys["RIGHT"]):
                self.position = (self.position[0] + speed, self.position[1])
            if (gui.keys["UP"]):
                self.position = (self.position[0], self.position[1] - speed)
            if (gui.keys["DOWN"]):
                self.position = (self.position[0], self.position[1] + speed)
            if (gui.keys["Q"]):
                self.rotation -= pi / 100
            if (gui.keys["E"]):
                self.rotation += pi / 100

        verts = self.getVerticies()
        verts.append(verts[0]) # loop it back

        color = (0,0,255)
        if self.colliding():
            color = (255,100,0)

        for i in range(len(verts) - 1):
            x1 = verts[i][0] + self.position[0]
            y1 = verts[i][1] + self.position[1]

            x2 = verts[i + 1][0] + self.position[0]
            y2 = verts[i + 1][1] + self.position[1]

            x1 *= scale
            y1 *= scale
            x2 *= scale
            y2 *= scale

            x1 -= view_position[0]
            x2 -= view_position[0]
            y1 -= view_position[1]
            y2 -= view_position[1]

            pygame.draw.line(canvas, color, (x1, y1), (x2, y2))

    
        # draw direction tick
        x_tick1, y_tick1 = rotatePoint(0, (self.size[1] / 2) - 10, self.rotation)
        x_tick2, y_tick2 = rotatePoint(0, (self.size[1] / 2) + 10, self.rotation)

        x_tick1 += self.position[0] - view_position[0]
        x_tick2 += self.position[0] - view_position[0]
        y_tick1 += self.position[1] - view_position[1]
        y_tick2 += self.position[1] - view_position[1]

        pygame.draw.line(canvas, (0,0,255), (x_tick1 * scale, y_tick1 * scale), (x_tick2 * scale, y_tick2 * scale))

        for sensor in self.sensors:
            sensor.draw(pygame, canvas)

        pass

def rotatePoint(x, y, r):
    return (
        x * cos(r) - y * sin(r),
        y * cos(r) + x * sin(r)
    )
    