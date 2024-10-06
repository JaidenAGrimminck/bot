from ultralytics import YOLO
import cv2

# load the model
model = YOLO("datasets/yolo11l.pt")

image = "datasets/images/val/cone.jpg"

# detect objects in the image

results = model(image)

# display the image
img = cv2.imread(image)

boxes = results[0].boxes.xyxy.tolist()
classes = results[0].boxes.cls.tolist()
names = results[0].names
confidences = results[0].boxes.conf.tolist()

# loop over the detections
for box, class_int, conf in zip(boxes, classes, confidences):
    x1, y1, x2, y2 = box
    confidence = conf
    name = names[int(class_int)]

    # draw the bounding box
    cv2.rectangle(img, (int(x1), int(y1)), (int(x2), int(y2)), (0, 255, 0), 2)

    # draw the text for the image
    cv2.putText(img, name + " " + str(round((confidence) * 100) / 100), (int(x1), int(y1)), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 0), 2)

    # fill rectangle
    cv2.rectangle(img, (int(x1), int(y1)), (int(x1) + 200, int(y1) + 30), (0, 255, 0), -1)

# display the image
cv2.imshow("image", img)

# wait for a key press
while True:
    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
        break
