package vn.bhxh.bhxhmail.activity.compose;


import java.util.ArrayList;
import java.util.List;

import vn.bhxh.bhxhmail.view.RecipientSelectView;

/** This is an immutable object which contains all relevant metadata entered
 * during e-mail composition to apply cryptographic operations before sending
 * or saving as draft.
 */
public class ComposeCryptoStatus {


    private RecipientPresenter.CryptoProviderState cryptoProviderState;
    private RecipientPresenter.CryptoMode cryptoMode;
    private boolean allKeysAvailable;
    private boolean allKeysVerified;
    private boolean hasRecipients;
    private Long signingKeyId;
    private Long selfEncryptKeyId;
    private String[] recipientAddresses;
    private boolean enablePgpInline;


    public long[] getEncryptKeyIds() {
        if (selfEncryptKeyId == null) {
            return null;
        }
        return new long[] { selfEncryptKeyId };
    }

    public String[] getRecipientAddresses() {
        return recipientAddresses;
    }

    public Long getSigningKeyId() {
        return signingKeyId;
    }

    public RecipientMvpView.CryptoStatusDisplayType getCryptoStatusDisplayType() {
        switch (cryptoProviderState) {
            case UNCONFIGURED:
                return RecipientMvpView.CryptoStatusDisplayType.UNCONFIGURED;
            case UNINITIALIZED:
                return RecipientMvpView.CryptoStatusDisplayType.UNINITIALIZED;
            case LOST_CONNECTION:
            case ERROR:
                return RecipientMvpView.CryptoStatusDisplayType.ERROR;
            case OK:
                // provider status is ok -> return value is based on cryptoMode
                break;
            default:
                throw new AssertionError("all CryptoProviderStates must be handled!");
        }

        switch (cryptoMode) {
            case PRIVATE:
                if (!hasRecipients) {
                    return RecipientMvpView.CryptoStatusDisplayType.PRIVATE_EMPTY;
                } else if (allKeysAvailable && allKeysVerified) {
                    return RecipientMvpView.CryptoStatusDisplayType.PRIVATE_TRUSTED;
                } else if (allKeysAvailable) {
                    return RecipientMvpView.CryptoStatusDisplayType.PRIVATE_UNTRUSTED;
                }
                return RecipientMvpView.CryptoStatusDisplayType.PRIVATE_NOKEY;
            case OPPORTUNISTIC:
                if (!hasRecipients) {
                    return RecipientMvpView.CryptoStatusDisplayType.OPPORTUNISTIC_EMPTY;
                } else if (allKeysAvailable && allKeysVerified) {
                    return RecipientMvpView.CryptoStatusDisplayType.OPPORTUNISTIC_TRUSTED;
                } else if (allKeysAvailable) {
                    return RecipientMvpView.CryptoStatusDisplayType.OPPORTUNISTIC_UNTRUSTED;
                }
                return RecipientMvpView.CryptoStatusDisplayType.OPPORTUNISTIC_NOKEY;
            case SIGN_ONLY:
                return RecipientMvpView.CryptoStatusDisplayType.SIGN_ONLY;
            case DISABLE:
                return RecipientMvpView.CryptoStatusDisplayType.DISABLED;
            default:
                throw new AssertionError("all CryptoModes must be handled!");
        }
    }

    public boolean shouldUsePgpMessageBuilder() {
        return cryptoProviderState != RecipientPresenter.CryptoProviderState.UNCONFIGURED && cryptoMode != RecipientPresenter.CryptoMode.DISABLE;
    }

    public boolean isEncryptionEnabled() {
        return cryptoMode == RecipientPresenter.CryptoMode.PRIVATE || cryptoMode == RecipientPresenter.CryptoMode.OPPORTUNISTIC;
    }

    public boolean isEncryptionOpportunistic() {
        return cryptoMode == RecipientPresenter.CryptoMode.OPPORTUNISTIC;
    }

    public boolean isSigningEnabled() {
        return cryptoMode != RecipientPresenter.CryptoMode.DISABLE && signingKeyId != null;
    }

    public boolean isPgpInlineModeEnabled() {
        return enablePgpInline;
    }

    public boolean isCryptoDisabled() {
        return cryptoMode == RecipientPresenter.CryptoMode.DISABLE;
    }

    public boolean isProviderStateOk() {
        return cryptoProviderState == RecipientPresenter.CryptoProviderState.OK;
    }

    public static class ComposeCryptoStatusBuilder {

        private RecipientPresenter.CryptoProviderState cryptoProviderState;
        private RecipientPresenter.CryptoMode cryptoMode;
        private Long signingKeyId;
        private Long selfEncryptKeyId;
        private List<RecipientSelectView.Recipient> recipients;
        private Boolean enablePgpInline;

        public ComposeCryptoStatusBuilder setCryptoProviderState(RecipientPresenter.CryptoProviderState cryptoProviderState) {
            this.cryptoProviderState = cryptoProviderState;
            return this;
        }

        public ComposeCryptoStatusBuilder setCryptoMode(RecipientPresenter.CryptoMode cryptoMode) {
            this.cryptoMode = cryptoMode;
            return this;
        }

        public ComposeCryptoStatusBuilder setSigningKeyId(long signingKeyId) {
            this.signingKeyId = signingKeyId;
            return this;
        }

        public ComposeCryptoStatusBuilder setSelfEncryptId(long selfEncryptKeyId) {
            this.selfEncryptKeyId = selfEncryptKeyId;
            return this;
        }

        public ComposeCryptoStatusBuilder setRecipients(List<RecipientSelectView.Recipient> recipients) {
            this.recipients = recipients;
            return this;
        }

        public ComposeCryptoStatusBuilder setEnablePgpInline(boolean cryptoEnableCompat) {
            this.enablePgpInline = cryptoEnableCompat;
            return this;
        }

        public ComposeCryptoStatus build() {
            if (cryptoProviderState == null) {
                throw new AssertionError("cryptoProviderState must be set!");
            }
            if (cryptoMode == null) {
                throw new AssertionError("crypto mode must be set!");
            }
            if (recipients == null) {
                throw new AssertionError("recipients must be set!");
            }
            if (enablePgpInline == null) {
                throw new AssertionError("enablePgpInline must be set!");
            }

            ArrayList<String> recipientAddresses = new ArrayList<>();
            boolean allKeysAvailable = true;
            boolean allKeysVerified = true;
            boolean hasRecipients = !recipients.isEmpty();
            for (RecipientSelectView.Recipient recipient : recipients) {
                RecipientSelectView.RecipientCryptoStatus cryptoStatus = recipient.getCryptoStatus();
                recipientAddresses.add(recipient.address.getAddress());
                if (cryptoStatus.isAvailable()) {
                    if (cryptoStatus == RecipientSelectView.RecipientCryptoStatus.AVAILABLE_UNTRUSTED) {
                        allKeysVerified = false;
                    }
                } else {
                    allKeysAvailable = false;
                }
            }

            ComposeCryptoStatus result = new ComposeCryptoStatus();
            result.cryptoProviderState = cryptoProviderState;
            result.cryptoMode = cryptoMode;
            result.recipientAddresses = recipientAddresses.toArray(new String[0]);
            result.allKeysAvailable = allKeysAvailable;
            result.allKeysVerified = allKeysVerified;
            result.hasRecipients = hasRecipients;
            result.signingKeyId = signingKeyId;
            result.selfEncryptKeyId = selfEncryptKeyId;
            result.enablePgpInline = enablePgpInline;
            return result;
        }
    }

    public enum SendErrorState {
        PROVIDER_ERROR, SIGN_KEY_NOT_CONFIGURED, PRIVATE_BUT_MISSING_KEYS
    }

    public SendErrorState getSendErrorStateOrNull() {
        if (cryptoProviderState != RecipientPresenter.CryptoProviderState.OK) {
            // TODO: be more specific about this error
            return SendErrorState.PROVIDER_ERROR;
        }
        boolean isSignKeyMissing = signingKeyId == null;
        if (isSignKeyMissing) {
            return SendErrorState.SIGN_KEY_NOT_CONFIGURED;
        }
        boolean isPrivateModeAndNotAllKeysAvailable = cryptoMode == RecipientPresenter.CryptoMode.PRIVATE && !allKeysAvailable;
        if (isPrivateModeAndNotAllKeysAvailable) {
            return SendErrorState.PRIVATE_BUT_MISSING_KEYS;
        }

        return null;
    }

    public enum AttachErrorState {
        IS_INLINE
    }

    public AttachErrorState getAttachErrorStateOrNull() {
        if (cryptoProviderState == RecipientPresenter.CryptoProviderState.UNCONFIGURED) {
            return null;
        }

        if (enablePgpInline) {
            return AttachErrorState.IS_INLINE;
        }

        return null;
    }

}
