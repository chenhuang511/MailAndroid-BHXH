package vn.bhxh.bhxhmail.mail.store.imap;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import vn.bhxh.bhxhmail.mail.filter.PeekableInputStream;


public class ImapResponseHelper {
    public static ImapResponse createImapResponse(String response) throws IOException {
        String input = response + "\r\n";
        PeekableInputStream inputStream = new PeekableInputStream(new ByteArrayInputStream(input.getBytes()));
        ImapResponseParser parser = new ImapResponseParser(inputStream);

        return parser.readResponse();
    }
}
