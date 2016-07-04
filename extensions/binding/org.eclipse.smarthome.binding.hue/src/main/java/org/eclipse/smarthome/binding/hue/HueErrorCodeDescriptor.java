package org.eclipse.smarthome.binding.hue;

public enum HueErrorCodeDescriptor {
	BRIDGE_NOT_RESPONDING(1, "The hue bridge doesn't respond. Please doublecheck the provided ip address."),
    IP_ADDRESS_MISSING(2, "No ip address for the hue bridge has been provided.");
	
	public static final String CONFIG_FLOW_ERROR_PREFIX = "setup-flow.hue.error.";

	private int code;
    private String message;

    private HueErrorCodeDescriptor(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public int getCode() {
        
		return code;
    }

    public String getMessage() {
        
		return message;
    }

	public String getBindingPrefix() {
		
		return CONFIG_FLOW_ERROR_PREFIX;
	}
}
