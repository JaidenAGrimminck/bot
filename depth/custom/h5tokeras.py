
import tensorflow as tf
from tensorflow.keras.models import load_model
import numpy as np
import cv2

print("loaded packages, loading model...")

# load model
model = load_model('nyu_depth_model.h5')

print("model loaded, saving model...")

# Save the model
model.save('nyu_depth_model.keras')

print("model saved.")