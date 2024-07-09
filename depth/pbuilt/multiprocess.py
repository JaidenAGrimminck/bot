from transformers import GLPNImageProcessor, GLPNForDepthEstimation
import torch
import numpy as np
from PIL import Image
import cv2
import asyncio

from time import sleep
import time


processor = GLPNImageProcessor.from_pretrained("vinvino02/glpn-nyu")
model = GLPNForDepthEstimation.from_pretrained("vinvino02/glpn-nyu")

webcam = cv2.VideoCapture(1)

async def cont(image, inputs):
    with torch.no_grad():
        predicted_depth = model(**inputs).predicted_depth

    print("done")

    # interpolate to original size
    prediction = torch.nn.functional.interpolate(
        predicted_depth.unsqueeze(1),
        size=image.size[::-1],
        mode="bicubic",
        align_corners=False,
    )

    # visualize the prediction
    output = prediction.squeeze().cpu().numpy()
    formatted = (output * 255 / np.max(output)).astype("uint8")
    depth = Image.fromarray(formatted)

    # cvt to opencv image
    depth = cv2.cvtColor(np.array(depth), cv2.COLOR_RGB2BGR)

    # to gray
    depth_gray = cv2.cvtColor(depth, cv2.COLOR_BGR2GRAY)

    # invert the image
    depth_gray = cv2.bitwise_not(depth_gray)

    # gaussian blur
    depth_gray = cv2.GaussianBlur(depth_gray, (5, 5), 0)

    # find the brightest color
    minVal, maxVal, minLoc, maxLoc = cv2.minMaxLoc(depth_gray)

    ret, thresh = cv2.threshold(depth_gray, max(0, maxVal - 20), min(maxVal + 10, 255), cv2.THRESH_BINARY)

    # get contours
    contours, _ = cv2.findContours(
        thresh,
        cv2.RETR_TREE,
        cv2.CHAIN_APPROX_NONE,
    )

    print(str(len(contours)))

    # draw contours
    cv2.drawContours(depth_gray, contours, -1, (0, 255, 0), 2)

    cv2.imshow("depth", depth_gray)

while True:
    # resize the image
    webcam_img = cv2.resize(webcam.read()[1], (320, 240))

    image = Image.fromarray(cv2.cvtColor(webcam_img, cv2.COLOR_BGR2RGB))

    # prepare image for the model
    inputs = processor(images=image, return_tensors="pt")

    time_now = time.time()

    print("predicting...")

    asyncio.run(cont(image, inputs))

    print(time.time() - time_now)

    print("next")

    #sleep(1)

    if cv2.waitKey(1) & 0xFF == ord("q"):
        break

    #break

while True:
    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
        break

webcam.release()