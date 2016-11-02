package vn.bhxh.bhxhmail.mail.store.imap;


import java.io.IOException;

import vn.bhxh.bhxhmail.mail.Part;
import vn.bhxh.bhxhmail.mail.filter.FixedLengthInputStream;
import vn.bhxh.bhxhmail.mail.internet.MimeHeader;
import vn.bhxh.bhxhmail.mail.internet.MimeUtility;


class FetchPartCallback implements ImapResponseCallback {
    private Part mPart;

    FetchPartCallback(Part part) {
        mPart = part;
    }

    @Override
    public Object foundLiteral(ImapResponse response, FixedLengthInputStream literal) throws IOException {
        if (response.getTag() == null &&
                ImapResponseParser.equalsIgnoreCase(response.get(1), "FETCH")) {
            //TODO: check for correct UID

            String contentTransferEncoding = mPart
                    .getHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING)[0];
            String contentType = mPart
                    .getHeader(MimeHeader.HEADER_CONTENT_TYPE)[0];

            return MimeUtility.createBody(literal, contentTransferEncoding, contentType);
        }
        return null;
    }
}
