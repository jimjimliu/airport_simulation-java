# CSI3131_airport_simulation java



## SIMULATING AN AIRPORT
Consider an airport used for launching international tourist flights. The airport has 4 launching/landing pads. There are 6 aeroplanes offering flights to 4 possible destinations. The flight destination is determined by the launching/landing pad used. Each aeroplane is capable of taking 3 passengers, and can go to any of the destinations. At the beginning, all aeroplanes start with zero passengers. There are 20 passengers. The behaviour of aeroplanes and passengers is captured in the following pseudo code:
### Aeroplane:
for (;;) {
    Wait until there is an empty launch/landing pad.
    Land at the airport on an empty pad.
    The passengers currently in the aeroplane (if any) leave the plane. Announce that the passengers can board to the destination
    determined by the pad used.
    The waiting passengers traveling to that destination board the
    aeroplane.
    The aeroplane is launched when it becomes full.
    Wait for random time 500..2000ms /* traveling in the air */
}

### Passenger:
for(;;) {
    /* Makes some money to afford another flight. Note the ratio
    between work and leisure. */
    Wait for random time 0..700 ms
    Choose a random destination 0..3.
    Arrive to the airport and wait until there is an aeroplane going to
    that destination, then board it. Enjoy the flight.
    Leave the aeroplane.
    
}
### Correctness specification
The following conditions must be satisfied in order for the simulation to run correctly:
Each landing/launching pad can at any time hold at most one aeroplane. All other aeroplanes are in the air doing the flight or waiting for landing.
At any moment, there are at most 3 passengers in an aeroplane.
A passenger can board only an aeroplane sitting on the launch/landing pad, after all passengers returning from a ride have already left, and after the aeroplane has announced boarding.
An aeroplane launches into the air if and only if it is full of passengers (i.e. 3 passengers).
A passenger can leave an aeroplane only after the aeroplane has landed back at the airport.
The boarding starts only after the last passenger has left the aeroplane.
Other minor restrictions, which follow from the comments in the provided skeleton code.
