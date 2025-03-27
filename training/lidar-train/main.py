import sys
#sys.path.append("~/Documents/coding projects/bot-project/bot/topica")

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import environment as ev
from agent import Agent
from math import pi, sin
#from topica import TopicaServer
from constants import num_agents, dt, save_files, data_dir, file_connection, multi_process, render, t_limit_ext, t_initial, verbose, windows_batch, macos_batch
import os
import time
import multiprocessing as mp
from functools import partial

# Set up multiprocessing with optimizations for 1000 agents

# Global process pool for efficiency
_process_pool = None

def get_process_pool():
    """Get or create process pool for reuse"""
    global _process_pool
    if _process_pool is None:
        # Leave one CPU free for system processes
        processes = max(1, mp.cpu_count() - 1)
        _process_pool = mp.Pool(processes=processes)
    return _process_pool

def process_agent_batch(agent_batch, goal, obstacles, dt):
    """Process a batch of agents in a single process"""
    for agent in agent_batch:
        if not (agent.has_collided or agent.reached_goal):
            agent.lidar(obstacles)
            agent.predict(np.array(goal))
            agent.step(dt)
            agent.collided(obstacles)
            agent.reached(np.array(goal))
    return agent_batch

def process_agents_parallel(agents, goal, obstacles, dt):
    """Process agents in parallel with optimized batching for 1000+ agents"""
    # For small number of agents, sequential processing is faster
    if len(agents) <= 20:
        for agent in agents:
            if not (agent.has_collided or agent.reached_goal):
                agent.lidar(obstacles)
                agent.predict(np.array(goal))
                agent.step(dt)
                agent.collided(obstacles)
                agent.reached(np.array(goal))
        return agents
    
    # Use larger batch sizes for Windows with many agents to reduce overhead
    num_processes = max(1, mp.cpu_count() - 1)

    batch_size_b = windows_batch if os.name == "nt" else macos_batch

    batch_size = max(batch_size_b, len(agents) // (num_processes * 2))
    
    # Create batches of agents
    agent_batches = [agents[i:i + batch_size] for i in range(0, len(agents), batch_size)]
    
    # Get process pool (reused between calls)
    pool = get_process_pool()
    
    # Process batches in parallel
    process_func = partial(process_agent_batch, goal=goal, obstacles=obstacles, dt=dt)
    result_batches = pool.map(process_func, agent_batches)
    
    # Combine results
    updated_agents = []
    for batch in result_batches:
        updated_agents.extend(batch)
    
    return updated_agents

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
        65, 20,
        pi / 10,
        30, 10
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

t_limit = t_initial


def draw(frame):
    global i, t, t_limit, s, agents

    if render:
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

    if not multi_process:
        for agent in agents:
            if (agent.has_collided or agent.reached_goal) and render:
                agent.plot(plt)
                continue
            
            if render:
                agent.plot(plt)
            agent.lidar(obstacles)
            agent.predict(np.array(goal))
            agent.step(dt) # 0.1 seconds steps
            agent.collided(obstacles)
            agent.reached(np.array(goal))

            if agent.has_collided:
                agent.points -= 1
    else:
        agents = process_agents_parallel(agents, goal, obstacles, dt)
        if render:
            for agent in agents:
                agent.plot(plt)

    i += 1

    t += dt #dt

    if verbose >= 2:
        print(f"Epoch {i} complete.")

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

            j = 0
            # save the top 10
            for agent in enumerate(agents[:10]):
                agent = agent[1]
                if not agent.reached_goal:
                    agent.save(f"{data_dir}{file_connection}saves-{rt}{file_connection}gen-{s}{file_connection}agent-{j}")
                j += 1
            j = 0

            # save any that have reached the goal
            for agent in agents:
                if agent.reached_goal:
                    agent.save(f"{data_dir}{file_connection}saves-{rt}{file_connection}gen-{s}{file_connection}agent-SUCCESS-{j}")

                j += 1

        # mutate the top 10%
        for agent in agents[:num_agents // 10]:
            agent.mutate()
        
        # for the rest, crossover
        for agent in agents[num_agents // 10:]:
            agent.crossover(agents[np.random.randint(num_agents)])
        
        if verbose >= 1:
            print(f"Generation {s} completed (t={t_limit})")
            print(f"Top 10 agents: {[agent.points for agent in agents[:10]]}")
            print("\n")

        for agent in agents:
            agent.reset(begin["pos"], begin["rot"])

        t_limit += t_limit_ext

ani = None

def run():
    global ani

    if render:
        plt.figure(figsize=(6, 6))

        ani = animation.FuncAnimation(plt.gcf(), draw, interval=1)

        plt.show()
    else:
        while True:
            draw(None)
    

if __name__ == "__main__":
    # set rt to now
    rt = time.time()
    setup()
    summonAgents()
    run()