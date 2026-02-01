import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class ProxyManager {

    private List<Proxy> proxies = new ArrayList<>();
    private Map<Proxy, ProxyInfo> proxyInfoMap = new HashMap<>();
    private Random random = new Random();

    /**
     * Загружает прокси из JSON строки
     */
    public void loadProxiesFromJson(String json) throws Exception {
        ProxyResponse response = ProxyParser.parseProxyJson(json);
        List<CustomProxyItem> items = response.getCustomProxyList();

        for (CustomProxyItem item : items) {
            if (item.isEnabled()) {
                ProxyInfo proxyInfo = item.getProxy();
                Proxy.Type type = ProxyParser.mapProxyType(proxyInfo.getType());
                InetSocketAddress address = new InetSocketAddress(
                        proxyInfo.getAddress(),
                        proxyInfo.getPort()
                );
                Proxy proxy = new Proxy(type, address);
                proxies.add(proxy);
                proxyInfoMap.put(proxy, proxyInfo);
            }
        }

        System.out.println("Загружено " + proxies.size() + " рабочих прокси");
    }

    /**
     * Получить случайный прокси
     */
    public Proxy getRandomProxy() {
        if (proxies.isEmpty()) {
            return Proxy.NO_PROXY;
        }
        return proxies.get(random.nextInt(proxies.size()));
    }

    /**
     * Получить самый быстрый прокси
     */
    public Proxy getFastestProxy() {
        if (proxies.isEmpty()) {
            return Proxy.NO_PROXY;
        }

        return proxies.stream()
                .min(Comparator.comparingDouble(p ->
                        proxyInfoMap.get(p).getResponseTime()))
                .orElse(Proxy.NO_PROXY);
    }

    /**
     * Получить список прокси для определенного типа (HTTP, SOCKS)
     */
    public List<Proxy> getProxiesByType(Proxy.Type type) {
        return proxies.stream()
                .filter(p -> p.type() == type)
                .collect(Collectors.toList());
    }

    /**
     * Получить информацию о прокси
     */
    public ProxyInfo getProxyInfo(Proxy proxy) {
        return proxyInfoMap.get(proxy);
    }

    /**
     * Проверяет работоспособность прокси
     */
    public boolean testProxy(Proxy proxy) {
        try {
            URL url = new URL("https://www.google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("HEAD");

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Получить аутентификатор для прокси с логином/паролем
     */
    public Authenticator getAuthenticator(Proxy proxy) {
        ProxyInfo info = proxyInfoMap.get(proxy);
        if (info != null && info.getUsername() != null && info.getPassword() != null) {
            return new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            info.getUsername(),
                            info.getPassword().toCharArray()
                    );
                }
            };
        }
        return null;
    }

    public int getProxyCount() {
        return proxies.size();
    }

    public List<Proxy> getAllProxies() {
        return new ArrayList<>(proxies);
    }
}
