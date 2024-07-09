from ultralytics import YOLO
import cv2
import time
from transformers import GLPNImageProcessor, GLPNForDepthEstimation
import torch
import numpy as np
from PIL import Image
import cv2
import asyncio
from functools import reduce

# https://medium.com/analytics-vidhya/tutorial-how-to-scale-and-rotate-contours-in-opencv-using-python-f48be59c35a2
def scale_contour(cnt, scale):
    if cnt is None:
        return None
    
    if scale == 1:
        return cnt

    M = cv2.moments(cnt)

    if M['m00'] == 0:
        return None

    cx = int(M['m10']/M['m00'])
    cy = int(M['m01']/M['m00'])

    cnt_norm = cnt - [cx, cy]
    cnt_scaled = cnt_norm * scale
    cnt_scaled = cnt_scaled + [cx, cy]
    cnt_scaled = cnt_scaled.astype(np.int32)

    return cnt_scaled

# load the model
classify_model = YOLO("yolo/datasets/yolov8l.pt")

webcam = cv2.VideoCapture(1)

processor = GLPNImageProcessor.from_pretrained("vinvino02/glpn-nyu")
depth_model = GLPNForDepthEstimation.from_pretrained("vinvino02/glpn-nyu")

def depth_process(image, inputs):
    with torch.no_grad():
        predicted_depth = depth_model(**inputs).predicted_depth

    print("predicted depths")

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

    return (contours)


while True:
    ret, image = webcam.read()

    # in reality, this should be 640x480 or greater, it's approx the same time to process no matter the size
    # it'll also be passed thru a websocket/ros
    webcam_img = cv2.resize(image, (320, 240))

    results = classify_model(webcam_img)

    smaller_img = cv2.resize(image, (320, 240))

    to_img = Image.fromarray(cv2.cvtColor(smaller_img, cv2.COLOR_BGR2RGB))
    inputs = processor(images=image, return_tensors="pt")

    depth_contours = depth_process(to_img, inputs)

    boxes = results[0].boxes.xyxy.tolist()
    classes = results[0].boxes.cls.tolist()
    names = results[0].names
    confidences = results[0].boxes.conf.tolist()

    # loop over the detections, creating a tuple with the box, class (index in names), and confidence
    for box, class_index, confidence in zip(boxes, classes, confidences):
        x1, y1, x2, y2 = box
        name = names[int(class_index)]

        # draw the bounding box
        cv2.rectangle(webcam_img, (int(x1), int(y1)), (int(x2), int(y2)), (0, 255, 0), 2)

        # draw the text for the image
        cv2.putText(webcam_img, name + " " + str(round((confidence) * 100) / 100), (int(x1), int(y1)), cv2.FONT_HERSHEY_SIMPLEX, 0.3, (0, 0, 0), 2)

        # fill rectangle
        #cv2.rectangle(webcam_img, (int(x1), int(y1)), (int(x1) + 200, int(y1) + 30), (0, 255, 0), -1)

    # from debugging
    scaled_contours = []

    for i in range(len(depth_contours)):
        scaled_new = scale_contour(depth_contours[i], 2)

        # move the contours to the right
        if scaled_new is not None:
            scaled_new = scaled_new + [320, 240]

        scaled_contours.append(scaled_new)

    cv2.drawContours(webcam_img, depth_contours, -1, (0, 255, 0), 2)

    cv2.imshow("image", webcam_img)

    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
        break

    break

while True:
    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
        break


webcam.release()