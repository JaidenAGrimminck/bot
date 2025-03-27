import sys
#sys.path.append("~/Documents/coding projects/bot-project/bot/topica")

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import environment as ev
from agent import Agent
from math import pi, sin
#from topica import TopicaServer
from constants import num_agents, dt, save_files, data_dir, file_connection
import os
import time

obstacles = []
agents = []

rt = 0

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
    for i in range(num_agents):
        agents.append(
            Agent(
                begin["pos"],
                begin["rot"]
            )
        )
    

goal = (92,92)
start = (20, 6)

begin = {
    "pos": start,
    "rot": pi / 4
}

i = 0
t = 0
s = 1

t_limit = 5
t_limit_ext = 5

def draw(frame):
    global i, t, t_limit, s

    plt.clf()
    plt.xlim(0, 100)
    plt.ylim(0, 100)
    plt.gca().set_aspect('equal', adjustable='box')
    # x axis and y axis 
    plt.title(f'Live Simulation t={int(t * 10) / 10}, s={s}, i={i}')

    for obs in obstacles:
        obs.plot(plt)

    plt.scatter(goal[0], goal[1], color='green', s=10)
    plt.scatter(start[0], start[1], color='blue', s=10)

    for agent in agents:
        if agent.has_collided or agent.reached_goal:
            agent.plot(plt)
            continue
        
        agent.plot(plt)
        agent.lidar(obstacles)
        agent.predict(np.array(goal))
        agent.step(dt) # 0.1 seconds steps
        agent.collided(obstacles)
        agent.reached(np.array(goal))

        if agent.has_collided:
            agent.points -= 1

    i += 1

    t += dt #dt

    if t > t_limit:
        t = 0
        i = 0
        s += 1

        

        # give points based on distance to goal
        for agent in agents:
            agent.points += 2 / np.linalg.norm(agent.location - np.array(goal))

        # order agents by points
        agents.sort(key=lambda x: x.points, reverse=True)

        if save_files:
            # check if the "saves" folder is present
            if not os.path.exists(f"{data_dir}{file_connection}saves-{rt}"):
                os.makedirs(f"{data_dir}{file_connection}saves-{rt}")
            
            os.makedirs(f"{data_dir}{file_connection}saves-{rt}{file_connection}gen-{s}", exist_ok=True)

            # save the top 10
            for j, agent in enumerate(agents[:10]):
                if not agent.reached_goal:
                    agent.save(f"{data_dir}saves-{rt}{file_connection}gen-{s}{file_connection}agent-{j}.npy")

            # save any that have reached the goal
            for j, agent in agents:
                if agent.reached_goal:
                    agent.save(f"{data_dir}saves-{rt}{file_connection}gen-{s}{file_connection}agent-SUCCESS-{j}.npy")

        # mutate the top 10%
        for agent in agents[:num_agents // 10]:
            agent.mutate()
        
        # for the rest, crossover
        for agent in agents[num_agents // 10:]:
            agent.crossover(agents[np.random.randint(num_agents)])

        for agent in agents:
            agent.reset(begin["pos"], begin["rot"])
        
        print(f"Generation {s} completed (t={t_limit})")
        print(f"Top 10 agents: {[agent.points for agent in agents[:10]]}")
        print("\n")

        t_limit += t_limit_ext

ani = None

def run():
    global ani

    plt.figure(figsize=(6, 6))

    ani = animation.FuncAnimation(plt.gcf(), draw, interval=1)

    plt.show()
    

if __name__ == "__main__":
    # set rt to now
    rt = time.time()
    setup()
    summonAgents()
    run()