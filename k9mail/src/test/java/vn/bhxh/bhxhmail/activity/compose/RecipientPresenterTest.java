package vn.bhxh.bhxhmail.activity.compose;


import android.app.LoaderManager;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.Arrays;
import java.util.List;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.helper.ReplyToParser;
import vn.bhxh.bhxhmail.mail.Address;
import vn.bhxh.bhxhmail.mail.Message;
import vn.bhxh.bhxhmail.message.ComposePgpInlineDecider;
import vn.bhxh.bhxhmail.view.RecipientSelectView;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 21)
public class RecipientPresenterTest {
    public static final ReplyToParser.ReplyToAddresses TO_ADDRESSES = new ReplyToParser.ReplyToAddresses(Address.parse("to@example.org"));
    public static final List<Address> ALL_TO_ADDRESSES = Arrays.asList(Address.parse("allTo@example.org"));
    public static final List<Address> ALL_CC_ADDRESSES = Arrays.asList(Address.parse("allCc@example.org"));


    RecipientPresenter recipientPresenter;
    private ReplyToParser replyToParser;
    private ComposePgpInlineDecider composePgpInlineDecider;
    private Account account;
    private RecipientMvpView recipientMvpView;
    private LoaderManager loaderManager;


    @Before
    public void setUp() throws Exception {
        Context context = ShadowApplication.getInstance().getApplicationContext();

        recipientMvpView = mock(RecipientMvpView.class);
        account = mock(Account.class);
        composePgpInlineDecider = mock(ComposePgpInlineDecider.class);
        replyToParser = mock(ReplyToParser.class);
        loaderManager = mock(LoaderManager.class);

        recipientPresenter = new RecipientPresenter(
                context, loaderManager, recipientMvpView, account, composePgpInlineDecider, replyToParser);
    }

    @Test
    public void testInitFromReplyToMessage() throws Exception {
        Message message = mock(Message.class);
        when(replyToParser.getRecipientsToReplyTo(message, account)).thenReturn(TO_ADDRESSES);

        recipientPresenter.initFromReplyToMessage(message, false);

        verify(recipientMvpView).addRecipients(Matchers.eq(Message.RecipientType.TO), any(RecipientSelectView.Recipient[].class));
    }

    @Test
    public void testInitFromReplyToAllMessage() throws Exception {
        Message message = mock(Message.class);
        when(replyToParser.getRecipientsToReplyTo(message, account)).thenReturn(TO_ADDRESSES);
        ReplyToParser.ReplyToAddresses replyToAddresses = new ReplyToParser.ReplyToAddresses(ALL_TO_ADDRESSES, ALL_CC_ADDRESSES);
        when(replyToParser.getRecipientsToReplyAllTo(message, account)).thenReturn(replyToAddresses);

        recipientPresenter.initFromReplyToMessage(message, true);

        verify(recipientMvpView).addRecipients(Matchers.eq(Message.RecipientType.TO), any(RecipientSelectView.Recipient.class));
        verify(recipientMvpView).addRecipients(Matchers.eq(Message.RecipientType.CC), any(RecipientSelectView.Recipient.class));
    }

    @Test
    public void initFromReplyToMessage_shouldCallComposePgpInlineDecider() throws Exception {
        Message message = mock(Message.class);
        when(replyToParser.getRecipientsToReplyTo(message, account)).thenReturn(TO_ADDRESSES);

        recipientPresenter.initFromReplyToMessage(message, false);

        verify(composePgpInlineDecider).shouldReplyInline(message);
    }
}
