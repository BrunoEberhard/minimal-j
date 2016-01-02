import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.nanoserver.MjWebSocketDaemon;

public class HerokuHttpdApplication {
	private static final int TIME_OUT = 5 * 60 * 1000;
	
	public static void main(final String[] args) throws Exception {
		Frontend.setInstance(new JsonFrontend());
		Application.initApplication(args);
		MjWebSocketDaemon webSocketDaemon = new MjWebSocketDaemon(Integer.valueOf(System.getenv("PORT")), false);
		webSocketDaemon.start(TIME_OUT);
		
		while (true) {
	        try {
	            Thread.sleep(1000);
	        } catch (Throwable ignored) {
	        }
		}
		
//        webSocketDaemon.stop();
//        System.out.println("Server stopped.\n");
	}
}
