import gui
import math
from utils import hypot

class Box2d:
    def __init__(self, x, y, width, height):
        self.x = x
        self.y = y
        self.width = width
        self.height = height

        self.tags = {}

    def getVerts(self):
        return [
            (self.x, self.y), # top left
            (self.x + self.width, self.y), # top right
            (self.x + self.width, self.y + self.height), #bot right
            (self.x, self.y + self.height) # bot left
        ]

    def signed_dist(self,x,y):
        x_min, y_min = (self.x,self.y)
        x_max, y_max = (self.x + self.width,self.y + self.height)

        # todo: implement stuff.
        if (x < x_min):
            if (y < y_min): return hypot(x_min-x, y_min-y)
            if (y <= y_max): return x_min - x
            return hypot(x_min-x, y_max-y)
        elif (x <= x_max):
            if (y < y_min): return y_min - y
            if (y <= y_max): return 0
            return y - y_max
        else:
            if (y < y_min): return hypot(x_max-x, y_min-y)
            if (y <= y_max): return x - x_max
            return hypot(x_max-x, y_max-y)

    def raycastDist(self, pos, to):
        if not self.lineIntersects(pos, to):
            return math.inf
        
        dir = (
            to[0] - pos[0],
            to[1] - pos[1]
        )

        thresh = 1
        rfac = 1
        maxn = 100

        t = 0
        r = self.signed_dist(pos[0], pos[1]) * rfac

        t += r

        n = 0

        while r > thresh and n < maxn:
            n += 1

            dir_magn = math.sqrt(dir[0] * dir[0] + dir[1] * dir[1])

            newpos = (
                pos[0] + ((dir[0] / dir_magn) * r),
                pos[1] + ((dir[1] / dir_magn) * r)
            )

            r = self.signed_dist(newpos[0], newpos[1]) * rfac

            t += r

            pos = newpos

        if n >= maxn:
            return math.inf
        
        return t

    def intersectsRay(self, p1, p2):
        dir = (p2[0] - p1[0], p2[1] - p1[0])

        if dir[0] == 0:
            dir = (0.000001, dir[1])
        if dir[1] == 0:
            dir = (dir[0], 0.000001)

        t1 = (self.x - p1[0]) / dir[0]
        t2 = (self.x + self.width - p1[0]) / dir[0]
        t3 = (self.y - p1[1]) / dir[1]
        t4 = (self.y + self.height - p1[1]) / dir[1]

        tmin = max(min(t1, t2), min(t3, t4))
        tmax = min(max(t1, t2), max(t3, t4))
        return not (tmax < 0 or tmin > tmax)
    
    def lineIntersects(self, start, end):
        if end[0] == start[0]:
            return (start[0] >= self.x and start[0] <= self.x + self.width) and not (start[1] > self.y + self.height or end[1] < self.y)

        slope = (end[1] - start[1]) / (end[0] - start[0])
        yIntercept = start[1] - slope * start[0]

        if (start[1] < self.y and end[1] > self.y):
            x = (self.y - yIntercept) / slope
            if (x >= self.x and x <= self.x + self.width): return True
            

        if (start[1] > self.y + self.height and end[1] < self.y + self.height):
            x = (self.y + self.height - yIntercept) / slope
            if (x >= self.x and x <= self.x + self.width): return True;


        if (start[0] < self.x and end[0] > self.x):
            y = slope * self.x + yIntercept
            if (y >= self.y and y <= self.y + self.height): return True


        if (start[0] > self.x + self.width and end[0] < self.x + self.width):
            y = slope * (self.x + self.width) + yIntercept
            if (y >= self.y and y <= self.y + self.height): return True

        return False
    

env = []
rewards = [
    # (92, 616),
    # (116, 212),
    # (316, 86),
    # (482, 218),
    # (460, 556),
    # (434, 824),
    # (576, 710),
    # (644, 228),
    # (838, 114),
    # (936, 308),
    # (872, 624),
    # (974, 878),
    (0,0)
]

starting_rots = [
    180,
    90
]

reward_thresh = 100

view_position = (0,0)
scale = 0.5

MAP_WIDTH = 0
MAP_HEIGHT = 0

def loadObstacles(path, boxSize):
    env = []

    map_w = 0
    map_h = 0

    # file with 0s and 1s
    with open(path, 'r') as f:
        lines = f.readlines()

        map_h = len(lines)
        
        for i in range(len(lines)):
            if map_w == 0:
                map_w = len(lines[i])
            
            for j in range(len(lines[i])):
                if lines[i][j] == '1':
                    env.append(Box2d(j * boxSize, i * boxSize, 1 * boxSize, 1 * boxSize))
    
    return (env, map_w * boxSize, map_h * boxSize)

def setup():
    global env, MAP_WIDTH, MAP_HEIGHT

    env, MAP_WIDTH, MAP_HEIGHT = loadObstacles("map3.txt", 10)

    env.append(
        Box2d(-10,-10,10,MAP_HEIGHT + 10)
    )
    env.append(
        Box2d(0,-10,MAP_WIDTH,10)
    )
    env.append(
        Box2d(MAP_WIDTH,-10,10, MAP_HEIGHT + 20)
    )
    env.append(
        Box2d(-10,MAP_HEIGHT,MAP_WIDTH + 10, 10)
    )

def drawObstacles(pygame, canvas):
    global view_position

    for box in env:
        if box.x < view_position[0] - box.width or box.x > view_position[0] + (gui.WIDTH / scale):
            continue
        if box.y < view_position[1] - box.height or box.y > view_position[1] + (gui.HEIGHT / scale):
            continue
        
        a = 0

        if "collide" in box.tags.keys():
            a = 255

        pygame.draw.rect(canvas, (255,a,0), (scale * (box.x - view_position[0]), scale * (box.y - view_position[1]), scale * box.width, scale * box.height))
    
    for reward in rewards:
        p = (scale * (reward[0] - view_position[0] - 5), scale * (reward[1] - view_position[1] - 5))
        pygame.draw.rect(canvas, (0,255,0), (p[0], p[1], scale * 10, scale * 10))

        pygame.draw.circle(canvas, (0,255,0), p, int(reward_thresh * scale), width=1)