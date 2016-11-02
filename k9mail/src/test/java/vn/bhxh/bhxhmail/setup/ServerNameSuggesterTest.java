package vn.bhxh.bhxhmail.setup;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import vn.bhxh.bhxhmail.mail.ServerSettings;

import static org.junit.Assert.assertEquals;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class ServerNameSuggesterTest {
    private ServerNameSuggester serverNameSuggester;


    @Before
    public void setUp() throws Exception {
        serverNameSuggester = new ServerNameSuggester();
    }

    @Test
    public void suggestServerName_forImapServer() throws Exception {
        ServerSettings.Type serverType = ServerSettings.Type.IMAP;
        String domainPart = "example.org";

        String result = serverNameSuggester.suggestServerName(serverType, domainPart);

        assertEquals("imap.example.org", result);
    }

    @Test
    public void suggestServerName_forPop3Server() throws Exception {
        ServerSettings.Type serverType = ServerSettings.Type.POP3;
        String domainPart = "example.org";

        String result = serverNameSuggester.suggestServerName(serverType, domainPart);

        assertEquals("pop3.example.org", result);
    }

    @Test
    public void suggestServerName_forWebDavServer() throws Exception {
        ServerSettings.Type serverType = ServerSettings.Type.WebDAV;
        String domainPart = "example.org";

        String result = serverNameSuggester.suggestServerName(serverType, domainPart);

        assertEquals("exchange.example.org", result);
    }

    @Test
    public void suggestServerName_forSmtpServer() throws Exception {
        ServerSettings.Type serverType = ServerSettings.Type.SMTP;
        String domainPart = "example.org";

        String result = serverNameSuggester.suggestServerName(serverType, domainPart);

        assertEquals("smtp.example.org", result);
    }
}
