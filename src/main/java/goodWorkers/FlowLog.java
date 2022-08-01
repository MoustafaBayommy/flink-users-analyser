package goodWorkers;

/**
 * @author : moustafabayommy
 * @mailto : moustafabayommy@gmail.com
 * @created : 28/07/2022, Thursday
 **/
public class FlowLog {
    String userId;
    String deviceId;
    String clientId;
    String clientVersion;
    String sessionId;
    String logType;
    String  eventName;
    String log;
    Long timeStamp;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "FlowLog{" +
                "userId='" + userId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientVersion='" + clientVersion + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", logType='" + logType + '\'' +
                ", eventName='" + eventName + '\'' +
                ", log='" + log + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }
}
