
"""
Clamp a value between a minimum and maximum value.
value: The value to clamp.
min_value: The minimum value.
max_value: The maximum value.
"""
def clamp(value, min_value, max_value):
    return max(min(value, max_value), min_value)