import ws
import agent
import numpy as np
from clamp import clamp

myagent = None

actions = [
    0xA1, # speed up
    0xA2, # speed down
    0xA3, # turn left
    0xA4, # turn right
    0xA0 # no action
]

def create():
    global myagent
    myagent = agent.Agent(agent.create_model(), 8, 5)

def normalizeState(sensorValues):
    return clamp(np.array(sensorValues) / 255, 0, 1)

def act():
    if myagent is None:
        create()

    state = np.zeros(8)

    for i in range(0x01, 0x08):
        if i in ws.sensors:
            state[i - 1] = ws.sensors[i].get_values()[0]

    values = myagent.predict(state)

    return actions[np.argmax(values)]

def onCollide(v):
    if myagent is None:
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
        ws.listen(i, lambda v: myagent.update_sensor(i, v))



# [speed up, speed down, turn left, turn right, no action]

if __name__ == "__main__":
    ws.start_websocket(finishStartup)



