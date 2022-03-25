/**
 * Shared enum to keep server operations in line
 */
public enum ServerOperations {
    CONNECT("CONNECT"),
    UPLOAD("UPLOAD"),
    DOWNLOAD("DOWNLOAD"),
    DELETE("DELETE"),
    DIR("DIR"),
    CLOSE("CLOSE"),
    HEARTBEAT("HB"), // short message to satisfy timeout
    ACKNOWLEDGE("ACK");

    private final String inputValue;

    ServerOperations(String inputValue) {
        this.inputValue = inputValue;
    }

    public String getInputValue() {
        return inputValue;
    }

    public static ServerOperations getOperation(String inputValue) {
        for (ServerOperations operation : values()) {
            if (operation.getInputValue().equals(inputValue)) {
                return operation;
            }
        }

        return null;
    }
}
