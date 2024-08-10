# import image to this program
import cv2
import numpy as np

# get arguments
import sys

# get arguments
if len(sys.argv) < 2:
    print("Usage: python map2data.py <map_image>")
    sys.exit(1)

args = sys.argv

print("Reading image ", args[1])

# read image
img = cv2.imread(args[1], 0)

# get image size
height, width = img.shape

text = ""

print("Creating map.txt...")

with open("map.txt", "w") as f:
    f.write("")

# clear memory, boosts performance
def saveCurrentProgress():
    global text
    with open("map.txt", "a") as f:
        # add text to file
        f.write(text)
    
    text = ""

for y in range(height):
    for x in range(width):
        # get pixel value
        pixel = img[y, x]

        # if pixel is black, it is wall
        if pixel == 0:
            text += "1"
        else:
            text += "0"
    text += "\n"

    if (y % 10 == 0):
        print("Processing... ", y, "/", height)

    if (y % 100 == 0 and y != 0):
        saveCurrentProgress()

saveCurrentProgress()

print("map.txt is finished in creation.")