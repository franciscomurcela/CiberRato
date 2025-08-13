/*
 * C++ CiberRato agent doing the same as min-01.mus
 */

#include "RobSock.h"

#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <unistd.h>
#include <math.h>

/*
 * The simulator has a button that allows to stop the simulation,
 * in which case it not applies orders coming from the agent.
 */
void readSensors()
{
    do { ReadSensors(); } while (GetStartButton() == false);
}

/*
 * This allows synchronize during n cycles,
 * keeping the motors orders sent before.
 */
void wait(unsigned int n)
{
    while (n > 0) { readSensors(); n--; }
}

int main(int argc, char *argv[])
{
    /* connect to simulator */
    const char * name = "Topo Gigio";
    const char * host = "127.0.0.1";
    if (InitRobot(name, 0, host) == -1)
    {
       fprintf(stderr, "Robot \"%s\" fail connecting to simulator @address \"%s\"\n", name, host); 
       exit(1);
    }
    wait(1);

    /* rotate to face North */
    printf("Rotating to face North\n");
    double a = -GetCompassSensor() * M_PI / 180.0;
    double v = (a * 0.5) / 16;
    DriveMotors(-v, v);
    wait(1);

    wait(16-1);

    /* go ahead for 100 cycles */
    printf("Going ahead for %d cycles\n", 100);
    DriveMotors(0.1, 0.1);
    wait(1);

    wait(100-1);

    /* stop robot */
    DriveMotors(0, 0);
    wait(1);
    
    /* signal end of run */
    Finish();
    wait(1);
}
