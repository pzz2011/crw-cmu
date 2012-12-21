/**
 * Airboat Control Firmware - Rudder
 *
 * Contains control and update code interfacing with a servo-driven
 * mechanical rudder.  This code runs a PID loop attempting to match
 * a desired yaw velocity using feedback from a gyro.
 */
#include "rudder.h"
#include "servo.h"
#include "meet_android.h"

#define RBUFSIZE  100
#define RMIN  -1000
#define RMAX  1000

#define RECV_RUDDER_POS 'r'

int pos;
float rBuffer[RBUFSIZE];
float rprevError = 0;
float rBufferSum = 0;
int rIndx = 0;

int send_pos_cnt = 3;

void initRudder() {
    init_servos();

    for (int i = 0; i < 100; i++)
        rBuffer[i] = 0;
}

void updateRudder() {
    float rError = desiredVelocity[5] - actualVelocity[5];

    rIndx++;
    if (rIndx == RBUFSIZE)
        rIndx = 0;

    rBufferSum -= rBuffer[rIndx];
    rBufferSum += rError;
    rBuffer[rIndx] = rError;

    float rPID = (pid.Kp[5] * rError) + (pid.Kd[5] * ((rError - rprevError) / (RUDDER_UPDATE_INTERVAL_MS))) + (pid.Ki[5] * rBufferSum);
    rprevError = rError;

    if (rPID < RMIN)
        rPID = RMIN;
    else
        if (rPID > RMAX)
        rPID = RMAX;

    set_servo(RUDDER_SERVO_CHANNEL, RUDDER_SERVO_WHICH, rPID);

    send_pos_cnt++;

    if (send_pos_cnt > 10) {
        amarino.send(RECV_RUDDER_POS);
        amarino.send(pos);
        amarino.sendln();

        send_pos_cnt = 0;
    }
}
