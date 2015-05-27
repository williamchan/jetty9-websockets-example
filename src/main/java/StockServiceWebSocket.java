import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebSocket
public class StockServiceWebSocket {

  private Session session;
  private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  // called when the socket connection with the browser is established
  @OnWebSocketConnect
  public void handleConnect(Session session) {
    this.session = session;
  }

  // called when the connection closed
  @OnWebSocketClose
  public void handleClose(int statusCode, String reason) {
    System.out.println("Connection closed with statusCode="
            + statusCode + ", reason=" + reason);
  }

  // called when a message received from the browser
  @OnWebSocketMessage
  public void handleMessage(String message) {
    if ("start".equals(message)) {
      send("Stock service started!");
      final Runnable runnable = new Runnable() {
        public void run() {
          send(StockService.getStockInfo());
        }
      };
      executor.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);
    } else {
      this.stop();
    }
  }

  // called in case of an error
  @OnWebSocketError
  public void handleError(Throwable error) {
    error.printStackTrace();
  }

  // sends message to browser
  private void send(String message) {
    try {
      if (session.isOpen()) {
        session.getRemote().sendString(message);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // closes the socket
  private void stop() {
    try {
      session.disconnect();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}