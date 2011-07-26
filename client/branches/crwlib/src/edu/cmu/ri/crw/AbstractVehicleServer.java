package edu.cmu.ri.crw;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.event.EventListenerList;

import org.ros.message.Time;
import org.ros.message.crwlib_msgs.SensorData;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CameraInfo;
import org.ros.message.sensor_msgs.CompressedImage;


public abstract class AbstractVehicleServer implements VehicleServer {

	protected double[][] _gains = new double[6][3];
	protected EventListenerList listenerList = new EventListenerList();

	public double[] getPID(int axis) {
		if (axis < 0 || axis >= _gains.length) 
			return new double[0];
		
		// Make a copy of the current state (for immutability) and return it
		double[] gains = new double[_gains[axis].length];
		System.arraycopy(_gains[axis], 0, gains, 0, _gains[axis].length);
		return gains;
	}
	
	public void setPID(int axis, double[] gains) {
		if (axis < 0 || axis >= _gains.length) 
			return;
		
		// Make a copy of the provided state (for immutability)
		System.arraycopy(gains, 0, _gains[axis], 0, Math.min(gains.length, _gains[axis].length));
	}
	
	public void addStateListener(VehicleStateListener l) {
		listenerList.add(VehicleStateListener.class, l);
	}

	public void removeStateListener(VehicleStateListener l) {
		listenerList.remove(VehicleStateListener.class, l);
	}

	protected void sendState(UtmPoseWithCovarianceStamped pose) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == VehicleStateListener.class) {
				((VehicleStateListener) listeners[i + 1]).receivedState(pose);
			}
		}
	}

	public void addImageListener(VehicleImageListener l) {
		listenerList.add(VehicleImageListener.class, l);
	}

	public void removeImageListener(VehicleImageListener l) {
		listenerList.remove(VehicleImageListener.class, l);
	}

	protected static CompressedImage toCompressedImage(RenderedImage image) {
		// This might be inefficient, but it is far more inefficient to
		// uncompress hardware-compressed JPEG images on Android.
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try { 
			ImageIO.write(image, "jpeg", buffer);
			return toCompressedImage(image.getWidth(), image.getHeight(), buffer.toByteArray());
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	protected static CompressedImage toCompressedImage(int width, int height, byte[] data) {
		CompressedImage cImage = new CompressedImage();
		CameraInfo cInfo = new CameraInfo();
		
		cImage.data = new byte[data.length];
		System.arraycopy(data, 0, cImage.data, 0, data.length);
		
		cImage.format = "jpeg";
		cImage.header.stamp = Time.fromMillis(System.currentTimeMillis());
		cImage.header.frame_id = "camera";
		
		cInfo.header.stamp = cImage.header.stamp;
		cInfo.header.frame_id = cImage.header.frame_id;
		cInfo.width = width;
		cInfo.height = height;
		// TODO: figure out what to do with cInfo?
		
		return cImage;
	}
	
	protected void sendImage(CompressedImage image) {
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

	protected void sendSensor(int channel, SensorData reading) {
		// TODO: add support for separate channels
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == VehicleSensorListener.class) {
				((VehicleSensorListener) listeners[i + 1])
						.receivedSensor(reading);
			}
		}
	}

	public void addVelocityListener(VehicleVelocityListener l) {
		listenerList.add(VehicleVelocityListener.class, l);
	}

	public void removeVelocityListener(VehicleVelocityListener l) {
		listenerList.remove(VehicleVelocityListener.class, l);
	}

	protected void sendVelocity(TwistWithCovarianceStamped velocity) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == VehicleVelocityListener.class) {
				((VehicleVelocityListener) listeners[i + 1])
						.receivedVelocity(velocity);
			}
		}
	}
}
