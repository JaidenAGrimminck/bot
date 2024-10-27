from math import sqrt, inf
import math

class Projection:
    def __init__(self, min_, max_):
        self.min = min_
        self.max = max_
    
    def overlaps(self, other):
        return self.max >= other.min and other.max >= self.min

def hypot(x,y):
    return sqrt(x * x + y * y)

def projectPolygon(polygon, axis):
    min_ = inf
    max_ = -inf

    for point in polygon:
        projection = point[0] * axis[0] + point[1] * axis[1]
        min_ = min(min_, projection)
        max_ = max(max_, projection)

    return Projection(min_, max_)

"""
A and B are lists of 2d points.
"""
def separatingAxisTheorem(a, b):
    for i in range(len(a)):
            p1 = a[i]
            p2 = a[(i + 1) % len(a)]

            edge = (p2[0] - p1[0], p2[1] - p1[1])

            axis = (-edge[1], edge[0])

            projA = projectPolygon(a, axis)
            projB = projectPolygon(b, axis)

            if not projA.overlaps(projB):
                return False
    return True