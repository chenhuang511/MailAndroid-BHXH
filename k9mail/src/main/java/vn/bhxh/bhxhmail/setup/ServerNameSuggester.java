package vn.bhxh.bhxhmail.setup;


import vn.bhxh.bhxhmail.mail.ServerSettings;


public class ServerNameSuggester {
    public String suggestServerName(ServerSettings.Type serverType, String domainPart) {
        switch (serverType) {
            case IMAP: {
                return "imap." + domainPart;
            }
            case SMTP: {
                return "smtp." + domainPart;
            }
            case WebDAV: {
                return "exchange." + domainPart;
            }
            case POP3: {
                return "pop3." + domainPart;
            }
        }

        throw new AssertionError("Missed case: " + serverType);
    }
}
