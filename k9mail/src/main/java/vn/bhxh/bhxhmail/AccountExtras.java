package vn.bhxh.bhxhmail;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hainv on 10/12/2016.
 */

public class AccountExtras implements Parcelable {

    public static final String KEY_EXTRAS = "AccountExtras.KEY_EXTRAS";
    String imapServer;
    String smtpServer;
    String imapUserName;
    String smtpUserName;
    String imapPass;
    String smtpPass;

    public AccountExtras() {

    }

    protected AccountExtras(Parcel in) {
        imapServer = in.readString();
        smtpServer = in.readString();
        imapUserName = in.readString();
        smtpUserName = in.readString();
        imapPass = in.readString();
        smtpPass = in.readString();
    }

    public static final Creator<AccountExtras> CREATOR = new Creator<AccountExtras>() {
        @Override
        public AccountExtras createFromParcel(Parcel in) {
            return new AccountExtras(in);
        }

        @Override
        public AccountExtras[] newArray(int size) {
            return new AccountExtras[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imapServer);
        dest.writeString(smtpServer);
        dest.writeString(imapUserName);
        dest.writeString(smtpUserName);
        dest.writeString(imapPass);
        dest.writeString(smtpPass);
    }

    public AccountExtras(String imapServer, String smtpServer, String imapUserName, String smtpUserName, String imapPass, String smtpPass) {
        this.imapServer = imapServer;
        this.smtpServer = smtpServer;
        this.imapUserName = imapUserName;
        this.smtpUserName = smtpUserName;
        this.imapPass = imapPass;
        this.smtpPass = smtpPass;
    }

    public String getImapServer() {
        return imapServer;
    }

    public void setImapServer(String imapServer) {
        this.imapServer = imapServer;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public String getImapUserName() {
        return imapUserName;
    }

    public void setImapUserName(String imapUserName) {
        this.imapUserName = imapUserName;
    }

    public String getSmtpUserName() {
        return smtpUserName;
    }

    public void setSmtpUserName(String smtpUserName) {
        this.smtpUserName = smtpUserName;
    }

    public String getImapPass() {
        return imapPass;
    }

    public void setImapPass(String imapPass) {
        this.imapPass = imapPass;
    }

    public String getSmtpPass() {
        return smtpPass;
    }

    public void setSmtpPass(String smtpPass) {
        this.smtpPass = smtpPass;
    }
}
