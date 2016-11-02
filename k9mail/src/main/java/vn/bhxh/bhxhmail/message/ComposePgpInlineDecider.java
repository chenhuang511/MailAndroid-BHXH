package vn.bhxh.bhxhmail.message;


import java.util.List;

import vn.bhxh.bhxhmail.crypto.MessageDecryptVerifier;
import vn.bhxh.bhxhmail.mail.Message;
import vn.bhxh.bhxhmail.mail.Part;


public class ComposePgpInlineDecider {
    public boolean shouldReplyInline(Message localMessage) {
        // TODO more criteria for this? maybe check the User-Agent header?
        return messageHasPgpInlineParts(localMessage);
    }

    private boolean messageHasPgpInlineParts(Message localMessage) {
        List<Part> inlineParts = MessageDecryptVerifier.findPgpInlineParts(localMessage);
        return !inlineParts.isEmpty();
    }
}
