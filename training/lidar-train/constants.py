import os

# # of pixels to 1 meter
conversion = 10 / 1

wheel_base = 0.4 * conversion  # Distance between the wheels (m => pixel units)
max_wheel_speed = 1.0 * conversion  # Maximum speed per wheel (m^2 => units per second)

num_agents = 1000 if os.name == "nt" else 100

dt = 0.1

mutation_rate = 0.1

save_files = True if os.name == "nt" else False
multi_process = True if os.name == "nt" else False

t_limit_ext = 1
t_initial = 2

goal_threshold = 5

max_t = 40

base_dir = os.path.dirname(os.path.realpath(__file__))
data_dir = os.path.join(base_dir, "data")

# / on macos and \ on windows
file_connection = "\\" if os.name == "nt" else "/"

render = False if os.name == "nt" else True
# always check if you have it installed
use_gpu_accel = False

# 2 has epoch printouts
# 1 has generation printouts
# 0 has no printouts
verbose = 2 if os.name == "nt" else 1

macos_batch = 50
windows_batch = 100

prefixed = {
    "use": False,
    "model": f"{data_dir}/saves/agent-SUCCESS-0.npz",
    "interval": 10,
}

checkpoint = {
    "use": True,
    "folder": f"{data_dir}/saves-1743594449.956884-2/gen-50-fullgen/",
    "time_limit": 20,
    "train": True
}
