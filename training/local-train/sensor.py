import robot
from math import sin, cos
import environment
from environment import scale, view_position

class Sensor:
    MAX_DIST = 255 / 10 * 5 # 5 = 10 cm, so 255 cm max

    def __init__(self, relativePosition, relativeRotation, robotParent):
        self.relativePosition = relativePosition
        self.rotation = relativeRotation
        self.parent = robotParent

    def getPosition(self):
        x,y = robot.rotatePoint(self.relativePosition[0], self.relativePosition[1], self.parent.rotation)

        return (
            x + self.parent.position[0],
            y + self.parent.position[1]
        )

    def draw(self, pygame, canvas):
        # draw a tick out
        pos = self.getPosition()
        pos = ((pos[0] - view_position[0]) * scale, (pos[1] - view_position[1]) * scale)

        rp = robot.rotatePoint(0, self.getDistance(), self.rotation + self.parent.rotation)
        rp = ((rp[0] - view_position[0]) * scale, (rp[1] - view_position[0]) * scale)

        pygame.draw.line(canvas, (255,0,255), pos, (rp[0] + pos[0], rp[1] + pos[1]))

        

    
    def getDistance(self):
        x,y = self.getPosition()

        x2,y2 = robot.rotatePoint(0, Sensor.MAX_DIST, self.rotation + self.parent.rotation)
        x2 += x
        y2 += y

        def tooFar(box: environment.Box2d):
            return box.signed_dist(x,y) <= Sensor.MAX_DIST

        env = filter(tooFar, environment.env)

        closestDistance = Sensor.MAX_DIST
        closestObject = None

        for box in env:
            if box.lineIntersects((x,y), (x2,y2)):
                #box.tags["collide"] = True

                d = box.raycastDist((x,y), (x2,y2))
                if d < closestDistance:
                    closestDistance = d
                    closestObject = box

        if closestObject != None:
            #closestObject.tags["collide"] = True
            return closestDistance
        else:
            return Sensor.MAX_DIST
        

