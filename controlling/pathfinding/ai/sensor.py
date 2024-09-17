
class Sensor:
    def __init__(self, address, n_values):
        self.address = address
        self.values = []
        self.n_values = n_values
        for i in range(n_values):
            self.values.append(0)

    def update(self, array_doubles):
        for i in range(self.n_values):
            self.values[i] = array_doubles[i]

    def update_value(self, index, value):
        self.values[index] = value

    def get_value(self, index):
        return self.values[index]

    def get_values(self):
        return self.values



