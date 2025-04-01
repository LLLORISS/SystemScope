package nm.sc.systemscope.ScopeHardware;

import org.jetbrains.annotations.NotNull;
import oshi.hardware.UsbDevice;
import java.util.List;

/**
 * Represents a USB device with additional functionality to wrap and extend the {@link UsbDevice} interface.
 * This class provides a custom implementation with enhanced flexibility and default values for null properties.
 */
public class ScopeUsbDevice implements UsbDevice {

    private final String deviceName;
    private final String vendor;
    private final String vendorID;
    private final String productID;
    private final String serialNumber;
    private final String uniqueDeviceID;
    private final List<UsbDevice> connectedDevice;

    /**
     * Constructs a ScopeUsbDevice by wrapping an existing {@link UsbDevice} instance.
     *
     * @param device the existing USB device to wrap
     */
    public ScopeUsbDevice(UsbDevice device){
        this.deviceName = device.getName();
        this.vendor = device.getVendor();
        this.vendorID = device.getVendorId();
        this.productID = device.getProductId();
        this.serialNumber = device.getSerialNumber();
        this.uniqueDeviceID = device.getUniqueDeviceId();
        this.connectedDevice = device.getConnectedDevices();
    }

    /**
     * Returns the name of the USB device. If the name is null, returns "Unknown Device".
     *
     * @return the name of the USB device
     */

    @Override public String getName() {
        return deviceName != null ? deviceName : "Unknown Device";
    }

    /**
     * Returns the vendor name of the USB device. If the vendor name is null, returns "Unknown Vendor".
     *
     * @return the vendor name of the USB device
     */
    @Override public String getVendor() {
        return vendor != null ? vendor: "Unknown Vendor";
    }

    /**
     * Returns the vendor ID of the USB device. If the vendor ID is null, returns "Unknown Vendor ID".
     *
     * @return the vendor ID of the USB device
     */
    @Override public String getVendorId(){
        return vendorID != null ? vendorID: "Unknown Vendor ID";
    }

    /**
     * Returns the product ID of the USB device. If the product ID is null, returns "Unknown Product ID".
     *
     * @return the product ID of the USB device
     */
    @Override public String getProductId(){
        return productID != null ? productID: "Unknown Product ID";
    }

    /**
     * Returns the serial number of the USB device. If the serial number is null, returns "Unknown Serial Number".
     *
     * @return the serial number of the USB device
     */
    @Override public String getSerialNumber(){
        return serialNumber != null ? serialNumber: "Unknown Serial Number";
    }

    /**
     * Returns the unique device ID of the USB device. If the unique device ID is null, returns "Unknown Unique Device ID".
     *
     * @return the unique device ID of the USB device
     */
    @Override public String getUniqueDeviceId(){
        return uniqueDeviceID != null ? uniqueDeviceID: "Unknown Unique Device ID";
    }

    /**
     * Returns the list of connected USB devices. If no devices are connected, returns an empty list.
     *
     * @return the list of connected USB devices
     */
    @Override public List<UsbDevice> getConnectedDevices() {
        return connectedDevice;
    }

    /**
     * Returns a string representation of the USB device, defaulting to its name.
     *
     * @return the string representation of the device
     */
    @Override public String toString() {
        return getName();
    }

    /**
     * Compares this USB device to another based on their names.
     *
     * @param o the other USB device to compare with
     * @return a negative integer, zero, or a positive integer as this device name is less than, equal to, or greater than the specified device name
     * @throws NullPointerException if the other device is null
     */
    @Override public int compareTo(@NotNull UsbDevice o) {
        return this.deviceName.compareTo(o.getName());
    }
}
