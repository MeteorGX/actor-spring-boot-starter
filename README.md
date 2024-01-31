# Getting Started

Actor-like multi-threading library based on SpringBoot, developing a relatively simple websocket server.

## Example

Import the following libraries | 引入以下的类库:

> note: websocket is not required, can switch netty to enable tcp | websocket 并不是必须, 可以切换 netty 启用 tcp

```xml

<dependencies>
    <!-- websocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>

    <dependency>
        <groupId>com.meteorcat.spring.boot</groupId>
        <artifactId>actor-spring-boot-starter</artifactId>
        <version>{actor-starter-version}</version>
    </dependency>
</dependencies>
```

First, create actor config | 首先, 创建 Actor 配置:

```java

@Configuration
public class ActorConfig {

    private final ApplicationContext context;


    public ActorConfig(ApplicationContext context) {
        this.context = context;
    }


    @Bean
    public ActorEventContainer searchActor() {
        return new ActorEventContainer(new ActorEventMonitor(5), context);
    }
}
```

Afterwards, generate websocket server | 之后, 生成 websocket 服务:

```java
/**
 * Global WebSocket Service
 */
@Order
@Component
public class WebSocketApplication extends TextWebSocketHandler {

    /**
     * Logger
     */
    Logger logger = LoggerFactory.getLogger(WebSocketApplication.class);


    /**
     * JSON Parser
     */
    ObjectMapper mapper = new ObjectMapper();


    /**
     * Actor events
     */
    final ActorEventContainer container;

    /**
     * construct method
     *
     * @param container Actor events
     */
    public WebSocketApplication(ActorEventContainer container) {
        this.container = container;
    }


    /**
     * Established
     *
     * @param session handler
     * @throws Exception Error
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        logger.debug("Established = {}", session);
    }


    /**
     * Closed
     *
     * @param session handler
     * @param status  close state
     * @throws Exception Error
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        logger.debug("Close = {},{}", session, status);
    }


    /**
     * Text - Callback
     *
     * @param session Websocket
     * @param message data
     * @throws Exception Error
     */
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        logger.debug("Message = {},{}| Thread = {}", session, message.getPayload(), Thread.currentThread().getId());

        // empty?
        String payload = message.getPayload();
        if (payload.isBlank()) return;

        // format { op: int, args: object }
        JsonNode json = mapper.readTree(payload);
        if (!json.isObject()) {
            logger.error("failed by transfer data");
            return;
        }

        // op or args exist?
        if (!json.has("op") || !json.has("args")) {
            logger.error("failed by transfer data");
            return;
        }

        // op is int? args is object?
        JsonNode opNode = json.get("op");
        JsonNode argsNode = json.get("args");
        if (!opNode.isInt() || !argsNode.isObject()) {
            logger.error("failed by transfer data");
            return;
        }

        // push message queue
        Integer op = opNode.asInt();
        ActorConfigurer configurer = container.get(op);
        if (configurer != null) {
            configurer.invoke(op, 0, container, session, argsNode);
        }
    }
}
```

By the way, don't forget the websocket configuration | 随便说下, 不要忘记 websocket 配置:

```java

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * path
     */
    @Value("${websocket.server.path:/}")
    private String serverPath;

    /**
     * buffer
     */
    @Value("${websocket.buffer.max.size:8192}")
    private Integer bufferMaxSize;


    /**
     * timeout
     */
    @Value("${websocket.idle.timeout:600000}")
    private Long idleTimeout;


    /**
     * origins
     */
    @Value("${websocket.allowed.origins:*}")
    private String allowOrigins;


    /**
     * load @Component websocket
     */
    private final WebSocketApplication handler;

    /**
     * get @Component websocket
     *
     * @param handler instance
     */
    public WebSocketConfig(WebSocketApplication handler) {
        this.handler = handler;
    }


    /**
     * register websocket
     *
     * @param registry register handler
     */
    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        if (handler == null) {
            throw new RuntimeException("failed by WebSocketHandler: WebSocketHandler");
        }
        registry.addHandler(handler, serverPath).setAllowedOrigins(allowOrigins);
    }


    /**
     * global Servlet configs
     *
     * @return ServletServerContainerFactoryBean
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(bufferMaxSize);
        container.setMaxBinaryMessageBufferSize(bufferMaxSize);
        container.setMaxSessionIdleTimeout(idleTimeout);
        return container;
    }
}
```

Okay, now it's time to write the logic code | 好的, 现在可以编写逻辑代码:

```java

@EnableActor(owner = EchoLogic.class)
public class EchoLogic extends ActorConfigurer {


    /**
     * echo server
     * request: { "op": 100, "args":{ "text": "hello.world"}}
     * response: { "text": "hello.world"}
     *
     * @param session websocket handler
     * @param node    JSON
     * @throws IOException ERROR
     */
    @ActorMapping(value = 100)
    public void echo(ActorEventContainer container, WebSocketSession session, JsonNode node) throws IOException {
        session.sendMessage(new TextMessage(node.toString()));
    }

    public Map<WebSocketSession, ScheduledFuture<?>> keepAlive = new HashMap<>();

    /**
     * Init
     *
     */
    @Override
    public void init() {
        logger.warn("server startup");
    }

    /**
     * Exit
     */
    @Override
    public void destroy() {
        logger.warn("server exit");
    }

    /**
     * keep-alive
     * request: { "op": 111, "args":{ }}
     * response: timestamp
     *
     * @param session websocket handler
     * @throws IOException ERROR
     */
    @ActorMapping(value = 111)
    public void tick(ActorEventContainer container, WebSocketSession session, JsonNode node) throws IOException {
        if (!keepAlive.containsKey(session)) {
            ScheduledFuture<?> future = container.scheduleAtFixedRate(() -> {
                if (!session.isOpen()) {
                    keepAlive.get(session).cancel(true);
                    keepAlive.remove(session);
                }

                try {
                    long timestamp = System.currentTimeMillis();
                    session.sendMessage(new TextMessage(Long.toString(timestamp)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, 5, 5, TimeUnit.SECONDS);
            keepAlive.put(session, future);
        }
    }
}
```

Request json data, test the logical code | 请求 JSON 数据, 测试这些逻辑代码:

```json lines
// echo mapping
{
  "op": 100,
  "args": {
    "text": "hello.world"
  }
}

// keep-alive request
{
  "op": 111,
  "args": {}
}
```

> note: If the Bean thread manager is unable to call scheduled tasks, please use the configuration '
> spring.main.allow-bean-definition-overriding=true' 



