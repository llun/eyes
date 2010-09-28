package clients;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import play.utils.YesSSLSocketFactory;

public class YesSSLProtocolSocketFactory implements ProtocolSocketFactory {

  public Socket createSocket(String host, int port) throws IOException,
      UnknownHostException {
    return YesSSLSocketFactory.getDefault().createSocket(host, port);
  }

  public Socket createSocket(String host, int port, InetAddress localAddress,
      int localPort) throws IOException, UnknownHostException {
    return YesSSLSocketFactory.getDefault().createSocket(host, port,
        localAddress, localPort);
  }

  public Socket createSocket(String host, int port, InetAddress localAddress,
      int localPort, HttpConnectionParams params) throws IOException,
      UnknownHostException, ConnectTimeoutException {
    if (params == null) {
      throw new IllegalArgumentException("Parameters may not be null");
    } else {
      int timeout = params.getConnectionTimeout();
      SocketFactory socketfactory = YesSSLSocketFactory.getDefault();
      if (timeout == 0) {
        return socketfactory.createSocket(host, port, localAddress, localPort);
      } else {
        Socket socket = socketfactory.createSocket();
        SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
        SocketAddress remoteaddr = new InetSocketAddress(host, port);
        socket.bind(localaddr);
        socket.connect(remoteaddr, timeout);
        return socket;
      }
    }
  }

}
