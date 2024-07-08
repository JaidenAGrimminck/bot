from ultralytics import YOLO

# load the model
model = YOLO("datasets/yolov8l.pt")

image = "datasets/images/val/cone.jpg"

# detect objects in the image

results = model(image)

# print the results

results.print()