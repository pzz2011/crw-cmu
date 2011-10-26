package edu.cmu.ri.crw;

// TODO: finish this class!
/**
 * A service that registers a vehicle server over UDP to allow control over the 
 * networn via a proxy server 
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class UdpVehicleService {
    
    protected final VehicleServer _server;
    
    public UdpVehicleService(VehicleServer server) {
        _server = server;
    }
    
    /**
     * Terminates service processes and de-registers the service from a 
     * registry, if one was being used.
     */
    public void shutdown() {
        
    }
}
