package vn.bhxh.bhxhmail.activity.setup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.Preferences;
import vn.bhxh.bhxhmail.activity.K9Activity;

public class AccountSetupComposition extends K9Activity {

    private static final String EXTRA_ACCOUNT = "account";

    private Account mAccount;

    private EditText mAccountSignature;
    private EditText mAccountEmail;
    private EditText mAccountAlwaysBcc;
    private EditText mAccountName;
    private CheckBox mAccountSignatureUse;
    private RadioButton mAccountSignatureBeforeLocation;
    private RadioButton mAccountSignatureAfterLocation;
    private LinearLayout mAccountSignatureLayout;

    public static void actionEditCompositionSettings(Activity context, Account account) {
        Intent i = new Intent(context, AccountSetupComposition.class);
        i.setAction(Intent.ACTION_EDIT);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        context.startActivity(i);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);

        setContentView(vn.bhxh.bhxhmail.R.layout.account_setup_composition);

        /*
         * If we're being reloaded we override the original account with the one
         * we saved
         */
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
            accountUuid = savedInstanceState.getString(EXTRA_ACCOUNT);
            mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        }

        mAccountName = (EditText)findViewById(vn.bhxh.bhxhmail.R.id.account_name);
        mAccountName.setText(mAccount.getName());

        mAccountEmail = (EditText)findViewById(vn.bhxh.bhxhmail.R.id.account_email);
        mAccountEmail.setText(mAccount.getEmail());

        mAccountAlwaysBcc = (EditText)findViewById(vn.bhxh.bhxhmail.R.id.account_always_bcc);
        mAccountAlwaysBcc.setText(mAccount.getAlwaysBcc());

        mAccountSignatureLayout = (LinearLayout)findViewById(vn.bhxh.bhxhmail.R.id.account_signature_layout);

        mAccountSignatureUse = (CheckBox)findViewById(vn.bhxh.bhxhmail.R.id.account_signature_use);
        boolean useSignature = mAccount.getSignatureUse();
        mAccountSignatureUse.setChecked(useSignature);
        mAccountSignatureUse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mAccountSignatureLayout.setVisibility(View.VISIBLE);
                    mAccountSignature.setText(mAccount.getSignature());
                    boolean isSignatureBeforeQuotedText = mAccount.isSignatureBeforeQuotedText();
                    mAccountSignatureBeforeLocation.setChecked(isSignatureBeforeQuotedText);
                    mAccountSignatureAfterLocation.setChecked(!isSignatureBeforeQuotedText);
                } else {
                    mAccountSignatureLayout.setVisibility(View.GONE);
                }
            }
        });

        mAccountSignature = (EditText)findViewById(vn.bhxh.bhxhmail.R.id.account_signature);

        mAccountSignatureBeforeLocation = (RadioButton)findViewById(vn.bhxh.bhxhmail.R.id.account_signature_location_before_quoted_text);
        mAccountSignatureAfterLocation = (RadioButton)findViewById(vn.bhxh.bhxhmail.R.id.account_signature_location_after_quoted_text);

        if (useSignature) {
            mAccountSignature.setText(mAccount.getSignature());

            boolean isSignatureBeforeQuotedText = mAccount.isSignatureBeforeQuotedText();
            mAccountSignatureBeforeLocation.setChecked(isSignatureBeforeQuotedText);
            mAccountSignatureAfterLocation.setChecked(!isSignatureBeforeQuotedText);
        } else {
            mAccountSignatureLayout.setVisibility(View.GONE);
        }
    }

    private void saveSettings() {
        mAccount.setEmail(mAccountEmail.getText().toString());
        mAccount.setAlwaysBcc(mAccountAlwaysBcc.getText().toString());
        mAccount.setName(mAccountName.getText().toString());
        mAccount.setSignatureUse(mAccountSignatureUse.isChecked());
        if (mAccountSignatureUse.isChecked()) {
            mAccount.setSignature(mAccountSignature.getText().toString());
            boolean isSignatureBeforeQuotedText = mAccountSignatureBeforeLocation.isChecked();
            mAccount.setSignatureBeforeQuotedText(isSignatureBeforeQuotedText);
        }

        mAccount.save(Preferences.getPreferences(this));
    }

    @Override
    public void onBackPressed() {
        saveSettings();
        super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_ACCOUNT, mAccount.getUuid());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mAccount.save(Preferences.getPreferences(this));
        finish();
    }
}
