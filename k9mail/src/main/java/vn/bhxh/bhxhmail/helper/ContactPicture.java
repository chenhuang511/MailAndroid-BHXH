package vn.bhxh.bhxhmail.helper;

import android.content.Context;
import android.util.TypedValue;

import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.activity.misc.ContactPictureLoader;

public class ContactPicture {

    public static ContactPictureLoader getContactPictureLoader(Context context) {
        final int defaultBgColor;
        if (!K9.isColorizeMissingContactPictures()) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(vn.bhxh.bhxhmail.R.attr.contactPictureFallbackDefaultBackgroundColor,
                    outValue, true);
            defaultBgColor = outValue.data;
        } else {
            defaultBgColor = 0;
        }

        return new ContactPictureLoader(context, defaultBgColor);
    }
}
