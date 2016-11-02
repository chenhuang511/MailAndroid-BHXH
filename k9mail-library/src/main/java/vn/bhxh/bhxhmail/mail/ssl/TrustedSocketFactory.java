package vn.bhxh.bhxhmail.mail.ssl;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import vn.bhxh.bhxhmail.mail.MessagingException;

public interface TrustedSocketFactory {
    Socket createSocket(Socket socket, String host, int port, String clientCertificateAlias)
            throws NoSuchAlgorithmException, KeyManagementException, MessagingException, IOException;
}
