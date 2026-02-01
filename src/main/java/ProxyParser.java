import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.stream.Collectors;

public class ProxyParser {

    public static ProxyResponse parseProxyJson(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, ProxyResponse.class);
    }

    public static List<Proxy> convertToJavaProxies(ProxyResponse response) {
        return response.getCustomProxyList().stream()
                .filter(CustomProxyItem::isEnabled) // Только включенные прокси
                .map(item -> {
                    ProxyInfo proxyInfo = item.getProxy();
                    Proxy.Type type = mapProxyType(proxyInfo.getType());
                    InetSocketAddress address = new InetSocketAddress(
                            proxyInfo.getAddress(),
                            proxyInfo.getPort()
                    );
                    return new Proxy(type, address);
                })
                .collect(Collectors.toList());
    }

    static Proxy.Type mapProxyType(String type) {
        if (type == null) {
            return Proxy.Type.HTTP;
        }

        switch (type.toUpperCase()) {
            case "HTTP":
                return Proxy.Type.HTTP;
            case "SOCKS":
                return Proxy.Type.SOCKS;
            case "DIRECT":
                return Proxy.Type.DIRECT;
            default:
                return Proxy.Type.HTTP; // По умолчанию HTTP
        }
    }

    public static List<Proxy> getFastestProxies(ProxyResponse response, int count) {
        return response.getCustomProxyList().stream()
                .filter(CustomProxyItem::isEnabled)
                .sorted((a, b) -> Double.compare(
                        a.getProxy().getResponseTime(),
                        b.getProxy().getResponseTime()
                ))
                .limit(count)
                .map(item -> {
                    ProxyInfo proxyInfo = item.getProxy();
                    Proxy.Type type = mapProxyType(proxyInfo.getType());
                    InetSocketAddress address = new InetSocketAddress(
                            proxyInfo.getAddress(),
                            proxyInfo.getPort()
                    );
                    return new Proxy(type, address);
                })
                .collect(Collectors.toList());
    }
}

// Корневой класс для всего ответа
class ProxyResponse {

    @JsonProperty("customProxyList")
    private List<CustomProxyItem> customProxyList;

    public List<CustomProxyItem> getCustomProxyList() {
        return customProxyList;
    }

    public void setCustomProxyList(List<CustomProxyItem> customProxyList) {
        this.customProxyList = customProxyList;
    }
}

// Элемент списка прокси
class CustomProxyItem {

    @JsonProperty("proxy")
    private ProxyInfo proxy;

    @JsonProperty("rangeRequestsSupported")
    private boolean rangeRequestsSupported;

    @JsonProperty("filter")
    private String filter;

    @JsonProperty("pac")
    private boolean pac;

    @JsonProperty("reconnectSupported")
    private boolean reconnectSupported;

    @JsonProperty("enabled")
    private boolean enabled;

    @JsonProperty("response_time")
    private double responseTime;

    // Геттеры и сеттеры
    public ProxyInfo getProxy() {
        return proxy;
    }

    public void setProxy(ProxyInfo proxy) {
        this.proxy = proxy;
    }

    public boolean isRangeRequestsSupported() {
        return rangeRequestsSupported;
    }

    public void setRangeRequestsSupported(boolean rangeRequestsSupported) {
        this.rangeRequestsSupported = rangeRequestsSupported;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public boolean isPac() {
        return pac;
    }

    public void setPac(boolean pac) {
        this.pac = pac;
    }

    public boolean isReconnectSupported() {
        return reconnectSupported;
    }

    public void setReconnectSupported(boolean reconnectSupported) {
        this.reconnectSupported = reconnectSupported;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public double getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(double responseTime) {
        this.responseTime = responseTime;
    }
}

// Информация о прокси
class ProxyInfo {

    @JsonProperty("address")
    private String address;

    @JsonProperty("port")
    private int port;

    @JsonProperty("type")
    private String type;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("preferNativeImplementation")
    private boolean preferNativeImplementation;

    @JsonProperty("resolveHostName")
    private boolean resolveHostName;

    @JsonProperty("connectMethodPreferred")
    private boolean connectMethodPreferred;

    @JsonProperty("response_time")
    private double responseTime;

    // Геттеры и сеттеры
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPreferNativeImplementation() {
        return preferNativeImplementation;
    }

    public void setPreferNativeImplementation(boolean preferNativeImplementation) {
        this.preferNativeImplementation = preferNativeImplementation;
    }

    public boolean isResolveHostName() {
        return resolveHostName;
    }

    public void setResolveHostName(boolean resolveHostName) {
        this.resolveHostName = resolveHostName;
    }

    public boolean isConnectMethodPreferred() {
        return connectMethodPreferred;
    }

    public void setConnectMethodPreferred(boolean connectMethodPreferred) {
        this.connectMethodPreferred = connectMethodPreferred;
    }

    public double getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(double responseTime) {
        this.responseTime = responseTime;
    }
}
