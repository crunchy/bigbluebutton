import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MySocketServer {

    private static final int RECEIVE_BUFFER_SIZE = 512000;

    public static void main(String args[]) throws IOException, InterruptedException {
	ServerSocket server = new ServerSocket(9124);
	server.setPerformancePreferences(0, 1, 2);
	server.setReceiveBufferSize(RECEIVE_BUFFER_SIZE);

	Socket socket = server.accept();

	BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
	long start = System.currentTimeMillis();
	int bytes = 0;

	while (true) {
	    int size = input.read(new byte[RECEIVE_BUFFER_SIZE]);

	    if (size > 0) {
		bytes += size;
		float secs = ((System.currentTimeMillis() - start) / 1000F);
		float kb = bytes / 1024F;
		float rate = kb / secs;
		System.out.println("recv: " + size / 1024F + " kbytes; kbytes/s: " + rate);
	    }
	}
    }

}
