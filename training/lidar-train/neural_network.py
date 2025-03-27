import numpy as np
import matplotlib.pyplot as plt
from scipy.ndimage import convolve1d
from constants import conversion, use_gpu_accel

torch_installed = False

try:
    import torch

    torch_installed = True
except ImportError:
    print("Torch not installed, ignoring.")

import numpy as np
import platform
import subprocess
import os

def check_amd_gpu():
    """Check specifically for AMD GPU availability"""
    if platform.system() == "Windows":
        try:
            import win32com.client
            wmi = win32com.client.GetObject("winmgmts:")
            for gpu in wmi.InstancesOf("Win32_VideoController"):
                if "AMD" in gpu.Name and "Radeon" in gpu.Name:
                    return True, gpu.Name
        except:
            pass
    elif platform.system() == "Linux":
        try:
            output = subprocess.check_output(["lspci"], universal_newlines=True)
            if "AMD" in output and "Radeon" in output:
                for line in output.split('\n'):
                    if "AMD" in line and "Radeon" in line:
                        return True, line.strip()
        except:
            pass
    return False, "No AMD Radeon GPU detected"

def check_gpu_availability():
    """Check if GPU acceleration is available"""
    has_amd, amd_info = check_amd_gpu()

    if not torch_installed:
        return "No GPU acceleration available"
    
    if torch.cuda.is_available():
        return f"CUDA ({torch.cuda.get_device_name(0)})", torch.device("cuda")
    elif hasattr(torch, 'has_rocm') and torch.has_rocm:
        return f"ROCm (AMD GPU: {amd_info})", torch.device("rocm")
    elif hasattr(torch.backends, 'mps') and torch.backends.mps.is_available():
        return "MPS (Apple GPU)", torch.device("mps")
    elif has_amd:
        print(f"AMD GPU detected ({amd_info}) but no acceleration available.")
        print("Consider installing PyTorch with ROCm support")
        return "CPU (AMD GPU not utilized)", torch.device("cpu")
    else:
        return "CPU", torch.device("cpu")

def numpy_to_tensor(np_array, device=None):
    """Convert NumPy array to PyTorch tensor on appropriate device"""
    if device is None:
        _, device = check_gpu_availability()
    return torch.tensor(np_array, device=device, dtype=torch.float32)

def tensor_to_numpy(tensor):
    """Convert PyTorch tensor to NumPy array"""
    return tensor.detach().cpu().numpy()

class GPUNeuralNetwork:
    """GPU-accelerated version of the neural network"""
    
    def __init__(self, shape, device=None):
        self.shape = shape
        self.size = len(shape)
        
        self.device_info, self.device = check_gpu_availability() if device is None else (None, device)
        print(f"Using device: {self.device_info}")
        
        self.weights = []
        self.biases = []
        
        for i in range(1, self.size):
            # He initialization
            self.weights.append(torch.randn(self.shape[i-1], self.shape[i], 
                                           device=self.device) * 
                              torch.sqrt(torch.tensor(2.0/self.shape[i-1], device=self.device)))
            self.biases.append(torch.zeros(self.shape[i], device=self.device))
    
    def predict(self, input_data):
        if isinstance(input_data, np.ndarray):
            layer = numpy_to_tensor(input_data, self.device)
        else:
            layer = input_data
            
        # First n-1 layers use ReLU
        for i in range(self.size - 2):
            layer = torch.matmul(layer, self.weights[i]) + self.biases[i]
            layer = torch.relu(layer)
        
        # Last layer uses sigmoid
        layer = torch.matmul(layer, self.weights[-1]) + self.biases[-1]
        layer = torch.sigmoid(layer)
        
        return tensor_to_numpy(layer)
    
    # Rest of methods similar to EnhancedNeuralNetwork but using torch functions
    def zero(self):
        for i in range(self.size - 1):
            self.weights[i].zero_()
            self.biases[i].zero_()
    
    def randomize(self):
        for i in range(self.size - 1):
            self.weights[i] = torch.randn(self.shape[i-1], self.shape[i], 
                                       device=self.device) * \
                           torch.sqrt(torch.tensor(2.0/self.shape[i-1], device=self.device))
            self.biases[i].zero_()
    
    def clone(self):
        n = GPUNeuralNetwork(self.shape, self.device)
        for i in range(self.size - 1):
            n.weights[i] = self.weights[i].clone()
            n.biases[i] = self.biases[i].clone()
        return n

class LidarProcessor:
    """Process raw LIDAR data and prepare it for neural network input"""
    
    def __init__(self, num_lidar_points=36, max_range=10.0, kernel_size=3):
        """
        Initialize the LIDAR processor
        
        Args:
            num_lidar_points: Number of LIDAR readings (e.g., 36 for 10° resolution in 360°)
            max_range: Maximum range of LIDAR in meters
            kernel_size: Size of the convolution kernel
        """
        self.num_lidar_points = num_lidar_points
        self.max_range = max_range
        
        # Create a circular convolution kernel for feature extraction
        # This helps in detecting edges, gaps, and other patterns in the LIDAR data
        self.kernel = self._create_kernel(kernel_size)
    
    def _create_kernel(self, size):
        """Create a kernel for circular 1D convolution"""
        # Simple edge detection kernel
        if size == 3:
            return np.array([1, -2, 1])
        # Gaussian-like kernel
        elif size == 5:
            return np.array([0.1, 0.2, 0.4, 0.2, 0.1])
        else:
            # Default to a simple kernel
            return np.array([0.25, 0.5, 0.25])
    
    def process_lidar_data(self, raw_lidar_readings):
        """
        Process raw LIDAR readings
        
        Args:
            raw_lidar_readings: Array of distance measurements from LIDAR
            
        Returns:
            Processed features from LIDAR data
        """
        # Normalize the readings
        normalized_readings = np.clip(raw_lidar_readings / self.max_range, 0, 1)
        
        # Apply circular 1D convolution (for feature extraction)
        # Mode='wrap' ensures circular convolution (connects the ends)
        features = convolve1d(normalized_readings, self.kernel, mode='wrap')
        
        # Return both normalized readings and extracted features
        return np.concatenate([normalized_readings, features])

class EnhancedNeuralNetwork:
    def __init__(self, shape):
        self.weights = []
        self.biases = []  # Adding biases for better performance
        
        self.shape = shape  # shape of the neural network
        self.size = len(shape)  # easy access for later
        
        for i in range(1, self.size):
            # He initialization for better training
            self.weights.append(np.random.randn(self.shape[i - 1], self.shape[i]) * 
                               np.sqrt(2.0 / self.shape[i - 1]))
            self.biases.append(np.zeros(self.shape[i]))
    
    def zero(self):
        """Zero the weights and biases"""
        for i in range(1, self.size):
            self.weights[i - 1] = np.zeros((self.shape[i - 1], self.shape[i]))
            self.biases[i - 1] = np.zeros(self.shape[i])
    
    def randomize(self):
        """Randomize the weights with proper scaling"""
        for i in range(1, self.size):
            # He initialization
            self.weights[i - 1] = np.random.randn(self.shape[i - 1], self.shape[i]) * \
                                 np.sqrt(2.0 / self.shape[i - 1])
            self.biases[i - 1] = np.zeros(self.shape[i])
    
    def set_weights(self, weights, biases=None):
        """Set weights and optionally biases"""
        for i in range(len(self.weights)):
            self.weights[i] = np.array(weights[i])
        
        if biases is not None:
            for i in range(len(self.biases)):
                self.biases[i] = np.array(biases[i])
    
    def predict(self, input_data):
        """Forward pass through the network with activation functions"""
        layer = input_data
        
        # First n-1 layers use ReLU
        for i in range(self.size - 2):
            layer = np.dot(layer, self.weights[i]) + self.biases[i]
            layer = relu(layer)
        
        # Last layer uses sigmoid for output between 0-1
        layer = np.dot(layer, self.weights[-1]) + self.biases[-1]
        layer = sigmoid(layer)
        
        return layer
    
    def clone(self):
        """Create a copy of the network"""
        n = EnhancedNeuralNetwork(self.shape)
        for i in range(self.size - 1):
            n.weights[i] = np.copy(self.weights[i])
            n.biases[i] = np.copy(self.biases[i])
        return n
    
    def mutate_weights(self, mutation_rate):
        """Mutate weights and biases for genetic algorithm training"""
        new_net = EnhancedNeuralNetwork(self.shape)
        
        for i in range(self.size - 1):
            new_net.weights[i] = self.weights[i] + (np.random.randn(*self.weights[i].shape) * mutation_rate)
            new_net.biases[i] = self.biases[i] + (np.random.randn(*self.biases[i].shape) * mutation_rate)
        
        return new_net
    
    def save(self, filename):
        """Save model weights and biases"""
        data_dict = {}
        for i, w in enumerate(self.weights):
            data_dict[f'weight_{i}'] = w
        for i, b in enumerate(self.biases):
            data_dict[f'bias_{i}'] = b
        np.savez(filename, **data_dict)
    
    def load(self, filename):
        """Load model weights and biases"""
        data = np.load(filename)
        self.weights = [data[f'weight_{i}'] for i in range(len(self.weights))]
        self.biases = [data[f'bias_{i}'] for i in range(len(self.biases))]

# Activation functions
def relu(x):
    return np.maximum(0, x)

def sigmoid(x):
    return 1 / (1 + np.exp(-x))

class RobotController:
    """Main robot controller that integrates LIDAR processing and neural network"""
    
    def __init__(self, num_lidar_points=36):
        """
        Initialize the robot controller
        
        Args:
            num_lidar_points: Number of LIDAR readings (e.g., 36 for 10° resolution)
        """
        self.lidar_processor = LidarProcessor(num_lidar_points=num_lidar_points)
        self.num_lidar_points = num_lidar_points
        
        # Calculate the exact input size
        # LIDAR readings + LIDAR features + goal direction (sin, cos) + goal distance
        input_size = num_lidar_points * 2 + 3
        
        # Output: [linear_velocity, angular_velocity]
        output_size = 2
        
        # Create neural network with appropriate architecture
        if torch_installed and use_gpu_accel:
            self.network = GPUNeuralNetwork([input_size, 64, 32, output_size])
        else:
            self.network = EnhancedNeuralNetwork([input_size, 64, 32, output_size])
        
        self.network.randomize()  # Initialize with random weights
    
    def crossover(self, other):
        """Crossover with another controller for genetic algorithm training"""
        new_network = self.network.clone()
        for i in range(len(self.network.weights)):
            mask = np.random.randint(2, size=self.network.weights[i].shape)
            new_network.weights[i] = np.where(mask, self.network.weights[i], other.network.weights[i])
            
            mask = np.random.randint(2, size=self.network.biases[i].shape)
            new_network.biases[i] = np.where(mask, self.network.biases[i], other.network.biases[i])
        
        new_controller = RobotController(num_lidar_points=self.num_lidar_points)
        new_controller.network = new_network
        return new_controller

    def get_action(self, lidar_readings, goal_direction, goal_distance):
        """
        Determine robot action based on sensor readings and goal
        
        Args:
            lidar_readings: Raw LIDAR distance readings (array)
            goal_direction: Direction to goal in radians (0 is forward)
            goal_distance: Distance to goal in meters
            
        Returns:
            [linear_velocity, angular_velocity] for robot control
        """
        # Process LIDAR data
        processed_lidar = self.lidar_processor.process_lidar_data(lidar_readings)
        
        # Normalize goal information
        sin_goal = np.sin(goal_direction)
        cos_goal = np.cos(goal_direction)
        normalized_distance = min(1.0, goal_distance / (conversion * 10))  # Assume max relevant distance is 10m
        
        # Create input vector for neural network
        nn_input = np.concatenate([
            processed_lidar,
            [sin_goal, cos_goal, normalized_distance]
        ])
        
        # Ensure input has the correct shape
        expected_size = self.num_lidar_points * 2 + 3
        if nn_input.shape[0] != expected_size:
            raise ValueError(f"Input size mismatch. Expected {expected_size}, got {nn_input.shape[0]}")
        
        # Get network prediction
        action = self.network.predict(nn_input)
        
        # Scale outputs to appropriate ranges
        linear_velocity = action[0] * 1.0  # Max 1 m/s
        angular_velocity = (action[1] * 2.0) - 1.0  # -1 to 1 rad/s
        
        return linear_velocity, angular_velocity
    
    def mutate(self, mutation_rate):
        """Mutate the controller for genetic algorithm training"""
        new_network = self.network.mutate_weights(mutation_rate)
        new_controller = RobotController(num_lidar_points=self.num_lidar_points)
        new_controller.network = new_network
        return new_controller

    def save(self, filename):
        """Save the controller"""
        self.network.save(filename)
    
    def load(self, filename):
        """Load the controller"""
        self.network.load(filename)

# Example usage
def simulate_environment(visualize=True):
    """Simple simulation to demonstrate the controller"""
    # Create simulated environment
    map_size = 20
    robot_x, robot_y = map_size/2, map_size/2
    robot_theta = 0
    goal_x, goal_y = map_size*0.8, map_size*0.8
    
    # Create obstacles (circles)
    obstacles = [
        {"x": map_size*0.3, "y": map_size*0.7, "radius": 2},
        {"x": map_size*0.7, "y": map_size*0.4, "radius": 1.5},
        {"x": map_size*0.5, "y": map_size*0.6, "radius": 1}
    ]
    
    # Create controller
    controller = RobotController(num_lidar_points=36)
    
    # Create figure for visualization if requested
    if visualize:
        plt.figure(figsize=(10, 8))
    
    # Simulation steps
    max_steps = 200
    dt = 0.1  # time step
    
    for step in range(max_steps):
        # Generate LIDAR readings
        lidar_readings = []
        for i in range(36):  # 36 points around the robot
            angle = i * (2 * np.pi / 36)
            # Combine with robot orientation
            world_angle = angle + robot_theta
            
            # Maximum distance the LIDAR can see
            max_dist = 10.0
            ray_dist = max_dist
            
            # Check for intersections with obstacles
            for obs in obstacles:
                # Vector from robot to obstacle center
                dx = obs["x"] - robot_x
                dy = obs["y"] - robot_y
                
                # Project onto ray direction
                ray_dir_x = np.cos(world_angle)
                ray_dir_y = np.sin(world_angle)
                
                # This is the distance along the ray to the closest point to the obstacle
                t = dx * ray_dir_x + dy * ray_dir_y
                
                # Only consider obstacles in front of the robot
                if t > 0:
                    # Closest distance from ray to obstacle center
                    closest_x = robot_x + ray_dir_x * t
                    closest_y = robot_y + ray_dir_y * t
                    
                    # Distance from closest point to obstacle center
                    closest_dist = np.sqrt((closest_x - obs["x"])**2 + (closest_y - obs["y"])**2)
                    
                    # If this distance is less than the obstacle radius, we have an intersection
                    if closest_dist < obs["radius"]:
                        # Calculate actual intersection distance
                        dt = np.sqrt(obs["radius"]**2 - closest_dist**2)
                        intersection_dist = t - dt
                        
                        if intersection_dist < ray_dist:
                            ray_dist = intersection_dist
            
            # Add some noise to the readings
            ray_dist += np.random.normal(0, 0.1)
            ray_dist = max(0, min(ray_dist, max_dist))
            lidar_readings.append(ray_dist)
        
        # Calculate goal direction and distance
        dx = goal_x - robot_x
        dy = goal_y - robot_y
        goal_distance = np.sqrt(dx**2 + dy**2)
        goal_angle = np.arctan2(dy, dx)
        
        # Convert to robot's reference frame
        goal_direction = goal_angle - robot_theta
        # Normalize angle to [-pi, pi]
        while goal_direction > np.pi:
            goal_direction -= 2 * np.pi
        while goal_direction < -np.pi:
            goal_direction += 2 * np.pi
        
        # Get control action
        linear_vel, angular_vel = controller.get_action(
            np.array(lidar_readings),
            goal_direction,
            goal_distance
        )
        
        # Update robot position
        robot_x += linear_vel * np.cos(robot_theta) * dt
        robot_y += linear_vel * np.sin(robot_theta) * dt
        robot_theta += angular_vel * dt

        print("step: ", step)
        
        # Normalize theta
        robot_theta = robot_theta % (2 * np.pi)
        
        # Visualize
        if visualize and step % 5 == 0:  # Only visualize every 5 steps for speed
            plt.clf()
            plt.xlim(0, map_size)
            plt.ylim(0, map_size)
            
            # Draw obstacles
            for obs in obstacles:
                circle = plt.Circle((obs["x"], obs["y"]), obs["radius"], color='red', alpha=0.5)
                plt.gca().add_patch(circle)
            
            # Draw goal
            plt.plot(goal_x, goal_y, 'g*', markersize=15)
            
            # Draw robot
            plt.plot(robot_x, robot_y, 'bo', markersize=10)
            robot_dir_x = robot_x + np.cos(robot_theta)
            robot_dir_y = robot_y + np.sin(robot_theta)
            plt.plot([robot_x, robot_dir_x], [robot_y, robot_dir_y], 'b-')
            
            # Draw LIDAR rays
            for i, dist in enumerate(lidar_readings):
                angle = i * (2 * np.pi / 36) + robot_theta
                end_x = robot_x + dist * np.cos(angle)
                end_y = robot_y + dist * np.sin(angle)
                plt.plot([robot_x, end_x], [robot_y, end_y], 'y-', alpha=0.3)
            
            plt.title(f'Step {step}, Dist to goal: {goal_distance:.2f}')
            plt.pause(0.001)
        
        # Check if we've reached the goal
        if goal_distance < 0.5:
            print(f"Goal reached in {step} steps!")
            break
    
    if visualize:
        plt.show()

if __name__ == "__main__":
    print(check_gpu_availability())
    # Example of using the controller in a simulated environment
    #simulate_environment(visualize=True)