
import sys
import math
import xml.etree.ElementTree as ET

from croblink import *

####################################################################################

CELLROWS = 7
CELLCOLS = 14

####################################################################################

class MyRob(CRobLink):
    def __init__(self, rob_name, rob_id, host):
        CRobLink.__init__(self, rob_name, rob_id, host)

    def readSensors(self):
        while True:
            CRobLink.readSensors(self)
            if self.measures.start: break

    def wait(self, n):
        for i in range(n):
            self.readSensors()

    def run(self):
        if self.status != 0:
            print("Connection refused or error")
            quit()
        self.wait(1)

        # rotate to face North
        print("Rotating to face North")
        a = -self.measures.compass * math.pi / 180.0
        v = (a * 0.5) / 16
        self.driveMotors(-v, v)
        self.wait(1)

        self.wait(15)

        # go ahead for 100 cycles
        print("Going ahead for", 100, "cycles")
        self.driveMotors(0.1, 0.1)
        self.wait(1)

        self.wait(100-1)

        self.driveMotors(0, 0)
        self.wait(1)

        self.finish()
        self.wait(1)


####################################################################################


if __name__ == '__main__':
    name = "Topo gigio"
    host = "127.0.0.1"
    rob = MyRob(name, 0, host)
    
    rob.run()

