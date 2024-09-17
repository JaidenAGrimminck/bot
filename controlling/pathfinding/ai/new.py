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

def act(state):
    if myagent is None:
        create()

    values = myagent.predict(state)

    return actions[np.argmax(values)]

def onCollide(v):
    if v[0] == 1:
        print("collided!!!")

def finishStartup():
    print("oisadgjlasndgl")
    ws.listen(0xC7, onCollide)

# [speed up, speed down, turn left, turn right, no action]

if __name__ == "__main__":
    ws.start_websocket(finishStartup)



