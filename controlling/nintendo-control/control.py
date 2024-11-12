from pyjoycon import JoyCon, get_R_id, get_L_id
import time
from math import floor
import pygame

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

pygame.init()

# Set up the drawing window
screen = pygame.display.set_mode((550, 300))
pygame.display.set_caption("Controls")


# Run until the user asks to quit
running = True

while True:

    for event in pygame.event.get():
            if event.type == pygame.QUIT:
                done = True
    
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


    pygame.display.flip()



print(callibrations)