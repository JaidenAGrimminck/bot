from transformers import GLPNImageProcessor, GLPNForDepthEstimation
import torch
import numpy as np
from PIL import Image
import cv2

processor = GLPNImageProcessor.from_pretrained("vinvino02/glpn-nyu")
model = GLPNForDepthEstimation.from_pretrained("vinvino02/glpn-nyu")

webcam = cv2.VideoCapture(1)

while True:
    image = Image.fromarray(cv2.cvtColor(webcam.read()[1], cv2.COLOR_BGR2RGB))

    # prepare image for the model
    inputs = processor(images=image, return_tensors="pt")

    print("predicting...")

    # takes a long time, need to optmize

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

    cv2.imshow("depth", cv2.cvtColor(np.array(depth), cv2.COLOR_RGB2BGR))

    # wait for the user to close the window
    if cv2.waitKey(1) & 0xFF == ord("q"):
        break

    print("next")

webcam.release()