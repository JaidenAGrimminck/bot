import sys
sys.path.append('../nn')

import numpy as np
import environment as ev
import matplotlib as mpl
from math import pi
from constants import conversion, wheel_base, max_wheel_speed
from robot import arcade_drive



max_ray = conversion * 10 # 10 meters

class Agent:
    def __init__(self, location, rotation):
        self.location = location
        self.rotation = rotation
        self.w = .60 * conversion
        self.h = .40 * conversion

        self.internal_rectangle = ev.Rectangle(location[0], location[1], rotation, self.w, self.h)
        self.internal_rectangle._calculate_corners()

        self.forward_input = 0
        self.horizontal_input = 0
    
    def plot(self, plt):
        # update internals of rectangle
        self.internal_rectangle.center = self.location
        self.internal_rectangle.rotation = self.rotation
        self.internal_rectangle._calculate_corners()
        self.internal_rectangle.plot(plt, "blue", False, "bo")

    def lidar(self, obstacles=[], plt=None):
        rot = int(self.rotation / pi * 180)
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
        pass

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
        