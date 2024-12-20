import ws
import agent
import numpy as np
from clamp import clamp
import threading
import constants
import random

agentsN = constants.robotN
agents = []

actions = [
    0xA3, # turn left
    0xA4 # turn right
]
#     0xA1, # speed up
#     0xA2, # speed down
#     0xA0 # no action
# ]


"""
Creates all agents and appends them to the agents list
"""
def create():
    global agents
    for i in range(agentsN):
        newagent = agent.Agent(5, 2)

        agents.append(newagent)


"""
Normalizes the sensor values to be between 0 and 1
:param sensorValues: the sensor values to normalize
"""
def normalizeState(sensorValues):
    return np.clip(np.divide(np.array(sensorValues), 256), 0, 1)

"""
Predicts the action for each agent and sends it to the server
"""
def act():
    global agents

    if len(agents) == 0:
        create()
        return

    robot_actions = []

    #print("number of agents: " + str(len(agents)))

    for i in range(agentsN):
        if agents[i].crashed:
            robot_actions.append(0xA0)
            continue

        # print(agents[i])
        # print("agent: " + str(i))

        ostate = [0] * 8

        for j in range(0x01, 0x08):
            if j in ws.sensors.keys():
                if ws.sensors[i][j] is not None:
                    state[j - 1] = ws.sensors[i][j].get_values()[0]
                else:
                    state[j - 1] = 0

        state = [
            ostate[3],
            ostate[5],
            ostate[0],
            ostate[4],
            ostate[2]
        ]

        state = np.array(state)

        state = normalizeState(state).reshape(1, -1)

        values = agents[i].predict(state)

        # do a probability distribution
        action = np.random.choice(actions, p=values[0])

        # print hexadecimal of action
        # if action == 0xA1:
        #     print("speed up")
        # elif action == 0xA2:
        #     print("speed down")
        # elif action == 0xA3:
        #     print("turn left")
        # elif action == 0xA4:
        #     print("turn right")
        # elif action == 0xA0:
        #     print("idle")

        robot_actions.append(action)

    return robot_actions


"""
Called when a robot collides with another robot (or not, just called whenever checking the collision)
:param v: the values of the collision
:param robotAddr: the address of the robot that collided
"""
def onCollide(v, robotAddr):
    if len(agents) == 0:
        return

    if v[0] == 1:
        if not agents[robotAddr].crashed:
            agents[robotAddr].crashed = True

            print("Robot " + str(robotAddr) + " crashed")

"""
Called when the server is ready to start the simulation
"""
def finishStartup():
    global agents

    print("finished startup")

    create()

    for i in range(agentsN):
        ws.listen(i, 0xC7, onCollide)

        for j in range(0x01, 0x09):
            j_copy = np.copy(j) + 1 - 1
            print("subscribing to sensor " + str(j))
            ws.listen(i, j, lambda v: agents[i].update_sensor(j_copy, v[0]))

    ws.sendPayload([0x01, 0x02, 0xB5, 0x01]) # start simulation>?

    # loop update
    threading.Thread(target=loop).start()

"""
Main loop.
"""
def loop():
    while True:
        update()
        threading.Event().wait(0.1)

"""
Updates the agents
"""
def update():
    if len(agents) == 0:
        return

    # predict action
    action = act()

    for i in range(agentsN):
        agent_n = agents[i]

        if agent_n.crashed:
            continue

        ws.send(0xB0, [i, action[i]])

"""
Called when a generation finishes
"""
def onGenFinish(scores):
    top_score = max(scores)
    top_score_index = scores.index(top_score)

    print("Top score: " + str(top_score) + " at index " + str(top_score_index))

    for i in range(agentsN):
        if i != top_score_index:
            agents[i] = agents[top_score_index].clone()
            agents[i].mutate_weights()

    agents[random.randint(0, agentsN - 1)] = agents[random.randint(0, agentsN - 1)].randomize()

    print("ready for next gen.")

"""
Called when the websocket disconnects
"""
def onDisconnect():
    print("Disconnected from server")

    # save model

# [speed up, speed down, turn left, turn right, no action]

if __name__ == "__main__":
    ws.setDisconnectMethod(onDisconnect)
    ws.setGenerationFinishMethod(onGenFinish)
    ws.start_websocket(finishStartup)



