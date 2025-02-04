
package vn.bhxh.bhxhmail.mail.transport;

import android.util.Log;

import java.util.Collections;

import vn.bhxh.bhxhmail.mail.K9MailLib;
import vn.bhxh.bhxhmail.mail.Message;
import vn.bhxh.bhxhmail.mail.MessagingException;
import vn.bhxh.bhxhmail.mail.ServerSettings;
import vn.bhxh.bhxhmail.mail.Transport;
import vn.bhxh.bhxhmail.mail.store.StoreConfig;
import vn.bhxh.bhxhmail.mail.store.webdav.WebDavHttpClient;
import vn.bhxh.bhxhmail.mail.store.webdav.WebDavStore;

import static vn.bhxh.bhxhmail.mail.K9MailLib.LOG_TAG;

public class WebDavTransport extends Transport {

    /**
     * Decodes a WebDavTransport URI.
     *
     * <p>
     * <b>Note:</b> Everything related to sending messages via WebDAV is handled by
     * {@link WebDavStore}. So the transport URI is the same as the store URI.
     * </p>
     */
    public static ServerSettings decodeUri(String uri) {
        return WebDavStore.decodeUri(uri);
    }

    /**
     * Creates a WebDavTransport URI.
     *
     * <p>
     * <b>Note:</b> Everything related to sending messages via WebDAV is handled by
     * {@link WebDavStore}. So the transport URI is the same as the store URI.
     * </p>
     */
    public static String createUri(ServerSettings server) {
        return WebDavStore.createUri(server);
    }


    private WebDavStore store;

    public WebDavTransport(StoreConfig storeConfig) throws MessagingException {
        store = new WebDavStore(storeConfig, new WebDavHttpClient.WebDavHttpClientFactory());

        if (K9MailLib.isDebug())
            Log.d(LOG_TAG, ">>> New WebDavTransport creation complete");
    }

    @Override
    public void open() throws MessagingException {
        if (K9MailLib.isDebug())
            Log.d(LOG_TAG, ">>> open called on WebDavTransport ");

        store.getHttpClient();
    }

    @Override
    public void close() {
    }

    @Override
    public void sendMessage(Message message) throws MessagingException {
        store.sendMessages(Collections.singletonList(message));
    }
}
