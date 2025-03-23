import numpy as np
import matplotlib as mpl
from matplotlib.patches import Polygon

class Object:
    def __init__(self, x, y, rotation, size):
        self.location = (x, y)
        self.rotation = rotation
        self.size = size
    
    def signedDistance(location=(0,0)):
        return np.Infinity
    
    def plot(self, plt):
        pass


# https://iquilezles.org/articles/distfunctions2d/ for all distances

class Rectangle(Object):
    def __init__(self, x, y, rotation, width, height):
        """
        Initialize a rectangle with center, rotation, width, and height.
        
        Args:
            center (tuple): (x, y) coordinates of the rectangle's center
            rotation (float): Rotation angle in radians
            width (float): Width of the rectangle
            height (float): Height of the rectangle
        """
        self.center = np.array((x, y))
        self.rotation = rotation
        self.width = width
        self.height = height
        
        # Pre-calculate corners for later use
        self._calculate_corners()
        
    def _calculate_corners(self):
        """Calculate the four corners of the rectangle."""
        # Get half dimensions
        hw = self.width / 2
        hh = self.height / 2
        
        # Calculate corners relative to origin
        corners_rel = np.array([
            [-hw, -hh],
            [hw, -hh],
            [hw, hh],
            [-hw, hh]
        ])
        
        # Create rotation matrix
        cos_r = np.cos(self.rotation)
        sin_r = np.sin(self.rotation)
        rotation_matrix = np.array([
            [cos_r, -sin_r],
            [sin_r, cos_r]
        ])
        
        # Rotate corners and add center
        self.corners = np.array([
            rotation_matrix @ corner + self.center for corner in corners_rel
        ])
        
    def signedDistance(self, point):
        """
        Calculate the signed distance from a point to the rectangle.
        Negative if inside, positive if outside.
        
        Args:
            point (tuple or np.ndarray): (x, y) coordinates of the point
        
        Returns:
            float: Signed distance to the rectangle
        """
        point = np.array(point)
        
        # Transform point to rectangle's local coordinate system
        translated = point - self.center
        
        # Rotate back
        cos_r = np.cos(-self.rotation)
        sin_r = np.sin(-self.rotation)
        rotation_matrix = np.array([
            [cos_r, -sin_r],
            [sin_r, cos_r]
        ])
        local_point = rotation_matrix @ translated
        
        # Calculate signed distance in local coordinates
        dx = np.abs(local_point[0]) - self.width / 2
        dy = np.abs(local_point[1]) - self.height / 2
        
        # Outside - return Euclidean distance to closest edge
        if dx > 0 and dy > 0:
            return np.sqrt(dx**2 + dy**2)
        # Outside - closest to vertical or horizontal edge
        elif dx > 0:
            return dx
        elif dy > 0:
            return dy
        # Inside - return negative distance to closest edge
        else:
            return -min(abs(dx), abs(dy))
    
    def ray_intersection(self, origin, direction):
        """
        Check if a ray intersects with the rectangle.
        
        Args:
            origin (tuple or np.ndarray): (x, y) coordinates of the ray origin
            direction (tuple or np.ndarray): (dx, dy) direction vector of the ray
            
        Returns:
            tuple or None: Intersection point (x, y) if ray intersects, None otherwise
        """
        origin = np.array(origin)
        direction = np.array(direction)
        direction = direction / np.linalg.norm(direction)  # Normalize
        
        # Get pairs of corners to form edges
        edges = [(self.corners[i], self.corners[(i+1)%4]) for i in range(4)]
        
        closest_intersection = None
        closest_distance = float('inf')
        
        for p1, p2 in edges:
            # Calculate coefficients for line-line intersection
            x1, y1 = origin
            x2, y2 = origin + direction
            x3, y3 = p1
            x4, y4 = p2
            
            # Check if lines are parallel
            denominator = (y4-y3)*(x2-x1) - (x4-x3)*(y2-y1)
            if abs(denominator) < 1e-10:
                continue
                
            # Calculate intersection parameters
            ua = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / denominator
            ub = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / denominator
            
            # Check if intersection is on both line segments
            if ua >= 0 and 0 <= ub <= 1:
                intersection = origin + ua * direction
                distance = ua
                
                if distance < closest_distance:
                    closest_distance = distance
                    closest_intersection = intersection
        
        return closest_intersection
    
    def plot(self, plt=None, clr="red", markers=True, center_clr="ro"):
        """
        Plot the rectangle on a matplotlib axis.
        
        Args:
            ax (matplotlib.axes.Axes, optional): Axis to plot on. If None, use current axis.
            
        Returns:
            matplotlib.axes.Axes: The axis with the rectangle plotted
        """
        if plt == None:
            return
        
        ax = plt.gca()
            
        # Create a polygon patch
        polygon = Polygon(self.corners, closed=True, fill=True, color=clr, alpha=0.5)
        ax.add_patch(polygon)
        
        if markers:
            # Plot corners
            ax.plot(self.corners[:, 0], self.corners[:, 1], 'ro', markersize=3)
        
        # Plot center
        ax.plot(self.center[0], self.center[1], center_clr, markersize=5)
        
        return ax
    
    def __repr__(self):
        return f"Rectangle(center={self.center}, rotation={self.rotation}, width={self.width}, height={self.height})"

#Rectangle.sd = staticmethod(Rectangle.sd)
