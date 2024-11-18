from pyjoycon import JoyCon, get_R_id, get_L_id
import time
from math import floor
import pygame
import ws
import struct

right_joycon_id = get_R_id()
right_joycon = JoyCon(*right_joycon_id)

left_joycon_id = get_L_id()
left_joycon = JoyCon(*left_joycon_id)

right_profile = {
    "rest": {
        "x": 2100,
        "y": 1750
    },
    "max_y": 2875,
    "min_y": 640,
    "max_x": 3500,
    "min_x": 780
}

left_profile = {
    "rest": {
        "x": 1980,
        "y": 2150
    },
    "max_y": 3300,
    "min_y": 1000,
    "max_x": 3350,
    "min_x": 700
}

threshold = 0.2

lastSent = (0,0)

def map(n, start1, stop1, start2, stop2):
    return ((n-start1)/(stop1-start1))*(stop2-start2)+start2

def calculateJoysticks(left_status, right_status):
    left_x = left_status["analog-sticks"]["left"]["horizontal"]
    left_y = left_status["analog-sticks"]["left"]["vertical"]
    right_x = right_status["analog-sticks"]["right"]["horizontal"]
    right_y = right_status["analog-sticks"]["right"]["vertical"]

    # first, the left side
    if left_x < left_profile["rest"]["x"]:
        left_x = map(left_x, left_profile["min_x"], left_profile["rest"]["x"], -1, 0)
    else:
        left_x = map(left_x, left_profile["rest"]["x"], left_profile["max_x"], 0, 1)
    
    if left_y < left_profile["rest"]["y"]:
        left_y = map(left_y, left_profile["min_y"], left_profile["rest"]["y"], -1, 0)
    else:
        left_y = map(left_y, left_profile["rest"]["y"], left_profile["max_y"], 0, 1)

    # now, the right side
    if right_x < right_profile["rest"]["x"]:
        right_x = map(right_x, right_profile["min_x"], right_profile["rest"]["x"], -1, 0)
    else:
        right_x = map(right_x, right_profile["rest"]["x"], right_profile["max_x"], 0, 1)
    

    if right_y < right_profile["rest"]["y"]:
        right_y = map(right_y, right_profile["min_y"], right_profile["rest"]["y"], -1, 0)
    else:
        right_y = map(right_y, right_profile["rest"]["y"], right_profile["max_y"], 0, 1)

    return (left_x, left_y), (right_x, right_y)

def printMapped(x,y):
    ppos = floor(x * 10)
    pneg = floor(y * 10)

    for x in range(20):
        for y in range(20):
            if x == ppos and y == pneg:
                print("X", end="")
            else:
                print(" ", end="")
        print()


def sendJoystick(l_state, r_state):
    payload = []

    # takes in a list of 34 bytes.
    # first 2 bytes are the buttons
    # next 32 bytes are the joysticks

    # to keep the data at a minimum, use the standard of 1kb per second.
    # this means 30 updates per second, should be plenty for a controller.

    # first byte
    # 0b 0000 0000 EXPANDED:
    # 0: a
    # 1: b
    # 2: x
    # 3: y
    # 4: l
    # 5: r
    # 6: zl
    # 7: zr
    buttons = 0b00000000

    buttons |= r_state["buttons"]["right"]["a"] << 0
    buttons |= r_state["buttons"]["right"]["b"] << 1
    buttons |= r_state["buttons"]["right"]["x"] << 2
    buttons |= r_state["buttons"]["right"]["y"] << 3
    buttons |= l_state["buttons"]["left"]["l"] << 4
    buttons |= r_state["buttons"]["right"]["r"] << 5
    buttons |= l_state["buttons"]["left"]["zl"] << 6
    buttons |= r_state["buttons"]["right"]["zr"] << 7

    payload.append(buttons)

    # second byte
    # 0b 0000 0000 EXPANDED:
    # 0: minus
    # 1: plus
    # 2: leftStick
    # 3: rightStick
    # 4: dpadLeft
    # 5: dpadRight
    # 6: dpadUp
    # 7: dpadDown

    buttons = 0b00000000

    buttons |= l_state["buttons"]["shared"]["minus"] << 0
    buttons |= l_state["buttons"]["shared"]["plus"] << 1
    buttons |= l_state["buttons"]["shared"]["l-stick"] << 2
    buttons |= l_state["buttons"]["shared"]["r-stick"] << 3
    buttons |= l_state["buttons"]["left"]["left"] << 4
    buttons |= l_state["buttons"]["left"]["right"] << 5
    buttons |= l_state["buttons"]["left"]["up"] << 6
    buttons |= l_state["buttons"]["left"]["down"] << 7

    payload.append(buttons)

    # third byte
    # 0b 0000 0000 EXPANDED:
    # 0: rightSl
    # 1: rightSr
    # 2: leftSl
    # 3: leftSr
    # 4: home
    # 5: capture
    # 6: chargingGrip
    # 7: unused

    buttons = 0b00000000

    buttons |= r_state["buttons"]["right"]["sl"] << 0
    buttons |= r_state["buttons"]["right"]["sr"] << 1
    buttons |= l_state["buttons"]["left"]["sl"] << 2
    buttons |= l_state["buttons"]["left"]["sr"] << 3
    buttons |= l_state["buttons"]["shared"]["home"] << 4
    buttons |= l_state["buttons"]["shared"]["capture"] << 5
    buttons |= l_state["buttons"]["shared"]["charging-grip"] << 6
    buttons |= 0 << 7

    payload.append(buttons)

    # then, the rest of the data is the joysticks values (each a double (8 bytes) and 4 doubles for the 2 joysticks)
    # 8 byte chunks:
    # 0: left x
    # 1: left y
    # 2: right x
    # 3: right y

    left, right = calculateJoysticks(l_state, r_state)

    left_x, left_y = left
    right_x, right_y = right

    left_x = struct.pack('>d', left_x)
    left_y = struct.pack('>d', left_y)
    right_x = struct.pack('>d', right_x)
    right_y = struct.pack('>d', right_y)

    for i in range(8):
        payload.append(left_x[i])
    for i in range(8):
        payload.append(left_y[i])
    for i in range(8):
        payload.append(right_x[i])
    for i in range(8):
        payload.append(right_y[i])

    ws.sendPayload(payload)    

pygame.init()

# Set up the drawing window
screen = pygame.display.set_mode((550, 300))
pygame.display.set_caption("Controls")


# Run until the user asks to quit
running = True

ws.start(False)

while True:

    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            break
    
    # get x and y values
    right_status = right_joycon.get_status()
    left_status = left_joycon.get_status()

    left, right = calculateJoysticks(left_status, right_status)

    pygame.draw.rect(screen, (255,255,255), (0,0,550,300))

    pygame.draw.rect(screen, (0,0,0), (12.5,25,250,250))
    pygame.draw.rect(screen, (0,0,0), (275 +12.5,25,250,250))

    x1, y1 = left
    x2, y2 = right

    x1 = round(x1 * 10) / 10
    y1 = round(y1 * 10) / 10
    x2 = round(x2 * 10) / 10
    y2 = round(y2 * 10) / 10

    left_color = (255,0,0)
    right_color = (255,0,0)

    r_button = right_status["buttons"]["right"]["r"]
    l_button = left_status["buttons"]["left"]["l"]

    if r_button:
        right_color = (0,0,255)
    if l_button:
        left_color = (0,0,255)

    if r_button and l_button:
        left_color = (0,255,0)
        right_color = (0,255,0)

    pygame.draw.circle(screen, left_color, (12.5 + 125 + 125 * x1, 25 + 125 + 125 * -y1), 10)
    pygame.draw.circle(screen, right_color, (275 + 12.5 + 125 + 125 * x2, 25 + 125 + 125 * -y2), 10)

    # create threshold lines
    # make it not fill
    pygame.draw.rect(screen, (125, 125, 125), (12.5 + 125 - 125 * threshold, 25 + 125 - 125 * threshold, 250 * threshold, 250 * threshold), 2)
    pygame.draw.rect(screen, (125, 125, 125), (12.5 + 125 + 275 - 125 * threshold, 25 + 125 - 125 * threshold, 250 * threshold, 250 * threshold), 2)

    # check if within threshold, if so, snap to 0
    if abs(x1) < threshold:
        x1 = 0
    if abs(y1) < threshold:
        y1 = 0
    if abs(x2) < threshold:
        x2 = 0
    if abs(y2) < threshold:
        y2 = 0

    sendJoystick(left_status, right_status)

    pygame.display.flip()
