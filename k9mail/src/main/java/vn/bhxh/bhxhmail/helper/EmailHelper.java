package vn.bhxh.bhxhmail.helper;


public final class EmailHelper {
    private EmailHelper() {}

    public static String getDomainFromEmailAddress(String email) {
        int separatorIndex = email.lastIndexOf('@');
        if (separatorIndex == -1 || separatorIndex + 1 == email.length()) {
            return null;
        }

        return email.substring(separatorIndex + 1);
    }
}
