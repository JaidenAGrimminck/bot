import sys
#sys.path.append("~/Documents/coding projects/bot-project/bot/topica")

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import environment as ev
from agent import Agent
from math import pi, sin
#from topica import TopicaServer

obstacles = []
agents = []

"""
Setup the environment with obstacles
"""
def setup():
    obstacles.append(ev.Rectangle(
        10, 20,
        2*pi - pi / 4,
        10, 10
    ))

    obstacles.append(ev.Rectangle(
        50, 50,
        pi / 6,
        10, 10
    ))
    
    obstacles.append(ev.Rectangle(
        20, 65,
        pi / 3,
        50, 10
    ))

    obstacles.append(ev.Rectangle(
        60, 20,
        -pi / 10,
        50, 10
    ))

    obstacles.append(ev.Rectangle(
        75, 75,
        1.7 * pi / 3,
        50, 10
    ))

def summonAgents():
    for i in range(1):
        agents.append(
            Agent(
                start,
                pi / 4
            )
        )
    

goal = (92,92)
start = (20, 6)

i = 0

def draw(frame):
    global i

    plt.clf()
    plt.xlim(0, 100)
    plt.ylim(0, 100)
    plt.gca().set_aspect('equal', adjustable='box')
    # x axis and y axis 
    plt.title(f'Live Simulation')

    for obs in obstacles:
        obs.plot(plt)

    plt.scatter(goal[0], goal[1], color='green', s=10)
    plt.scatter(start[0], start[1], color='blue', s=10)

    for agent in agents:
        agent.plot(plt)
        agent.lidar(obstacles, plt)
        agent.step(0.1) # 0.1 seconds steps

    i += 1

ani = None

def run():
    global ani

    plt.figure(figsize=(6, 6))

    ani = animation.FuncAnimation(plt.gcf(), draw, interval=100)

    plt.show()
    

if __name__ == "__main__":
    setup()
    summonAgents()
    run()