package serverfacade;

public class ServerException extends Exception {
  private int statusCode;

  public ServerException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public ServerException(String message) {
    super(message);
    this.statusCode = -1;
  }

  public int getStatusCode() {
    return statusCode;
  }
}

