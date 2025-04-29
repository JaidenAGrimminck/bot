import os

# the os_name, seperated as a separate variable if you want to use windows/macos settings on cross-platform etc
os_name = "nt" #os.name

# # of pixels to 1 meter
conversion = 10 / 1

wheel_base = 0.4 * conversion  # Distance between the wheels (m => pixel units)
max_wheel_speed = 1.0 * conversion  # Maximum speed per wheel (m^2 => units per second)

# how many agents to summon per generation
num_agents = 1000 if os_name == "nt" else 1

# time step for the simulation
dt = 0.1

# mutation rate for the genetic algorithm
mutation_rate = 0.1

# the environment to start at
start_env_index = 1

# how many generations to run before switching environments
env_switch_at = 100

# whether to save files or not
save_files = True if os_name == "nt" else False

# whether to use multiprocessing or not, splits processes into an even number of agents per core
multi_process = True if os_name == "nt" else False

# how much the time limit should be extended by each epoch
t_limit_ext = 1
# the initial time limit for the simulation
t_initial = 2

# the distance threshold for the goal
goal_threshold = 5

# the maximum time limit for the simulation
max_t = 40

# the base directory for this folder, don't change.
base_dir = os.path.dirname(os.path.realpath(__file__))

# the data directory for this folder
# this is where the data will be saved, should be created manually
data_dir = os.path.join(base_dir, "data")

# / on macos and \ on windows, ignore / do not change.
file_connection = "\\" if os.name == "nt" else "/"

# whether to render the simulation or not
render = False if os_name == "nt" else True

# always check if you have it installed
use_gpu_accel = False

# 2 has epoch printouts
# 1 has generation printouts
# 0 has no printouts
verbose = 2 if os_name == "nt" else 1

# the batch size for the genetic algorithm (acts as a maximum)
macos_batch = 25

# the batch size for the genetic algorithm (acts as a maximum)
windows_batch = 100

# whether to use a specific model to test it out
prefixed = {
    # to enable it
    "use": False,
    # the model in question
    "model": f"{data_dir}/saves/agent-SUCCESS-0.npz",
    # the interval per timestep (in ms)
    "interval": 10,
}

# whether to start at a specific saved generation and resume progress
checkpoint = {
    # to enable it
    "use": True,
    # the folder containing the generation in question
    "folder": f"{data_dir}/saves-1745857601.892839/gen-100-fullgen/",
    # the time limit for the simulation to start it at (bypassing the initial time limit)
    "time_limit": 40,
    # whether to train the model or not
    "train": True
}
