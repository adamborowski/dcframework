class CyclicRegister:
    def __init__(self, array):
        self.array = array
        self.count = len(array)
        self.current = 0
        self.overflowOccured = False

    def get(self):
        return self.array[self.current]

    def next(self):
        self.current += 1
        if self.current == self.count:
            self.current = 0
            self.overflowOccured = True
            return True
        return False

    def isOverflowOccured(self):
        return self.overflowOccured


class OverflowManager:
    def __init__(self, registers, initial_state=0):
        self.registers = registers
        for i in range(0, initial_state):
            self.next()

    def next(self):
        for register in self.registers:
            overflowOccured = register.next()
            if not overflowOccured:
                break

    def isOverflowOccured(self):
        return self.registers[-1].isOverflowOccured()

    def getArray(self):
        return [register.get() for register in self.registers]

    def getIndices(self):
        return [str(register.current) for register in self.registers]
