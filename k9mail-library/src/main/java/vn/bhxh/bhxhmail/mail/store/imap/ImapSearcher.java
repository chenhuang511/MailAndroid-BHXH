package vn.bhxh.bhxhmail.mail.store.imap;


import java.io.IOException;
import java.util.List;

import vn.bhxh.bhxhmail.mail.MessagingException;


interface ImapSearcher {
    List<ImapResponse> search() throws IOException, MessagingException;
}
