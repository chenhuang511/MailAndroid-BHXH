package vn.bhxh.bhxhmail.helper;


public class HtmlSanitizerHelper {
    public static HtmlSanitizer getDummyHtmlSanitizer() {
        return new HtmlSanitizer() {
            @Override
            public String sanitize(String html) {
                return html;
            }
        };
    }
}
