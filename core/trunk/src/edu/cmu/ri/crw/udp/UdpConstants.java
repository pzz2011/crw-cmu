/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.crw.udp;

/**
 * Static helper class that contains constants and definitions required by the
 * UDP communications system.
 *
 * @author Pras Velagapudi <psigen@gmail.com>
 */
public class UdpConstants {

    public static final int REGISTRATION_RATE_MS = 1000;

    public static final int RETRY_RATE_MS = 1000;
    public static final int RETRY_COUNT = 3;
    public static final int TIMEOUT_NS = RETRY_RATE_MS * (RETRY_COUNT + 1) * 1000;
    public static final int NO_TICKET = -1;

    /**
     * Enumeration of tunneled commands and the strings used in the UDP packet
     * to represent them.
     */
    public enum COMMAND {
        CMD_REGISTER_STATE_LISTENER("RSTL"),
        CMD_SET_STATE("SS"),
        CMD_GET_STATE("GS"),
        CMD_REGISTER_IMAGE_LISTENER("RIL"),
        CMD_CAPTURE_IMAGE("CI"),
        CMD_REGISTER_CAMERA_LISTENER("CIL"),
        CMD_START_CAMERA("STC"),
        CMD_STOP_CAMERA("SPC"),
        CMD_GET_CAMERA_STATUS("CS"),
        CMD_REGISTER_SENSOR_LISTENER("RSEL"),
        CMD_SET_SENSOR_TYPE("SST"),
        CMD_GET_SENSOR_TYPE("GST"),
        CMD_GET_NUM_SENSORS("GNS"),
        CMD_REGISTER_VELOCITY_LISTENER("RVL"),
        CMD_SET_VELOCITY("SV"),
        CMD_GET_VELOCITY("GV"),
        CMD_REGISTER_WAYPOINT_LISTENER("RWL"),
        CMD_START_WAYPOINTS("STW"),
        CMD_STOP_WAYPOINTS("SPW"),
        CMD_GET_WAYPOINTS("GW"),
        CMD_GET_WAYPOINT_STATUS("GWS"),
        CMD_IS_CONNECTED("IC"),
        CMD_IS_AUTONOMOUS("IA"),
        CMD_SET_AUTONOMOUS("SA"),
        CMD_SET_GAINS("SG"),
        CMD_GET_GAINS("GG");

        COMMAND(String s) {
            str = s;
        }

        public final String str;
    }

}
