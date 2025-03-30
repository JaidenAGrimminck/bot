import sys
sys.path.append('../nn')

import numpy as np
import environment as ev
import matplotlib as mpl
from math import pi
from constants import conversion, wheel_base, max_wheel_speed, mutation_rate
from robot import arcade_drive
from neural_network import RobotController


max_ray = conversion * 10 # 10 meters

class Agent:
    def __init__(self, location, rotation, model_path=None):
        self.location = location
        self.rotation = rotation
        self.w = .60 * conversion
        self.h = .40 * conversion

        self.internal_rectangle = ev.Rectangle(location[0], location[1], rotation, self.w, self.h)
        self.internal_rectangle._calculate_corners()

        self.forward_input = 0
        self.horizontal_input = 0

        self.lidar_cache = []

        self.controller = RobotController(36)

        if model_path is not None:
            self.controller.load(model_path)

        self.has_collided = False
        self.reached_goal = False

        self.points = 0
    
    def plot(self, plt):
        # update internals of rectangle
        self.internal_rectangle.center = self.location
        self.internal_rectangle.rotation = self.rotation
        self.internal_rectangle._calculate_corners()
        self.internal_rectangle.plot(plt, "blue", False, "bo")

    def lidar(self, obstacles=[], plt=None):
        rot = int(self.rotation / pi * 180)
        cache = []

        for t in range(rot, 360 + rot, 10):
            dir = np.array([
                max_ray * np.cos(t / 180 * np.pi),
                max_ray * np.sin(t / 180 * np.pi)
            ])  

            closest = None
            
            for obs in obstacles:
                intersection = obs.ray_intersection(self.location, dir)
                if intersection is not None:
                    dist = np.linalg.norm(intersection - self.location)
                    if dist > max_ray:
                        continue

                    end = np.array([
                        dist * np.cos(t / 180 * np.pi) + self.location[0],
                        dist * np.sin(t / 180 * np.pi) + self.location[1]
                    ])
                    
                    if closest is None or dist < np.linalg.norm(closest - self.location):
                        closest = end
            
            if plt:
                if closest is not None:
                    end = closest
                    plt.plot([self.location[0], end[0]], [self.location[1], end[1]], color='red', alpha=0.2)
                else:
                    plt.plot([self.location[0], dir[0] + self.location[0]], [self.location[1], dir[1] + self.location[1]], color='black', alpha=0.2)
            
            if closest is not None:
                cache.append(np.linalg.norm(closest - self.location))
            else:
                cache.append(max_ray)
        
        self.lidar_cache = cache

        pass

    def predict(self, goal=np.array([92,92])):
        dir = goal - self.location
        dir = dir / np.linalg.norm(dir)
        # convert to angle
        theta = np.arctan2(dir[1], dir[0])
        theta = theta - self.rotation

        distance = np.linalg.norm(goal - self.location)

        y, x = self.controller.get_action(np.array(self.lidar_cache), theta, distance)

        self.forward_input = y
        self.horizontal_input = x
    
    def collided(self, obstacles=[]):
        # recalculate the internal rectangle
        self.internal_rectangle.center = self.location
        self.internal_rectangle.rotation = self.rotation
        self.internal_rectangle._calculate_corners()

        for obs in obstacles:
            if obs.intersects(self.internal_rectangle):
                self.has_collided = True
                return True
        return False
    
    def reached(self, goal):
        if np.linalg.norm(goal - self.location) < 1:
            self.reached_goal = True
            return True
        return False

    def step(self, dt):
        left_output, right_output = arcade_drive(self.forward_input, self.horizontal_input)
        
        left_speed = left_output * max_wheel_speed
        right_speed = right_output * max_wheel_speed

        # linear and angular velocities
        v = (left_speed + right_speed) / 2.0
        omega = (right_speed - left_speed) / wheel_base

        x = self.location[0] + v * np.cos(self.rotation) * dt
        y = self.location[1] + v * np.sin(self.rotation) * dt
        theta = self.rotation + omega * dt

        self.location = np.array([x, y])
        self.rotation = theta
        pass

    def mutate(self):
        self.controller.mutate(mutation_rate)
        pass

    def crossover(self, other):
        self.controller.crossover(other.controller)
        pass

    def randomize(self):
        self.controller.randomize()
        pass

    def reset(self, pos, rot):
        self.location = pos
        self.rotation = rot
        self.has_collided = False
        self.forward_input = 0
        self.horizontal_input = 0
        self.lidar_cache = []

        self.points = 0

        self.internal_rectangle._calculate_corners()
        pass

    def save(self, path):
        self.controller.save(path)
        pass