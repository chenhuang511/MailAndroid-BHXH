package vn.bhxh.bhxhmail.mailstore.migrations;


import android.content.Context;

import java.util.List;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.mail.Flag;
import vn.bhxh.bhxhmail.mailstore.LocalStore;
import vn.bhxh.bhxhmail.preferences.Storage;


/**
 * Helper to allow accessing classes and methods that aren't visible or accessible to the 'migrations' package
 */
public interface MigrationsHelper {
    LocalStore getLocalStore();
    Storage getStorage();
    Account getAccount();
    Context getContext();
    String serializeFlags(List<Flag> flags);
}
