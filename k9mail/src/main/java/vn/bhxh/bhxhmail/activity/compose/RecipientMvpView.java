package vn.bhxh.bhxhmail.activity.compose;


import android.app.LoaderManager;
import android.app.PendingIntent;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Toast;
import android.widget.ViewAnimator;

import java.util.Arrays;
import java.util.List;

import vn.bhxh.bhxhmail.FontSizes;
import vn.bhxh.bhxhmail.R;
import vn.bhxh.bhxhmail.activity.MessageCompose;
import vn.bhxh.bhxhmail.mail.Address;
import vn.bhxh.bhxhmail.mail.Message;
import vn.bhxh.bhxhmail.view.RecipientSelectView;
import vn.bhxh.bhxhmail.view.RecipientSelectView.Recipient;


public class RecipientMvpView implements OnFocusChangeListener, OnClickListener {
    private static final int VIEW_INDEX_HIDDEN = -1;
    private static final int VIEW_INDEX_CRYPTO_STATUS_DISABLED = 0;
    private static final int VIEW_INDEX_CRYPTO_STATUS_ERROR = 1;
    private static final int VIEW_INDEX_CRYPTO_STATUS_NO_RECIPIENTS = 2;
    private static final int VIEW_INDEX_CRYPTO_STATUS_ERROR_NO_KEY = 3;
    private static final int VIEW_INDEX_CRYPTO_STATUS_DISABLED_NO_KEY = 4;
    private static final int VIEW_INDEX_CRYPTO_STATUS_UNTRUSTED = 5;
    private static final int VIEW_INDEX_CRYPTO_STATUS_TRUSTED = 6;
    private static final int VIEW_INDEX_CRYPTO_STATUS_SIGN_ONLY = 7;

    private static final int VIEW_INDEX_BCC_EXPANDER_VISIBLE = 0;
    private static final int VIEW_INDEX_BCC_EXPANDER_HIDDEN = 1;

    private final MessageCompose activity;
    private final View ccWrapper;
    private final View ccDivider;
    private final View bccWrapper;
    private final View bccDivider;
    private final RecipientSelectView toView;
    private final RecipientSelectView ccView;
    private final RecipientSelectView bccView;
    private final ViewAnimator cryptoStatusView;
    private final ViewAnimator recipientExpanderContainer;
    private final View pgpInlineIndicator;
    private RecipientPresenter presenter;


    public RecipientMvpView(MessageCompose activity) {
        this.activity = activity;

        toView = (RecipientSelectView) activity.findViewById(vn.bhxh.bhxhmail.R.id.to);
        ccView = (RecipientSelectView) activity.findViewById(vn.bhxh.bhxhmail.R.id.cc);
        bccView = (RecipientSelectView) activity.findViewById(vn.bhxh.bhxhmail.R.id.bcc);
        ccWrapper = activity.findViewById(vn.bhxh.bhxhmail.R.id.cc_wrapper);
        ccDivider = activity.findViewById(vn.bhxh.bhxhmail.R.id.cc_divider);
        bccWrapper = activity.findViewById(vn.bhxh.bhxhmail.R.id.bcc_wrapper);
        bccDivider = activity.findViewById(vn.bhxh.bhxhmail.R.id.bcc_divider);
        recipientExpanderContainer = (ViewAnimator) activity.findViewById(vn.bhxh.bhxhmail.R.id.recipient_expander_container);
        cryptoStatusView = (ViewAnimator) activity.findViewById(vn.bhxh.bhxhmail.R.id.crypto_status);
        cryptoStatusView.setOnClickListener(this);
        pgpInlineIndicator = activity.findViewById(vn.bhxh.bhxhmail.R.id.pgp_inline_indicator);

        toView.setOnFocusChangeListener(this);
        ccView.setOnFocusChangeListener(this);
        bccView.setOnFocusChangeListener(this);

        View recipientExpander = activity.findViewById(vn.bhxh.bhxhmail.R.id.recipient_expander);
        recipientExpander.setOnClickListener(this);

        View toLabel = activity.findViewById(vn.bhxh.bhxhmail.R.id.to_label);
        View ccLabel = activity.findViewById(vn.bhxh.bhxhmail.R.id.cc_label);
        View bccLabel = activity.findViewById(vn.bhxh.bhxhmail.R.id.bcc_label);
        toLabel.setOnClickListener(this);
        ccLabel.setOnClickListener(this);
        bccLabel.setOnClickListener(this);

        pgpInlineIndicator.setOnClickListener(this);
    }

    public void setPresenter(final RecipientPresenter presenter) {
        this.presenter = presenter;

        if (presenter == null) {
            toView.setTokenListener(null);
            ccView.setTokenListener(null);
            bccView.setTokenListener(null);
            return;
        }

        toView.setTokenListener(new RecipientSelectView.TokenListener<Recipient>() {
            @Override
            public void onTokenAdded(RecipientSelectView.Recipient recipient) {
                presenter.onToTokenAdded(recipient);
            }

            @Override
            public void onTokenRemoved(RecipientSelectView.Recipient recipient) {
                presenter.onToTokenRemoved(recipient);
            }

            @Override
            public void onTokenChanged(RecipientSelectView.Recipient recipient) {
                presenter.onToTokenChanged(recipient);
            }
        });

        ccView.setTokenListener(new RecipientSelectView.TokenListener<Recipient>() {
            @Override
            public void onTokenAdded(RecipientSelectView.Recipient recipient) {
                presenter.onCcTokenAdded(recipient);
            }

            @Override
            public void onTokenRemoved(RecipientSelectView.Recipient recipient) {
                presenter.onCcTokenRemoved(recipient);
            }

            @Override
            public void onTokenChanged(RecipientSelectView.Recipient recipient) {
                presenter.onCcTokenChanged(recipient);
            }
        });

        bccView.setTokenListener(new RecipientSelectView.TokenListener<Recipient>() {
            @Override
            public void onTokenAdded(RecipientSelectView.Recipient recipient) {
                presenter.onBccTokenAdded(recipient);
            }

            @Override
            public void onTokenRemoved(RecipientSelectView.Recipient recipient) {
                presenter.onBccTokenRemoved(recipient);
            }

            @Override
            public void onTokenChanged(RecipientSelectView.Recipient recipient) {
                presenter.onBccTokenChanged(recipient);
            }
        });
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        toView.addTextChangedListener(textWatcher);
        ccView.addTextChangedListener(textWatcher);
        bccView.addTextChangedListener(textWatcher);
    }

    public void setCryptoProvider(String openPgpProvider) {
        toView.setCryptoProvider(openPgpProvider);
        ccView.setCryptoProvider(openPgpProvider);
        bccView.setCryptoProvider(openPgpProvider);
    }

    public void requestFocusOnToField() {
        toView.requestFocus();
    }

    public void requestFocusOnCcField() {
        ccView.requestFocus();
    }

    public void requestFocusOnBccField() {
        bccView.requestFocus();
    }

    public void setFontSizes(FontSizes fontSizes, int fontSize) {
        fontSizes.setViewTextSize(toView, fontSize);
        fontSizes.setViewTextSize(ccView, fontSize);
        fontSizes.setViewTextSize(bccView, fontSize);
    }

    public void addRecipients(Message.RecipientType recipientType, RecipientSelectView.Recipient... recipients) {
        switch (recipientType) {
            case TO: {
                toView.addRecipients(recipients);
                break;
            }
            case CC: {
                ccView.addRecipients(recipients);
                break;
            }
            case BCC: {
                bccView.addRecipients(recipients);
                break;
            }
        }
    }

    public void setCcVisibility(boolean visible) {
        ccWrapper.setVisibility(visible ? View.VISIBLE : View.GONE);
        ccDivider.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setBccVisibility(boolean visible) {
        bccWrapper.setVisibility(visible ? View.VISIBLE : View.GONE);
        bccDivider.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setRecipientExpanderVisibility(boolean visible) {
        int childToDisplay = visible ? VIEW_INDEX_BCC_EXPANDER_VISIBLE : VIEW_INDEX_BCC_EXPANDER_HIDDEN;
        if (recipientExpanderContainer.getDisplayedChild() != childToDisplay) {
            recipientExpanderContainer.setDisplayedChild(childToDisplay);
        }
    }

    public boolean isCcVisible() {
        return ccWrapper.getVisibility() == View.VISIBLE;
    }

    public boolean isBccVisible() {
        return bccWrapper.getVisibility() == View.VISIBLE;
    }

    public void showNoRecipientsError() {
        toView.setError(toView.getContext().getString(vn.bhxh.bhxhmail.R.string.message_compose_error_no_recipients));
    }

    public List<Address> getToAddresses() {
        return Arrays.asList(toView.getAddresses());
    }

    public List<Address> getCcAddresses() {
        return Arrays.asList(ccView.getAddresses());
    }

    public List<Address> getBccAddresses() {
        return Arrays.asList(bccView.getAddresses());
    }

    public List<RecipientSelectView.Recipient> getToRecipients() {
        return toView.getObjects();
    }

    public List<RecipientSelectView.Recipient> getCcRecipients() {
        return ccView.getObjects();
    }

    public List<RecipientSelectView.Recipient> getBccRecipients() {
        return bccView.getObjects();
    }

    public boolean recipientToHasUncompletedText() {
        return toView.hasUncompletedText();
    }

    public boolean recipientCcHasUncompletedText() {
        return ccView.hasUncompletedText();
    }

    public boolean recipientBccHasUncompletedText() {
        return bccView.hasUncompletedText();
    }

    public boolean recipientToTryPerformCompletion() {
        return toView.tryPerformCompletion();
    }

    public boolean recipientCcTryPerformCompletion() {
        return ccView.tryPerformCompletion();
    }

    public boolean recipientBccTryPerformCompletion() {
        return bccView.tryPerformCompletion();
    }

    public void showToUncompletedError() {
        toView.setError(toView.getContext().getString(vn.bhxh.bhxhmail.R.string.compose_error_incomplete_recipient));
    }

    public void showCcUncompletedError() {
        ccView.setError(ccView.getContext().getString(vn.bhxh.bhxhmail.R.string.compose_error_incomplete_recipient));
    }

    public void showBccUncompletedError() {
        bccView.setError(bccView.getContext().getString(vn.bhxh.bhxhmail.R.string.compose_error_incomplete_recipient));
    }

    public void showPgpInlineModeIndicator(boolean pgpInlineModeEnabled) {
        pgpInlineIndicator.setVisibility(pgpInlineModeEnabled ? View.VISIBLE : View.GONE);
        activity.invalidateOptionsMenu();
    }

    public void showCryptoStatus(final CryptoStatusDisplayType cryptoStatusDisplayType) {
        boolean shouldBeHidden = cryptoStatusDisplayType.childToDisplay == VIEW_INDEX_HIDDEN;
        if (shouldBeHidden) {
            cryptoStatusView.setVisibility(View.GONE);
            return;
        }

        cryptoStatusView.setVisibility(View.VISIBLE);
        int childToDisplay = cryptoStatusDisplayType.childToDisplay;
        cryptoStatusView.setDisplayedChild(childToDisplay);
    }

    public void showContactPicker(int requestCode) {
        activity.showContactPicker(requestCode);
    }

    public void showErrorContactNoAddress() {
        Toast.makeText(activity, vn.bhxh.bhxhmail.R.string.error_contact_address_not_found, Toast.LENGTH_LONG).show();
    }

    public void showErrorOpenPgpConnection() {
        Toast.makeText(activity, vn.bhxh.bhxhmail.R.string.error_crypto_provider_connect, Toast.LENGTH_LONG).show();
    }

    public void showErrorOpenPgpUserInteractionRequired() {
        Toast.makeText(activity, vn.bhxh.bhxhmail.R.string.error_crypto_provider_ui_required, Toast.LENGTH_LONG).show();
    }

    public void showErrorMissingSignKey() {
        Toast.makeText(activity, vn.bhxh.bhxhmail.R.string.compose_error_no_signing_key, Toast.LENGTH_LONG).show();
    }

    public void showErrorPrivateButMissingKeys() {
        Toast.makeText(activity, vn.bhxh.bhxhmail.R.string.compose_error_private_missing_keys, Toast.LENGTH_LONG).show();
    }

    public void showErrorAttachInline() {
        Toast.makeText(activity, vn.bhxh.bhxhmail.R.string.error_crypto_inline_attach, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (!hasFocus) {
            return;
        }

        switch (view.getId()) {
            case vn.bhxh.bhxhmail.R.id.to: {
                presenter.onToFocused();
                break;
            }
            case vn.bhxh.bhxhmail.R.id.cc: {
                presenter.onCcFocused();
                break;
            }
            case vn.bhxh.bhxhmail.R.id.bcc: {
                presenter.onBccFocused();
                break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case vn.bhxh.bhxhmail.R.id.to_label: {
                presenter.onClickToLabel();
                break;
            }
            case vn.bhxh.bhxhmail.R.id.cc_label: {
                presenter.onClickCcLabel();
                break;
            }
            case vn.bhxh.bhxhmail.R.id.bcc_label: {
                presenter.onClickBccLabel();
                break;
            }
            case vn.bhxh.bhxhmail.R.id.recipient_expander: {
                presenter.onClickRecipientExpander();
                break;
            }
            case vn.bhxh.bhxhmail.R.id.crypto_status: {
                presenter.onClickCryptoStatus();
                break;
            }
            case vn.bhxh.bhxhmail.R.id.pgp_inline_indicator: {
                presenter.onClickPgpInlineIndicator();
            }
        }
    }

    public void showCryptoDialog(RecipientPresenter.CryptoMode currentCryptoMode) {
        CryptoSettingsDialog dialog = CryptoSettingsDialog.newInstance(currentCryptoMode);
        dialog.show(activity.getFragmentManager(), "crypto_settings");
    }

    public void showOpenPgpInlineDialog(boolean firstTime) {
        PgpInlineDialog dialog = PgpInlineDialog.newInstance(firstTime, vn.bhxh.bhxhmail.R.id.pgp_inline_indicator);
        dialog.show(activity.getFragmentManager(), "openpgp_inline");
    }

    public void launchUserInteractionPendingIntent(PendingIntent pendingIntent, int requestCode) {
        activity.launchUserInteractionPendingIntent(pendingIntent, requestCode);
    }

    public void setLoaderManager(LoaderManager loaderManager) {
        toView.setLoaderManager(loaderManager);
        ccView.setLoaderManager(loaderManager);
        bccView.setLoaderManager(loaderManager);
    }

    public enum CryptoStatusDisplayType {
        UNCONFIGURED(VIEW_INDEX_HIDDEN),
        UNINITIALIZED(VIEW_INDEX_HIDDEN),
        DISABLED(VIEW_INDEX_CRYPTO_STATUS_DISABLED),
        SIGN_ONLY(VIEW_INDEX_CRYPTO_STATUS_SIGN_ONLY),
        OPPORTUNISTIC_EMPTY(VIEW_INDEX_CRYPTO_STATUS_NO_RECIPIENTS),
        OPPORTUNISTIC_NOKEY(VIEW_INDEX_CRYPTO_STATUS_DISABLED_NO_KEY),
        OPPORTUNISTIC_UNTRUSTED(VIEW_INDEX_CRYPTO_STATUS_UNTRUSTED),
        OPPORTUNISTIC_TRUSTED(VIEW_INDEX_CRYPTO_STATUS_TRUSTED),
        PRIVATE_EMPTY(VIEW_INDEX_CRYPTO_STATUS_NO_RECIPIENTS),
        PRIVATE_NOKEY(VIEW_INDEX_CRYPTO_STATUS_ERROR_NO_KEY),
        PRIVATE_UNTRUSTED(VIEW_INDEX_CRYPTO_STATUS_UNTRUSTED),
        PRIVATE_TRUSTED(VIEW_INDEX_CRYPTO_STATUS_TRUSTED),
        ERROR(VIEW_INDEX_CRYPTO_STATUS_ERROR);


        final int childToDisplay;

        CryptoStatusDisplayType(int childToDisplay) {
            this.childToDisplay = childToDisplay;
        }
    }

    public void setEmailFeedback(){
        toView.setText(activity.getString(R.string.email_feedback));
    }
}
