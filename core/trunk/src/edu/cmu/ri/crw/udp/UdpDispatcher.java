/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.crw.udp;

/**
 * Just some sketches of helper functions, don't use these for anything.
 *
 * Assumptions:
 * - Server function calls are FAST w.r.t. RPC timeouts
 * - Spurious packet loss is common
 * - Packets may be delayed for a relatively long time (~1s) due to lossy WLAN
 *
 * Overview of three-way RPC from client
 * 1. Send function call: <12131, "GET_STATE", args>
 * 2. No response after timeout
 * 3. Send function call: <12131, "GET_STATE", args>
 * 4. Get response: <12131, response>
 * 5. Send function ack: <12131, "OK">
 */
public class UdpDispatcher {

}
