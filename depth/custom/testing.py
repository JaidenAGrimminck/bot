import cv2
import tensorflow as tf
import numpy as np

# Load the trained model
model = tf.keras.models.load_model('nyu_depth_model.keras')

# Function to preprocess the images for the model
def preprocess_image(image):
    image = cv2.resize(image, (480, 640))  # Resize to match the input shape expected by the model (width, height)
    image = image / 255.0  # Normalize to [0, 1]

    image = np.transpose(image, (2, 0, 1))

    image = np.expand_dims(image, axis=0)  # Add batch dimension
    return image

# Function to postprocess the model output
def postprocess_depth(depth_map):
    depth_map = np.squeeze(depth_map)  # Remove batch dimension
    depth_map = cv2.normalize(depth_map, None, 0, 255, cv2.NORM_MINMAX)  # Normalize to [0, 255]
    depth_map = depth_map.astype(np.uint8)  # Convert to uint8
    return depth_map

# Initialize the webcam
cap = cv2.VideoCapture(1)

if not cap.isOpened():
    print("Error: Could not open webcam.")
    exit()

while True:
    ret, frame = cap.read()
    if not ret:
        print("Error: Failed to capture image.")
        break

    # Preprocess the captured frame
    input_image = preprocess_image(frame)
    
    # Predict the depth map using the model
    predicted_depth = model.predict(input_image)

    # Postprocess the depth map
    output_depth = postprocess_depth(predicted_depth)

    # Display the original frame and the depth map
    cv2.imshow('Original', frame)
    cv2.imshow('Depth Map', output_depth)

    k = cv2.waitKey(33)

    if k==27:    # Esc key to stop
        break
    elif k==-1:  # normally -1 returned,so don't print it
        continue
    else:
        print (k) # else print its value

# Release the webcam and close all OpenCV windows
cap.release()
cv2.destroyAllWindows()