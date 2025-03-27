# # of pixels to 1 meter

conversion = 10 / 1

wheel_base = 0.4 * conversion  # Distance between the wheels (m => pixel units)
max_wheel_speed = 1.0 * conversion  # Maximum speed per wheel (m^2 => units per second)

num_agents = 100

dt = 0.1

mutation_rate = 0.1