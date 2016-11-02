package vn.bhxh.bhxhmail.mailstore;


import android.net.Uri;

import vn.bhxh.bhxhmail.mail.Part;
import vn.bhxh.bhxhmail.ui.messageview.AttachmentController;


public class AttachmentViewInfo {
    public static final long UNKNOWN_SIZE = -1;

    public final String mimeType;
    public final String displayName;
    public final long size;

    /**
     * A content provider URI that can be used to retrieve the decoded attachment.
     * <p/>
     * Note: All content providers must support an alternative MIME type appended as last URI segment.
     *
     * @see AttachmentController#getAttachmentUriForMimeType(AttachmentViewInfo, String)
     */
    public final Uri uri;
    public final boolean inlineAttachment;
    public final Part part;
    public final boolean isContentAvailable;

    public AttachmentViewInfo(String mimeType, String displayName, long size, Uri uri, boolean inlineAttachment,
            Part part, boolean isContentAvailable) {
        this.mimeType = mimeType;
        this.displayName = displayName;
        this.size = size;
        this.uri = uri;
        this.inlineAttachment = inlineAttachment;
        this.part = part;
        this.isContentAvailable = isContentAvailable;
    }
}
