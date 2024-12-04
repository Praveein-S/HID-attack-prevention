import usb.core
import usb.util
import platform
import os

# Whitelist: Replace with valid Vendor ID (VID) and Product ID (PID) pairs of allowed devices
WHITELIST = [
    {"vid": 0x1234, "pid": 0x5678},  # Example allowed device 1
    {"vid": 0x8765, "pid": 0x4321},  # Example allowed device 2
]

def get_device_description(device):
    """
    Get a human-readable description of the USB device.
    """
    try:
        manufacturer = usb.util.get_string(device, device.iManufacturer) or "Unknown Manufacturer"
        product = usb.util.get_string(device, device.iProduct) or "Unknown Product"
        print(f"{manufacturer} - {product}")
        print("dkskdj")
        return f"{manufacturer} - {product}"
    except Exception as e:
        return f"Unknown Device (Error: {e})"

def is_device_whitelisted(device):
    """
    Check if the device is in the whitelist.
    """
    for item in WHITELIST:
        if device.idVendor == item["vid"] and device.idProduct == item["pid"]:
            return True
    return False

def block_device(device):
    """
    Block a detected device by disabling it.
    """
    system = platform.system()
    if system == "Windows":
        block_device_windows(device)
    elif system == "Linux":
        block_device_linux(device)
    else:
        print(f"Blocking is not supported on {system}.")

def block_device_windows(device):
    """
    Block device on Windows using devcon.exe.
    """
    device_id = f"USB\\VID_{device.idVendor:04X}&PID_{device.idProduct:04X}"
    print(f"Blocking device on Windows: {device_id}")
    os.system(f"devcon disable \"{device_id}\"")

def block_device_linux(device):
    """
    Block device on Linux using udevadm.
    """
    print(f"Blocking device on Linux: VID={hex(device.idVendor)}, PID={hex(device.idProduct)}")
    os.system(f"echo '1-1' | sudo tee /sys/bus/usb/devices/{device.bus}-{device.address}/authorized")

def detect_and_block_hid_devices():
    """
    Detect HID devices, check against whitelist, and block if not whitelisted.
    """
    print("Scanning for HID devices...")
    devices = usb.core.find(find_all=True)
    print(devices)
    devices = usb.core.find(find_all=True)
    for device in devices:
        print(f"Device: VID={hex(device.idVendor)}, PID={hex(device.idProduct)}")

    # # print(len(devices))
    # for device in devices:
    #     # HID devices have bDeviceClass = 3
    #     if device.bDeviceClass == 3:
    #         description = get_device_description(device)
    #         print(f"Detected HID: VID={hex(device.idVendor)}, PID={hex(device.idProduct)}, Description: {description}")
    #         if not is_device_whitelisted(device):
    #             print("Device is NOT whitelisted! Blocking it now...")
    #             block_device(device)
    #         else:
    #             print("Device is whitelisted. No action needed.")

if __name__ == "__main__":
    detect_and_block_hid_devices()
