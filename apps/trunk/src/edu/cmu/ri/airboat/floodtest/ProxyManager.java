/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.floodtest;

import gov.nasa.worldwind.render.markers.Marker;
import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class ProxyManager {

    private static Singleton instance = new Singleton();
    private Random rand = new Random();

    public void redraw() {
        instance.redraw();
    }

    void setConsole(OperatorConsole c) {
        instance.setConsole(c);
    }

    public ArrayList<Marker> getMarkers() {
        return instance.getMarkers();
    }

    public static void setCameraRates(double d) {
        instance.setCameraRates(d);
    }
    
    public BoatSimpleProxy getRandomProxy() {

        if (instance.boatProxies.isEmpty()) {
            return null;
        }

        return instance.boatProxies.get(rand.nextInt(instance.boatProxies.size()));
    }

    public static void remove(BoatSimpleProxy proxy) {
        instance.remove(proxy);
    }
    
    public boolean createSimulatedBoatProxy(String name, URI uri, Color color) {        
        
        instance.createBoatProxy(name, uri, color);

        return true;
    }

    public boolean createVBSBoatProxy(int port) {

        /*
        try {
            instance.createBoatProxy(new URI("http://localhost:" + port), Material.YELLOW);
        } catch (URISyntaxException e) {
            System.out.println("Problem with URI: " + e);
        }
         * */

        System.out.println("\n\n\nUnimplemented create VBS proxy!!!!!\n\n\n");
        return true;
    }

    public boolean createPhysicalBoatProxy(String name, String host, Color color) {

        try {
            System.out.println("Creating physical boat proxy");
            instance.createBoatProxy(name, new URI(host), color);
        } catch (URISyntaxException e) {
            System.out.println("Problem with URI: " + e);
        }
        return true;
    }

    public void shutdown() {
        instance.shutdown();
    }
    
    private static class Singleton {

        public ArrayList<Marker> markers = new ArrayList<Marker>();
        ArrayList<BoatSimpleProxy> boatProxies = new ArrayList<BoatSimpleProxy>();
        HashMap<URI, BoatSimpleProxy> boatMap = new HashMap<URI, BoatSimpleProxy>();
        OperatorConsole console = null;

        public Singleton() {
           
        }
        
        public void createBoatProxy(String name, URI uri, Color color) {
        
            try {
                BoatSimpleProxy proxy = new BoatSimpleProxy(name, markers, color, 1, uri, "vehicle_client" + (int) (new Random().nextInt(1000000)));               
                boatProxies.add(proxy);
                boatMap.put(uri, proxy);
                
                if (console != null) {
                    console.setSelected(proxy);
                }
                
                proxy.start();
                
            } catch (Exception e) {
                System.out.println("Creating proxy failed: " + e);
                e.printStackTrace();
            }
        }

        private void redraw() {
            if (console != null) {
                console.redraw();
            }
        }

        public void setConsole(OperatorConsole console) {
            this.console = console;
        }

        public ArrayList<Marker> getMarkers() {
            return markers;
        }
        
        public void setCameraRates(double d) {
            System.out.println("Setting camera speeds to " + d);
            for (BoatSimpleProxy p : boatProxies) {
                p._server.stopCamera();
                p._server.startCamera(0, d, 640, 480, null);
            }
        }

        private void remove(BoatSimpleProxy proxy) {
            boatProxies.remove(proxy);
            // @todo Proxies are not removed from hash table, expecting that something else with a new URI will override
            // boatMap.remove(proxy.)
        }

        private void shutdown() {
            for (BoatSimpleProxy p : boatProxies) {
                p._server.stopCamera();
                p._server.shutdown();
            }
        }
    }
}
