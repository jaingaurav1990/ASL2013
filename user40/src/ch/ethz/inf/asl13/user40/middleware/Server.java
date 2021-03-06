package ch.ethz.inf.asl13.user40.middleware;

import java.util.HashMap;
import java.util.logging.*;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import ch.ethz.inf.asl13.user40.MessagingService;
//import ch.ethz.inf.asl13.user40.util.RequestTracer;
import ch.ethz.inf.asl13.user40.util.SingleLineFormatter;

/**
 * Implements the messaging service.
 */
public class Server {

	public static final String SERVICE_URL_KEY = "service.url";

	private static final Logger logger = Logger.getLogger(Server.class.getName());

	static {
		Formatter formatter = new SingleLineFormatter();
		Logger rootLogger = Logger.getLogger("");
		for (Handler h : rootLogger.getHandlers()) {
			h.setFormatter(formatter);
		}
	}

	public static String getServiceUrl() {
		return System.getProperty(SERVICE_URL_KEY, "http://localhost:9999/");
	}

	/**
	 * Entry point for the application.
	 * Creates and publishes the messaging service.
	 */
	public static void main(String[] args) {

		// Logger rootLogger = Logger.getLogger("");
		// rootLogger.setLevel(Level.FINE);
		// for (Handler h : rootLogger.getHandlers()) {
		// 	System.out.println("h:" + h);
		// 	h.setLevel(Level.FINE);
		// }

		publish(new MemoryMessagingService());
        // publish(new DatabaseServicePreparedStatement());
	}

	/**
	 * Creates and publishes a messaging service endpoint at the specified address.
	 * @param service The service that should be started at <var>address</var>.
	 * @return The newly created endpoint.
	 */
	public static Endpoint publish(MessagingService service) {

		//service = new RequestTracer(new MessagingServiceImpl(service));
        service = new MessagingServiceImpl(service);

		Endpoint endpoint = Endpoint.create(service);
		endpoint.setProperties(new HashMap<String, Object>() {{
			put(Endpoint.WSDL_PORT, new QName(MessagingService.NAMESPACE, MessagingService.PORT_NAME));
			put(Endpoint.WSDL_SERVICE, new QName(MessagingService.NAMESPACE, MessagingService.SERVICE_NAME));
		}});

		String address = getServiceUrl();
		endpoint.publish(address);
		logger.log(Level.INFO, "Listening at {0}…", address);
		return endpoint;
	}
}
