package edu.cmu.ri.crw;

import javax.swing.event.EventListenerList;

import org.ros.message.geometry_msgs.PoseStamped;



public abstract class AbstractVehicleServer implements VehicleServer {

	public String[] WaypointStatus = {"SUCCEEDED", "READING", "MOVING", "STOPPED", "LOITER", "ERROR" 	};
	protected PoseStamped current_pose;
	protected EventListenerList listenerList = new EventListenerList();

	public void addStateListener(VehicleStateListener l) {
		listenerList.add(VehicleStateListener.class, l);
	}

	public void removeStateListener(VehicleStateListener l) {
		listenerList.remove(VehicleStateListener.class, l);
	}

	protected void sendState(Object state) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == VehicleStateListener.class) {
				((VehicleStateListener) listeners[i + 1]).receivedState(state);
			}
		}
	}

	public void addImageListener(VehicleImageListener l) {
		listenerList.add(VehicleImageListener.class, l);
	}

	public void removeImageListener(VehicleImageListener l) {
		listenerList.remove(VehicleImageListener.class, l);
	}

	protected void sendImage(Object image) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == VehicleImageListener.class) {
				((VehicleImageListener) listeners[i + 1]).receivedImage(image);
			}
		}
	}

	public void addSensorListener(int channel, VehicleSensorListener l) {
		// TODO: add support for separate channels
		listenerList.add(VehicleSensorListener.class, l);
	}

	public void removeSensorListener(int channel, VehicleSensorListener l) {
		// TODO: add support for separate channels
		listenerList.remove(VehicleSensorListener.class, l);
	}

	protected void sendSensor(int channel, Object reading) {
		// TODO: add support for separate channels
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == VehicleSensorListener.class) {
				((VehicleSensorListener) listeners[i + 1]).receivedSensor(reading);
			}
		}
	}

	
}
