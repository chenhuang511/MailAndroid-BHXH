package vn.bhxh.bhxhmail.mail.store.imap;


import java.io.IOException;
import java.util.Map;

import vn.bhxh.bhxhmail.mail.Message;
import vn.bhxh.bhxhmail.mail.MessagingException;
import vn.bhxh.bhxhmail.mail.filter.FixedLengthInputStream;


class FetchBodyCallback implements ImapResponseCallback {
    private Map<String, Message> mMessageMap;

    FetchBodyCallback(Map<String, Message> messageMap) {
        mMessageMap = messageMap;
    }

    @Override
    public Object foundLiteral(ImapResponse response,
                               FixedLengthInputStream literal) throws MessagingException, IOException {
        if (response.getTag() == null &&
                ImapResponseParser.equalsIgnoreCase(response.get(1), "FETCH")) {
            ImapList fetchList = (ImapList)response.getKeyedValue("FETCH");
            String uid = fetchList.getKeyedString("UID");

            ImapMessage message = (ImapMessage) mMessageMap.get(uid);
            message.parse(literal);

            // Return placeholder object
            return 1;
        }
        return null;
    }
}
