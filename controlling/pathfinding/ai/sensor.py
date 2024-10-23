
"""
This class is used to represent a sensor. It has an address and a list of values.
"""
class Sensor:
    """
    A sensor has an address and a list of values.
    :param address: The address of the sensor. (more like identification, but this is fine)
    :param n_values: The number of values the sensor has.
    """
    def __init__(self, address, n_values):
        self.address = address
        self.values = []
        self.n_values = n_values
        for i in range(n_values):
            self.values.append(0)

    """
    Updates the values of the sensor.
    :param array_doubles: The new values of the sensor.
    """
    def update(self, array_doubles):
        for i in range(self.n_values):
            self.values[i] = array_doubles[i]

    """
    Updates the value of the sensor at the given index.
    :param index: The index of the value to update.
    :param value: The new value of the sensor.
    """
    def update_value(self, index, value):
        self.values[index] = value

    """
    Gets the value of the sensor at the given index.
    :param index: The index of the value to get.
    :return: The value of the sensor at the given index.
    """
    def get_value(self, index):
        return self.values[index]

    """
    Gets the values of the sensor.
    :return: The values of the sensor.
    """
    def get_values(self):
        return self.values



