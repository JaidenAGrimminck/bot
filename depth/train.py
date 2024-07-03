import h5py
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Conv2D, MaxPooling2D, Flatten, Dense, Dropout, UpSampling2D
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.preprocessing.image import ImageDataGenerator
import numpy as np
import cv2

print("loaded packages...")

mat_file = "/Volumes/Jaiden Grimminck 1TB/nyu_depth_v2_labeled.mat"

# custom callback to print progress
class ProgressPrinter(tf.keras.callbacks.Callback):
    def on_epoch_end(self, epoch, logs=None):
        print(f"Epoch {epoch+1}: Loss = {logs['loss']:.4f}, MAE = {logs['mae']:.4f}, Val Loss = {logs['val_loss']:.4f}, Val MAE = {logs['val_mae']:.4f}")

with h5py.File(mat_file, 'r') as f:
    images = np.array(f['images'])
    depths = np.array(f['depths'])

# Preprocess the images
def preprocess_images(images):
    # Normalize and resize images
    images = np.transpose(images, (0, 2, 3, 1))  # (count, rgb, width, height) => (count, width, height, rgb)
    images = images / 255.0 # normalize to [0, 1]
    return images

# Preprocess the depth maps
def preprocess_depths(depths):
    #depths = np.transpose(depths, (2, 0, 1))  # (height, width, samples) => (samples, height, width)
    depths = depths / np.max(depths)
    depths = depths[..., np.newaxis]  # add in channel dimension
    return depths

train_images = preprocess_images(images)
train_depths = preprocess_depths(depths)

# Split data into training and validation sets, 80% training, 20% validation
split_index = int(0.8 * len(train_images))
# validation images and depths
val_images = train_images[split_index:]
val_depths = train_depths[split_index:]
# training images and depths
train_images = train_images[:split_index]
train_depths = train_depths[:split_index]

print("loaded everything, beginning to train.")

# Create the model
def create_model(input_shape):
    model = Sequential()
    model.add(Conv2D(64, (3, 3), activation='relu', padding='same', input_shape=input_shape))
    model.add(MaxPooling2D((2, 2), padding='same'))
    model.add(Conv2D(128, (3, 3), activation='relu', padding='same'))
    model.add(MaxPooling2D((2, 2), padding='same'))
    model.add(Conv2D(256, (3, 3), activation='relu', padding='same'))
    model.add(MaxPooling2D((2, 2), padding='same'))
    model.add(Conv2D(512, (3, 3), activation='relu', padding='same'))
    model.add(MaxPooling2D((2, 2), padding='same'))
    
    # Add upsampling layers to ensure the output shape matches the input shape
    model.add(UpSampling2D((2, 2)))
    model.add(UpSampling2D((2, 2)))
    model.add(UpSampling2D((2, 2)))
    model.add(UpSampling2D((2, 2)))
    
    # Final Conv2D layer to get the output shape (fix issue with depth map dimensions)
    model.add(Conv2D(1, (3, 3), activation='linear', padding='same'))

    # resizing layer to match the target depth map dimensions (fix error that doesn't match)
    model.add(tf.keras.layers.Resizing(train_depths.shape[1], train_depths.shape[2]))

    model.compile(optimizer=Adam(learning_rate=0.0001), loss='mean_squared_error', metrics=['mae'])
    return model

# shapes of the data
print("Train images shape:", train_images.shape)
print("Train depths shape:", train_depths.shape)
print("Validation images shape:", val_images.shape)
print("Validation depths shape:", val_depths.shape)

model = create_model(train_images.shape[1:])

print("created model, now training...")

# Train the model
history = model.fit(
    train_images, train_depths, epochs=2, batch_size=32, validation_data=(val_images, val_depths),
    callbacks=[ProgressPrinter()]
)

# Save the model
model.save('nyu_depth_model.h5')

print("finished :)")