import ws
import agent
import numpy as np
from clamp import clamp
import threading

agents = []

actions = [
    0xA1, # speed up
    0xA2, # speed down
    0xA3, # turn left
    0xA4, # turn right
    0xA0 # no action
]

def create():
    global agents
    for i in range(5):
        newagent = agent.Agent(8, 5)

        agents.append(newagent)


def normalizeState(sensorValues):
    return np.clip(np.divide(np.array(sensorValues), 256), 0, 1)

def act():
    if len(agents) == 0:
        create()

    robot_actions = []

    for i in range(5):
        state = [0] * 8

        for i in range(0x01, 0x08):
            if i in ws.sensors.keys():
                state[i - 1] = ws.sensors[i].get_values()[0]

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



def onCollide(v):
    if len(agents) == 0:
        return

    if v[0] == 1:
        if not myagent.crashed:
            myagent.crashed = True
            print("agent crashed!")

def finishStartup():
    print("finished startup")

    create()

    ws.listen(0xC7, onCollide)

    for i in range(0x01, 0x08):
        ws.listen(i, lambda v: myagent.update_sensor(i, v[0]))

    # loop update
    threading.Thread(target=loop).start()

def loop():
    while True:
        update()
        threading.Event().wait(0.1)

def update():
    if myagent is None:
        return

    if myagent.crashed:
        return

    # predict action
    action = act()

    ws.send(0xB0, [action])

def onDisconnect():
    print("Disconnected from server")

    # save model

# [speed up, speed down, turn left, turn right, no action]

if __name__ == "__main__":
    ws.setDisconnectMethod(onDisconnect)
    ws.start_websocket(finishStartup)



