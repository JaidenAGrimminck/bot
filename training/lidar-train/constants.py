import os

# # of pixels to 1 meter
conversion = 10 / 1

wheel_base = 0.4 * conversion  # Distance between the wheels (m => pixel units)
max_wheel_speed = 1.0 * conversion  # Maximum speed per wheel (m^2 => units per second)

num_agents = 1000

dt = 0.1

mutation_rate = 0.1

save_files = True
multi_process = True

base_dir = os.path.dirname(os.path.realpath(__file__))
data_dir = os.path.join(base_dir, "data")

# / on macos and \ on windows
file_connection = "\\" if os.name == "nt" else "/"

render = False