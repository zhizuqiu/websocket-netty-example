package server.bean;

public class WebSocketMessage {
    private String type;
    private String group;
    private String message;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "WebSocketMessage{" +
                "type='" + type + '\'' +
                ", group='" + group + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    public WebSocketMessage() {
    }
}
