package vn.bhxh.bhxhmail;


import android.content.Context;


public class Globals {
    private static Context context;

    static void setContext(Context context) {
        Globals.context = context;
    }

    public static Context getContext() {
        if (context == null) {
            throw new IllegalStateException("No context provided");
        }

        return context;
    }
}
