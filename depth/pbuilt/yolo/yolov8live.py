from ultralytics import YOLO
import cv2
import time

# load the model
model = YOLO("datasets/yolov8l.pt")

webcam = cv2.VideoCapture(1)

while True:
    # detect objects in the image
    ret, image = webcam.read()
    
    # resize the image
    webcam_img = cv2.resize(webcam.read()[1], (640, 480))

    n1 = time.time()

    # run the model
    results = model(image)

    n2 = time.time()

    # takes about half a second
    print("Time taken: ", n2 - n1)

    n1 = time.time()

    boxes = results[0].boxes.xyxy.tolist()
    classes = results[0].boxes.cls.tolist()
    names = results[0].names
    confidences = results[0].boxes.conf.tolist()

    # loop over the detections, creating a tuple with the box, class (index in names), and confidence
    for box, class_index, confidence in zip(boxes, classes, confidences):
        x1, y1, x2, y2 = box
        name = names[int(class_index)]

        # draw the bounding box
        cv2.rectangle(image, (int(x1), int(y1)), (int(x2), int(y2)), (0, 255, 0), 2)

        # draw the text for the image
        cv2.putText(image, name + " " + str(round((confidence) * 100) / 100), (int(x1), int(y1)), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 0), 2)

        # fill rectangle
        cv2.rectangle(image, (int(x1), int(y1)), (int(x1) + 200, int(y1) + 30), (0, 255, 0), -1)

    n2 = time.time()

    # consistently takes ~0.0001
    print("Time taken to draw image: ", n2 - n1)

    # display the image
    cv2.imshow("image", image)

    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
        break

webcam.release()
