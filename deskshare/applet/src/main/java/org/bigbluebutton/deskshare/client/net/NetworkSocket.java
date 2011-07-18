package org.bigbluebutton.deskshare.client.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkSocket {
    private static final int SEND_BUFFER_SIZE = 512000;
    private Socket socket;
    private final DataOutputStream outStream;

    public NetworkSocket(String host, int port) throws ConnectionException {
	System.out.println("[NetworkSocket] connect: " + host + ":" + port);

	try {
	    socket = new Socket(host, port);
	    socket.setTcpNoDelay(true);
	    socket.setPerformancePreferences(0, 1, 2);
	    socket.setSendBufferSize(SEND_BUFFER_SIZE);
	    socket.shutdownInput();

	    outStream = new DataOutputStream(socket.getOutputStream());
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	    throw new ConnectionException("UnknownHostException: " + host);
	} catch (IOException e) {
	    e.printStackTrace();
	    throw new ConnectionException("IOException: " + host + ":" + port);
	}
    }

    public void disconnect() throws IOException {
	synchronized (outStream) {
	    outStream.flush();
	    outStream.close();
	}

	socket.close();
    }

    public int write(byte[] data) throws IOException {
	synchronized (outStream) {
	    outStream.write(data);
	    outStream.flush();
	}

	return data.length;
    }

    public int write(ByteArrayOutputStream dataToSend) throws IOException {
	synchronized (outStream) {
	    dataToSend.writeTo(outStream);
	    outStream.flush();
	}

	return dataToSend.size();
    }
}
