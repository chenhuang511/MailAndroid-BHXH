package vn.bhxh.bhxhmail.mail.store.imap;


import java.net.URI;
import java.net.URISyntaxException;

import vn.bhxh.bhxhmail.mail.AuthType;
import vn.bhxh.bhxhmail.mail.ConnectionSecurity;
import vn.bhxh.bhxhmail.mail.ServerSettings;
import vn.bhxh.bhxhmail.mail.ServerSettings.Type;

import static vn.bhxh.bhxhmail.mail.helper.UrlEncodingHelper.decodeUtf8;


class ImapStoreUriDecoder {
    /**
     * Decodes an ImapStore URI.
     *
     * <p>Possible forms:</p>
     * <pre>
     * imap://auth:user:password@server:port ConnectionSecurity.NONE
     * imap+tls+://auth:user:password@server:port ConnectionSecurity.STARTTLS_REQUIRED
     * imap+ssl+://auth:user:password@server:port ConnectionSecurity.SSL_TLS_REQUIRED
     * </pre>
     *
     * NOTE: this method expects the userinfo part of the uri to be encoded twice, due to a bug in
     * {@link ImapStoreUriCreator#create(ServerSettings)}.
     *
     * @param uri the store uri.
     */
    public static ImapStoreSettings decode(String uri) {
        String host;
        int port;
        ConnectionSecurity connectionSecurity;
        AuthType authenticationType = null;
        String username = null;
        String password = null;
        String clientCertificateAlias = null;
        String pathPrefix = null;
        boolean autoDetectNamespace = true;

        URI imapUri;
        try {
            imapUri = new URI(uri);
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException("Invalid ImapStore URI", use);
        }

        String scheme = imapUri.getScheme();
        /*
         * Currently available schemes are:
         * imap
         * imap+tls+
         * imap+ssl+
         *
         * The following are obsolete schemes that may be found in pre-existing
         * settings from earlier versions or that may be found when imported. We
         * continue to recognize them and re-map them appropriately:
         * imap+tls
         * imap+ssl
         */
        if (scheme.equals("imap")) {
            connectionSecurity = ConnectionSecurity.NONE;
            port = Type.IMAP.defaultPort;
        } else if (scheme.startsWith("imap+tls")) {
            connectionSecurity = ConnectionSecurity.STARTTLS_REQUIRED;
            port = Type.IMAP.defaultPort;
        } else if (scheme.startsWith("imap+ssl")) {
            connectionSecurity = ConnectionSecurity.SSL_TLS_REQUIRED;
            port = Type.IMAP.defaultTlsPort;
        } else {
            throw new IllegalArgumentException("Unsupported protocol (" + scheme + ")");
        }

        host = imapUri.getHost();

        if (imapUri.getPort() != -1) {
            port = imapUri.getPort();
        }

        if (imapUri.getUserInfo() != null) {
            String userinfo = imapUri.getUserInfo();
            String[] userInfoParts = userinfo.split(":");

            if (userinfo.endsWith(":")) {
                // Password is empty. This can only happen after an account was imported.
                authenticationType = AuthType.valueOf(userInfoParts[0]);
                username = decodeUtf8(userInfoParts[1]);
            } else if (userInfoParts.length == 2) {
                authenticationType = AuthType.PLAIN;
                username = decodeUtf8(userInfoParts[0]);
                password = decodeUtf8(userInfoParts[1]);
            } else if (userInfoParts.length == 3) {
                authenticationType = AuthType.valueOf(userInfoParts[0]);
                username = decodeUtf8(userInfoParts[1]);

                if (AuthType.EXTERNAL == authenticationType) {
                    clientCertificateAlias = decodeUtf8(userInfoParts[2]);
                } else {
                    password = decodeUtf8(userInfoParts[2]);
                }
            }
        }

        String path = imapUri.getPath();
        if (path != null && path.length() > 1) {
            // Strip off the leading "/"
            String cleanPath = path.substring(1);

            if (cleanPath.length() >= 2 && cleanPath.charAt(1) == '|') {
                autoDetectNamespace = cleanPath.charAt(0) == '1';
                if (!autoDetectNamespace) {
                    pathPrefix = cleanPath.substring(2);
                }
            } else {
                if (cleanPath.length() > 0) {
                    pathPrefix = cleanPath;
                    autoDetectNamespace = false;
                }
            }
        }

        return new ImapStoreSettings(host, port, connectionSecurity, authenticationType, username,
                password, clientCertificateAlias, autoDetectNamespace, pathPrefix);
    }
}
