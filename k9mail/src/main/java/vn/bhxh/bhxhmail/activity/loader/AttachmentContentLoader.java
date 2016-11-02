package vn.bhxh.bhxhmail.activity.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.cketti.safecontentresolver.SafeContentResolver;
import de.cketti.safecontentresolver.SafeContentResolverCompat;
import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.activity.misc.Attachment;
import vn.bhxh.bhxhmail.activity.misc.Attachment.LoadingState;

/**
 * Loader to fetch the content of an attachment.
 *
 * This will copy the data to a temporary file in our app's cache directory.
 */
public class AttachmentContentLoader extends AsyncTaskLoader<Attachment> {
    private static final String FILENAME_PREFIX = "attachment";


    private final Attachment sourceAttachment;
    private Attachment cachedResultAttachment;


    public AttachmentContentLoader(Context context, Attachment attachment) {
        super(context);
        if (attachment.state != LoadingState.METADATA) {
            throw new IllegalArgumentException("Attachment provided to content loader must be in METADATA state");
        }

        sourceAttachment = attachment;
    }

    @Override
    protected void onStartLoading() {
        if (cachedResultAttachment != null) {
            deliverResult(sourceAttachment);
        }

        if (takeContentChanged() || cachedResultAttachment == null) {
            forceLoad();
        }
    }

    @Override
    public Attachment loadInBackground() {
        Context context = getContext();

        try {
            File file = File.createTempFile(FILENAME_PREFIX, null, context.getCacheDir());
            file.deleteOnExit();

            if (K9.DEBUG) {
                Log.v(K9.LOG_TAG, "Saving attachment to " + file.getAbsolutePath());
            }

            SafeContentResolver safeContentResolver = SafeContentResolverCompat.newInstance(context);
            InputStream in = safeContentResolver.openInputStream(sourceAttachment.uri);
            try {
                FileOutputStream out = new FileOutputStream(file);
                try {
                    IOUtils.copy(in, out);
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }

            cachedResultAttachment = sourceAttachment.deriveWithLoadComplete(file.getAbsolutePath());
            return cachedResultAttachment;
        } catch (IOException e) {
            Log.e(K9.LOG_TAG, "Error saving attachment!", e);
        }

        cachedResultAttachment = sourceAttachment.deriveWithLoadCancelled();
        return cachedResultAttachment;
    }
}
