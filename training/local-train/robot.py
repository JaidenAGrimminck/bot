from math import cos, sin, pi
from environment import scale, view_position
import environment
import sensor
import gui
import utils
import numpy as np
import agent

starting_pos = (938,80)
starting_rot = pi

actions = [
    "LEFT",
    "STRAIGHT",
    "RIGHT"
]

ROTATION_SPEED = pi / 100
SPEED = 5

class Robot:
    def __init__(self, x, y, r):
        self.agent = agent.Agent(3, 3)

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
        for i in range(len(self.sensors)):
            self.sensor_values[i] = self.sensors[i].getDistance()

    def step(self):
        # update sensors
        self.update_sensors()

        if self.inCollision:
            return

        # normalize sensors
        normal_sensors = []
        for i in range(len(self.sensor_values)):
            normal_sensors.append(
                self.sensor_values[i] / sensor.Sensor.MAX_DIST
            )

        # use model to predict action
        values = self.agent.predict(normal_sensors)
        
        # do probabilistic check of the action
        action = np.random.choice(actions, p=values[0])
        
        # perform action
        if action == "LEFT":
            self.rotation -= ROTATION_SPEED
        elif action == "RIGHT":
            self.rotation += ROTATION_SPEED

        # move forward
        newPosX, newPosY = rotatePoint(0, SPEED, self.rotation)
        newPosX += self.position[0]
        newPosY += self.position[1]
        
        # update position
        self.position = (newPosX, newPosY)


    def reset(self):
        self.position = starting_pos
        self.rotation = starting_rot + 0.000001

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
    