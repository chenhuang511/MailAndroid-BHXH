
package vn.bhxh.bhxhmail.activity.setup;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.AccountExtras;
import vn.bhxh.bhxhmail.EmailAddressValidator;
import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.Preferences;
import vn.bhxh.bhxhmail.R;
import vn.bhxh.bhxhmail.Utils;
import vn.bhxh.bhxhmail.account.AccountCreator;
import vn.bhxh.bhxhmail.activity.Accounts;
import vn.bhxh.bhxhmail.activity.K9Activity;
import vn.bhxh.bhxhmail.helper.UrlEncodingHelper;
import vn.bhxh.bhxhmail.helper.Utility;
import vn.bhxh.bhxhmail.mail.AuthType;
import vn.bhxh.bhxhmail.mail.ConnectionSecurity;
import vn.bhxh.bhxhmail.mail.ServerSettings;
import vn.bhxh.bhxhmail.mail.Transport;
import vn.bhxh.bhxhmail.mail.store.RemoteStore;
import vn.bhxh.bhxhmail.view.ClientCertificateSpinner;
import vn.bhxh.bhxhmail.view.ClientCertificateSpinner.OnClientCertificateChangedListener;

/**
 * Prompts the user for the email address and password.
 * Attempts to lookup default settings for the domain the user specified. If the
 * domain is known the settings are handed off to the AccountSetupCheckSettings
 * activity. If no settings are found the settings are handed off to the
 * AccountSetupAccountType activity.
 */
public class AccountSetupBasics extends K9Activity
        implements OnClickListener, TextWatcher, OnCheckedChangeListener, OnClientCertificateChangedListener {
    private final static String EXTRA_ACCOUNT = "com.fsck.k9.AccountSetupBasics.account";
    private final static int DIALOG_NOTE = 1;
    private final static String STATE_KEY_PROVIDER =
            "com.fsck.k9.AccountSetupBasics.provider";
    private final static String STATE_KEY_CHECKED_INCOMING =
            "com.fsck.k9.AccountSetupBasics.checkedIncoming";

    private EditText mUserName;
    private EditText mEmailView;
    private EditText mPasswordView;
    private CheckBox mClientCertificateCheckBox;
    private ClientCertificateSpinner mClientCertificateSpinner;
    private ImageView mNextButton;
    private View mNextButtonOver;
    private Account mAccount;
    private Provider mProvider;
    private LinearLayout mLayoutManual;
//    private Button mManualSetupButton;

    private EmailAddressValidator mEmailValidator = new EmailAddressValidator();
    private boolean mCheckedIncoming = false;
    private CheckBox mShowPasswordCheckBox;
    private ImageView mBack;
    private TextView mTitle;
    private EditText mInputNameImap;
    private EditText mInputMailImap;
    private EditText mInputPassImap;
    private EditText mInputNameSmtp;
    private EditText mInputMailSmtp;
    private EditText mInputPassSmtp;
    private boolean mIsGmail;

    public static void actionNewAccount(Context context) {
        Intent i = new Intent(context, AccountSetupBasics.class);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(vn.bhxh.bhxhmail.R.layout.account_setup_basics);
        mEmailView = (EditText) findViewById(vn.bhxh.bhxhmail.R.id.account_email);
        mPasswordView = (EditText) findViewById(vn.bhxh.bhxhmail.R.id.account_password);
        mClientCertificateCheckBox = (CheckBox) findViewById(vn.bhxh.bhxhmail.R.id.account_client_certificate);
        mClientCertificateSpinner = (ClientCertificateSpinner) findViewById(vn.bhxh.bhxhmail.R.id.account_client_certificate_spinner);
        mNextButton = (ImageView) findViewById(vn.bhxh.bhxhmail.R.id.common_ic_send);
        mNextButtonOver = findViewById(vn.bhxh.bhxhmail.R.id.common_ic_send_over);
        mUserName = (EditText) findViewById(vn.bhxh.bhxhmail.R.id.username);
        mShowPasswordCheckBox = (CheckBox) findViewById(vn.bhxh.bhxhmail.R.id.show_password);
        mBack = (ImageView) findViewById(vn.bhxh.bhxhmail.R.id.common_ic_back);
        mTitle = (TextView) findViewById(R.id.common_title);
        mLayoutManual = (LinearLayout) findViewById(vn.bhxh.bhxhmail.R.id.layout_manual);
        mInputMailImap = (EditText) findViewById(vn.bhxh.bhxhmail.R.id.address_imap);
        mInputMailSmtp = (EditText) findViewById(vn.bhxh.bhxhmail.R.id.address_smtp);
        mInputNameImap = (EditText) findViewById(vn.bhxh.bhxhmail.R.id.option_imap);
        mInputNameSmtp = (EditText) findViewById(vn.bhxh.bhxhmail.R.id.option_smtp);
        mInputPassImap = (EditText) findViewById(vn.bhxh.bhxhmail.R.id.pass_imap);
        mInputPassSmtp = (EditText) findViewById(vn.bhxh.bhxhmail.R.id.pass_smtp);
        mNextButton.setOnClickListener(this);
        mNextButton.setVisibility(View.VISIBLE);
        mNextButtonOver.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.VISIBLE);
        mBack.setOnClickListener(this);
        mIsGmail = getIntent().getBooleanExtra("IS_GMAIL", false);
        mTitle.setText(getString(R.string.c_login));
        if (getIntent().getBooleanExtra("IS_MANUAL", false)) {
            mLayoutManual.setVisibility(View.GONE);
        } else
            mLayoutManual.setVisibility(View.GONE);
    }

    private void initializeViewListeners() {
        mUserName.addTextChangedListener(this);
        mEmailView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateFields();
                mInputNameImap.setText(s.toString());
                mInputNameSmtp.setText(s.toString());
                mUserName.setText(s.toString());
            }
        });
        mPasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateFields();
                mInputPassImap.setText(s.toString());
                mInputPassSmtp.setText(s.toString());
            }
        });
        mInputMailImap.addTextChangedListener(this);
        mInputMailSmtp.addTextChangedListener(this);
        mInputNameImap.addTextChangedListener(this);
        mInputNameSmtp.addTextChangedListener(this);
        mInputPassImap.addTextChangedListener(this);
        mInputPassSmtp.addTextChangedListener(this);
        mClientCertificateCheckBox.setOnCheckedChangeListener(this);
        mClientCertificateSpinner.setOnClientCertificateChangedListener(this);
        mShowPasswordCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showPassword(isChecked);
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAccount != null) {
            outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
        }
        if (mProvider != null) {
            outState.putSerializable(STATE_KEY_PROVIDER, mProvider);
        }
        outState.putBoolean(STATE_KEY_CHECKED_INCOMING, mCheckedIncoming);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
            String accountUuid = savedInstanceState.getString(EXTRA_ACCOUNT);
            mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        }

        if (savedInstanceState.containsKey(STATE_KEY_PROVIDER)) {
            mProvider = (Provider) savedInstanceState.getSerializable(STATE_KEY_PROVIDER);
        }

        mCheckedIncoming = savedInstanceState.getBoolean(STATE_KEY_CHECKED_INCOMING);

        updateViewVisibility(mClientCertificateCheckBox.isChecked());

        showPassword(mShowPasswordCheckBox.isChecked());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        /*
         * We wait until now to initialize the listeners because we didn't want
         * the OnCheckedChangeListener active while the
         * mClientCertificateCheckBox state was being restored because it could
         * trigger the pop-up of a ClientCertificateSpinner.chooseCertificate()
         * dialog.
         */
        initializeViewListeners();
        validateFields();
    }

    public void afterTextChanged(Editable s) {
        validateFields();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void onClientCertificateChanged(String alias) {
        validateFields();
    }

    /**
     * Called when checking the client certificate CheckBox
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        updateViewVisibility(isChecked);
        validateFields();

        // Have the user select (or confirm) the client certificate
        if (isChecked) {
            mClientCertificateSpinner.chooseCertificate();
        }
    }

    private void updateViewVisibility(boolean usingCertificates) {
        if (usingCertificates) {
            // hide password fields, show client certificate spinner
            mPasswordView.setVisibility(View.GONE);
            mShowPasswordCheckBox.setVisibility(View.GONE);
            mClientCertificateSpinner.setVisibility(View.VISIBLE);
        } else {
            // show password fields, hide client certificate spinner
            mPasswordView.setVisibility(View.VISIBLE);
            mShowPasswordCheckBox.setVisibility(View.VISIBLE);
            mClientCertificateSpinner.setVisibility(View.GONE);
        }
    }

    private void showPassword(boolean show) {
        int cursorPosition = mPasswordView.getSelectionStart();
        if (show) {
            mPasswordView.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        mPasswordView.setSelection(cursorPosition);
    }

    private void validateFields() {
        boolean clientCertificateChecked = mClientCertificateCheckBox.isChecked();
        String clientCertificateAlias = mClientCertificateSpinner.getAlias();
        String email = mEmailView.getText().toString();
        if (mIsGmail && !email.endsWith("@gmail.com")) {
            email = email + "@gmail.com";
        }
        String emailImap = mInputNameImap.getText().toString();
        String emailSmtp = mInputNameSmtp.getText().toString();

        boolean valid = Utility.requiredFieldValid(mEmailView)
                && ((!clientCertificateChecked && Utility.requiredFieldValid(mPasswordView))
                || (clientCertificateChecked && clientCertificateAlias != null))
                && mEmailValidator.isValidAddressOnly(email);
        boolean validManual = Utility.requiredFieldValid(mInputMailImap)
                && Utility.requiredFieldValid(mInputMailSmtp)
                && Utility.requiredFieldValid(mInputPassImap)
                && Utility.requiredFieldValid(mInputPassSmtp)
                && Utility.requiredFieldValid(mInputNameImap)
                && Utility.requiredFieldValid(mInputNameSmtp)
                && mEmailValidator.isValidAddressOnly(emailImap)
                && mEmailValidator.isValidAddressOnly(emailSmtp);
        if (mLayoutManual.getVisibility() != View.VISIBLE) {
            validManual = true;
        }

//        mNextButton.setEnabled(valid);
        if (valid && validManual && !Utils.isStringBlank(mUserName.getText().toString())) {
//            mManualSetupButton.setEnabled(true);
            mNextButtonOver.setVisibility(View.GONE);
        } else {
            mNextButtonOver.setVisibility(View.VISIBLE);
//            mManualSetupButton.setEnabled(false);
        }

        /*
         * Dim the next button's icon to 50% if the button is disabled.
         * TODO this can probably be done with a stateful drawable. Check into it.
         * android:state_enabled
         */
//        Utility.setCompoundDrawablesAlpha(mNextButton, mNextButton.isEnabled() ? 255 : 128);
    }

    private String getOwnerName() {
        String name = null;
        try {
            name = getDefaultAccountName();
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Could not get default account name", e);
        }

        if (name == null) {
            name = "";
        }
        return name;
    }

    private String getDefaultAccountName() {
        String name = null;
        Account account = Preferences.getPreferences(this).getDefaultAccount();
        if (account != null) {
            name = account.getName();
        }
        return name;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_NOTE) {
            if (mProvider != null && mProvider.note != null) {
                return new AlertDialog.Builder(this)
                        .setMessage(mProvider.note)
                        .setPositiveButton(
                                getString(vn.bhxh.bhxhmail.R.string.okay_action),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finishAutoSetup();
                                    }
                                })
                        .setNegativeButton(
                                getString(vn.bhxh.bhxhmail.R.string.cancel_action),
                                null)
                        .create();
            }
        }
        return null;
    }

    private void finishAutoSetup() {
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String[] emailParts = splitEmail(email);
        String user = emailParts[0];
        String domain = emailParts[1];
        try {
            String userEnc = UrlEncodingHelper.encodeUtf8(user);
            String passwordEnc = UrlEncodingHelper.encodeUtf8(password);

            String incomingUsername = mProvider.incomingUsernameTemplate;
            incomingUsername = incomingUsername.replaceAll("\\$email", email);
            incomingUsername = incomingUsername.replaceAll("\\$user", userEnc);
            incomingUsername = incomingUsername.replaceAll("\\$domain", domain);

            URI incomingUriTemplate = mProvider.incomingUriTemplate;
            URI incomingUri = new URI(incomingUriTemplate.getScheme(), incomingUsername + ":" + passwordEnc,
                    incomingUriTemplate.getHost(), incomingUriTemplate.getPort(), null, null, null);

            String outgoingUsername = mProvider.outgoingUsernameTemplate;

            URI outgoingUriTemplate = mProvider.outgoingUriTemplate;


            URI outgoingUri;
            if (outgoingUsername != null) {
                outgoingUsername = outgoingUsername.replaceAll("\\$email", email);
                outgoingUsername = outgoingUsername.replaceAll("\\$user", userEnc);
                outgoingUsername = outgoingUsername.replaceAll("\\$domain", domain);
                outgoingUri = new URI(outgoingUriTemplate.getScheme(), outgoingUsername + ":"
                        + passwordEnc, outgoingUriTemplate.getHost(), outgoingUriTemplate.getPort(), null,
                        null, null);

            } else {
                outgoingUri = new URI(outgoingUriTemplate.getScheme(),
                        null, outgoingUriTemplate.getHost(), outgoingUriTemplate.getPort(), null,
                        null, null);


            }
            if (mAccount == null) {
                mAccount = Preferences.getPreferences(this).newAccount();
            }
//            mAccount.setName(getOwnerName());
            mAccount.setName(mUserName.getText().toString());
            mAccount.setEmail(email);
            mAccount.setStoreUri(incomingUri.toString());
            mAccount.setTransportUri(outgoingUri.toString());

            setupFolderNames(incomingUriTemplate.getHost().toLowerCase(Locale.US));

            ServerSettings incomingSettings = RemoteStore.decodeStoreUri(incomingUri.toString());
            mAccount.setDeletePolicy(AccountCreator.getDefaultDeletePolicy(incomingSettings.type));

            // Check incoming here.  Then check outgoing in onActivityResult()
            AccountSetupCheckSettings.actionCheckSettings(this, mAccount, AccountSetupCheckSettings.CheckDirection.INCOMING);
        } catch (URISyntaxException use) {
            /*
             * If there is some problem with the URI we give up and go on to
             * manual setup.
             */
            onManualSetup();
        }
    }

    private void onNext() {
        if (mClientCertificateCheckBox.isChecked()) {

            // Auto-setup doesn't support client certificates.
            onManualSetup();
            return;
        }

        String email = mEmailView.getText().toString();
        if (mIsGmail && !email.endsWith("@gmail.com")) {
            email = email + "@gmail.com";
            mEmailView.setText(email);
        }
        String[] emailParts = splitEmail(email);
        String domain = emailParts[1];
        mProvider = findProviderForDomain(domain);
        if (mProvider == null) {
            /*
             * We don't have default settings for this account, start the manual
             * setup process.
             */
            onManualSetup();
            return;
        }

        if (mProvider.note != null) {
            showDialog(DIALOG_NOTE);
        } else {
            finishAutoSetup();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (!mCheckedIncoming) {
                //We've successfully checked incoming.  Now check outgoing.
                mCheckedIncoming = true;
                AccountSetupCheckSettings.actionCheckSettings(this, mAccount, AccountSetupCheckSettings.CheckDirection.OUTGOING);
            } else {
                //We've successfully checked outgoing as well.
                mAccount.setDescription(mAccount.getEmail());
                mAccount.setName(mUserName.getText().toString());
                mAccount.save(Preferences.getPreferences(this));
                K9.setServicesEnabled(this);
//                AccountSetupNames.actionSetNames(this, mAccount);
                Accounts.listAccounts(this);
                finish();
            }
        }
    }

    private void onManualSetup() {
        String email = mEmailView.getText().toString();
        String[] emailParts = splitEmail(email);
        String user = email;
        String domain = emailParts[1];

        String password = null;
        String clientCertificateAlias = null;
        AuthType authenticationType;
        if (mClientCertificateCheckBox.isChecked()) {
            authenticationType = AuthType.EXTERNAL;
            clientCertificateAlias = mClientCertificateSpinner.getAlias();
        } else {
            authenticationType = AuthType.PLAIN;
            password = mPasswordView.getText().toString();
        }

        if (mAccount == null) {
            mAccount = Preferences.getPreferences(this).newAccount();
        }
//        mAccount.setName(getOwnerName());
        mAccount.setName(mUserName.getText().toString());
        mAccount.setEmail(email);

        // set default uris
        // NOTE: they will be changed again in AccountSetupAccountType!
        ServerSettings storeServer = new ServerSettings(ServerSettings.Type.IMAP, "mail." + domain, -1,
                ConnectionSecurity.SSL_TLS_REQUIRED, authenticationType, user, password, clientCertificateAlias);
        ServerSettings transportServer = new ServerSettings(ServerSettings.Type.SMTP, "mail." + domain, -1,
                ConnectionSecurity.SSL_TLS_REQUIRED, authenticationType, user, password, clientCertificateAlias);
        String storeUri = RemoteStore.createStoreUri(storeServer);
        String transportUri = Transport.createTransportUri(transportServer);
        mAccount.setStoreUri(storeUri);
        mAccount.setTransportUri(transportUri);

        setupFolderNames(domain);

        AccountExtras accountExtras = new AccountExtras(mInputMailImap.getText().toString(),
                mInputMailSmtp.getText().toString(),
                mInputNameImap.getText().toString(),
                mInputNameSmtp.getText().toString(),
                mInputPassImap.getText().toString(),
                mInputPassSmtp.getText().toString());

        AccountSetupAccountType.actionSelectAccountType(this, mAccount, false, accountExtras);

        finish();
    }

    private void setupFolderNames(String domain) {
        mAccount.setDraftsFolderName(getString(vn.bhxh.bhxhmail.R.string.special_mailbox_name_drafts));
        mAccount.setTrashFolderName(getString(vn.bhxh.bhxhmail.R.string.special_mailbox_name_trash));
        mAccount.setSentFolderName(getString(vn.bhxh.bhxhmail.R.string.special_mailbox_name_sent));
        mAccount.setArchiveFolderName(getString(vn.bhxh.bhxhmail.R.string.special_mailbox_name_archive));

        // Yahoo! has a special folder for Spam, called "Bulk Mail".
        if (domain.endsWith(".yahoo.com")) {
            mAccount.setSpamFolderName("Bulk Mail");
        } else {
            mAccount.setSpamFolderName(getString(vn.bhxh.bhxhmail.R.string.special_mailbox_name_spam));
        }
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case vn.bhxh.bhxhmail.R.id.common_ic_send:
                if (mLayoutManual.getVisibility() == View.VISIBLE) {
                    onManualSetup();
                } else {
                    onNext();
                }
                break;
            case vn.bhxh.bhxhmail.R.id.common_ic_back:
                onBackPressed();
                break;
            case vn.bhxh.bhxhmail.R.id.manual_setup:
                onManualSetup();
                break;
        }
    }

    /**
     * Attempts to get the given attribute as a String resource first, and if it fails
     * returns the attribute as a simple String value.
     *
     * @param xml
     * @param name
     * @return
     */
    private String getXmlAttribute(XmlResourceParser xml, String name) {
        int resId = xml.getAttributeResourceValue(null, name, 0);
        if (resId == 0) {
            return xml.getAttributeValue(null, name);
        } else {
            return getString(resId);
        }
    }

    private Provider findProviderForDomain(String domain) {
        try {
            XmlResourceParser xml = getResources().getXml(vn.bhxh.bhxhmail.R.xml.providers);
            int xmlEventType;
            Provider provider = null;
            while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
                if (xmlEventType == XmlResourceParser.START_TAG
                        && "provider".equals(xml.getName())
                        && domain.equalsIgnoreCase(getXmlAttribute(xml, "domain"))) {
                    provider = new Provider();
                    provider.id = getXmlAttribute(xml, "id");
                    provider.label = getXmlAttribute(xml, "label");
                    provider.domain = getXmlAttribute(xml, "domain");
                    provider.note = getXmlAttribute(xml, "note");
                } else if (xmlEventType == XmlResourceParser.START_TAG
                        && "incoming".equals(xml.getName())
                        && provider != null) {
                    provider.incomingUriTemplate = new URI(getXmlAttribute(xml, "uri"));
                    provider.incomingUsernameTemplate = getXmlAttribute(xml, "username");
                } else if (xmlEventType == XmlResourceParser.START_TAG
                        && "outgoing".equals(xml.getName())
                        && provider != null) {
                    provider.outgoingUriTemplate = new URI(getXmlAttribute(xml, "uri"));
                    provider.outgoingUsernameTemplate = getXmlAttribute(xml, "username");
                } else if (xmlEventType == XmlResourceParser.END_TAG
                        && "provider".equals(xml.getName())
                        && provider != null) {
                    return provider;
                }
            }
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Error while trying to load provider settings.", e);
        }
        return null;
    }

    private String[] splitEmail(String email) {
        String[] retParts = new String[2];
        String[] emailParts = email.split("@");
        retParts[0] = (emailParts.length > 0) ? emailParts[0] : "";
        retParts[1] = (emailParts.length > 1) ? emailParts[1] : "";
        return retParts;
    }

    static class Provider implements Serializable {
        private static final long serialVersionUID = 8511656164616538989L;

        public String id;

        public String label;

        public String domain;

        public URI incomingUriTemplate;

        public String incomingUsernameTemplate;

        public URI outgoingUriTemplate;

        public String outgoingUsernameTemplate;

        public String note;
    }

}
