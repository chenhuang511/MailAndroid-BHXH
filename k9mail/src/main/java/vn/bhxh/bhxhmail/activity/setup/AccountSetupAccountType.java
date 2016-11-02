package vn.bhxh.bhxhmail.activity.setup;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.AccountExtras;
import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.Preferences;
import vn.bhxh.bhxhmail.activity.K9Activity;
import vn.bhxh.bhxhmail.helper.EmailHelper;
import vn.bhxh.bhxhmail.mail.ServerSettings.Type;
import vn.bhxh.bhxhmail.setup.ServerNameSuggester;

import static vn.bhxh.bhxhmail.mail.ServerSettings.Type.IMAP;
import static vn.bhxh.bhxhmail.mail.ServerSettings.Type.SMTP;
import static vn.bhxh.bhxhmail.mail.ServerSettings.Type.WebDAV;


/**
 * Prompts the user to select an account type. The account type, along with the
 * passed in email address, password and makeDefault are then passed on to the
 * AccountSetupIncoming activity.
 */
public class AccountSetupAccountType extends K9Activity implements OnClickListener {
    private static final String EXTRA_ACCOUNT = "account";
    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";

    private final ServerNameSuggester serverNameSuggester = new ServerNameSuggester();
    private Account mAccount;
    private AccountExtras mAccountExtras;
    private boolean mMakeDefault;

    public static void actionSelectAccountType(Context context, Account account, boolean makeDefault, AccountExtras accountExtras) {
        Intent i = new Intent(context, AccountSetupAccountType.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        i.putExtra(AccountExtras.KEY_EXTRAS, accountExtras);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(vn.bhxh.bhxhmail.R.layout.account_setup_account_type);
        findViewById(vn.bhxh.bhxhmail.R.id.pop).setOnClickListener(this);
        findViewById(vn.bhxh.bhxhmail.R.id.imap).setOnClickListener(this);
        findViewById(vn.bhxh.bhxhmail.R.id.webdav).setOnClickListener(this);

        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        mMakeDefault = getIntent().getBooleanExtra(EXTRA_MAKE_DEFAULT, false);
        mAccountExtras = getIntent().getParcelableExtra(AccountExtras.KEY_EXTRAS);

        try {
            setupStoreAndSmtpTransport(IMAP, "imap+ssl+");
        } catch (Exception ex) {
            failure(ex);
        }

        AccountSetupIncoming.actionIncomingSettings(this, mAccount, mMakeDefault, mAccountExtras);
        finish();
    }

    private void setupStoreAndSmtpTransport(Type serverType, String schemePrefix) throws URISyntaxException {
        String domainPart = EmailHelper.getDomainFromEmailAddress(mAccount.getEmail());

        String suggestedStoreServerName = serverNameSuggester.suggestServerName(serverType, domainPart);
        URI storeUriForDecode = new URI(mAccount.getStoreUri());
        URI storeUri = new URI(schemePrefix, storeUriForDecode.getUserInfo(), suggestedStoreServerName,
                storeUriForDecode.getPort(), null, null, null);
        mAccount.setStoreUri(storeUri.toString());

        String suggestedTransportServerName = serverNameSuggester.suggestServerName(SMTP, domainPart);
        URI transportUriForDecode = new URI(mAccount.getTransportUri());
        URI transportUri = new URI("smtp+tls+", transportUriForDecode.getUserInfo(), suggestedTransportServerName,
                transportUriForDecode.getPort(), null, null, null);
        mAccount.setTransportUri(transportUri.toString());
    }

    private void setupDav() throws URISyntaxException {
        URI uriForDecode = new URI(mAccount.getStoreUri());

        /*
         * The user info we have been given from
         * AccountSetupBasics.onManualSetup() is encoded as an IMAP store
         * URI: AuthType:UserName:Password (no fields should be empty).
         * However, AuthType is not applicable to WebDAV nor to its store
         * URI. Re-encode without it, using just the UserName and Password.
         */
        String userPass = "";
        String[] userInfo = uriForDecode.getUserInfo().split(":");
        if (userInfo.length > 1) {
            userPass = userInfo[1];
        }
        if (userInfo.length > 2) {
            userPass = userPass + ":" + userInfo[2];
        }

        String domainPart = EmailHelper.getDomainFromEmailAddress(mAccount.getEmail());
        String suggestedServerName = serverNameSuggester.suggestServerName(WebDAV, domainPart);
        URI uri = new URI("webdav+ssl+", userPass, suggestedServerName, uriForDecode.getPort(), null, null, null);
        mAccount.setStoreUri(uri.toString());
    }

    public void onClick(View v) {
//        try {
//            switch (v.getId()) {
//                case R.id.pop: {
//                    setupStoreAndSmtpTransport(POP3, "pop3+ssl+");
//                    break;
//                }
//                case R.id.imap: {
//                    setupStoreAndSmtpTransport(IMAP, "imap+ssl+");
//                    break;
//                }
//                case R.id.webdav: {
//                    setupDav();
//                    break;
//                }
//            }
//        } catch (Exception ex) {
//            failure(ex);
//        }
//
//        AccountSetupIncoming.actionIncomingSettings(this, mAccount, mMakeDefault);
//        finish();
    }

    private void failure(Exception use) {
        Log.e(K9.LOG_TAG, "Failure", use);
        String toastText = getString(vn.bhxh.bhxhmail.R.string.account_setup_bad_uri, use.getMessage());

        Toast toast = Toast.makeText(getApplication(), toastText, Toast.LENGTH_LONG);
        toast.show();
    }
}
