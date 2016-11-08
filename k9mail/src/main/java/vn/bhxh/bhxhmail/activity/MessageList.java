package vn.bhxh.bhxhmail.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Collection;
import java.util.List;

import vn.bhxh.bhxhmail.Account;
import vn.bhxh.bhxhmail.Account.SortType;
import vn.bhxh.bhxhmail.BaseAccount;
import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.Preferences;
import vn.bhxh.bhxhmail.R;
import vn.bhxh.bhxhmail.activity.compose.MessageActions;
import vn.bhxh.bhxhmail.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;
import vn.bhxh.bhxhmail.activity.setup.AccountSettings;
import vn.bhxh.bhxhmail.activity.setup.AccountSetupTypes;
import vn.bhxh.bhxhmail.activity.setup.FolderSettings;
import vn.bhxh.bhxhmail.activity.setup.Prefs;
import vn.bhxh.bhxhmail.controller.MessagingController;
import vn.bhxh.bhxhmail.fragment.MessageListFragment;
import vn.bhxh.bhxhmail.mailstore.StorageManager;
import vn.bhxh.bhxhmail.preferences.StorageEditor;
import vn.bhxh.bhxhmail.search.LocalSearch;
import vn.bhxh.bhxhmail.search.SearchAccount;
import vn.bhxh.bhxhmail.search.SearchSpecification;
import vn.bhxh.bhxhmail.ui.messageview.MessageViewFragment;
import vn.bhxh.bhxhmail.ui.messageview.MessageViewFragment.MessageViewFragmentListener;
import vn.bhxh.bhxhmail.view.MessageHeader;
import vn.bhxh.bhxhmail.view.ViewSwitcher;
import vn.bhxh.bhxhmail.view.ViewSwitcher.OnSwitchCompleteListener;


/**
 * MessageList is the primary user interface for the program. This Activity
 * shows a list of messages.
 * From this Activity the user can perform all standard message operations.
 */
public class MessageList extends K9Activity implements MessageListFragment.MessageListFragmentListener,
        MessageViewFragmentListener, OnBackStackChangedListener, OnSwipeGestureListener,
        OnSwitchCompleteListener {

    // for this activity
    private static final String EXTRA_SEARCH = "search";
    private static final String EXTRA_NO_THREADING = "no_threading";

    private static final String ACTION_SHORTCUT = "shortcut";
    private static final String EXTRA_SPECIAL_FOLDER = "special_folder";

    private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";

    // used for remote search
    public static final String EXTRA_SEARCH_ACCOUNT = "com.fsck.k9.search_account";
    private static final String EXTRA_SEARCH_FOLDER = "com.fsck.k9.search_folder";

    private static final String STATE_DISPLAY_MODE = "displayMode";
    private static final String STATE_MESSAGE_LIST_WAS_DISPLAYED = "messageListWasDisplayed";

    // Used for navigating to next/previous message
    private static final int PREVIOUS = 1;
    private static final int NEXT = 2;

    public static final int REQUEST_MASK_PENDING_INTENT = 1 << 16;

    public static void actionDisplaySearch(Context context, SearchSpecification search,
                                           boolean noThreading, boolean newTask) {
        actionDisplaySearch(context, search, noThreading, newTask, true);
    }

    public static void actionDisplaySearch(Context context, SearchSpecification search,
                                           boolean noThreading, boolean newTask, boolean clearTop) {
        context.startActivity(
                intentDisplaySearch(context, search, noThreading, newTask, clearTop));
    }

    public static Intent intentDisplaySearch(Context context, SearchSpecification search,
                                             boolean noThreading, boolean newTask, boolean clearTop) {
        Intent intent = new Intent(context, MessageList.class);
        intent.putExtra(EXTRA_SEARCH, search);
        intent.putExtra(EXTRA_NO_THREADING, noThreading);

        if (clearTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (newTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        return intent;
    }

    public static Intent shortcutIntent(Context context, String specialFolder) {
        Intent intent = new Intent(context, MessageList.class);
        intent.setAction(ACTION_SHORTCUT);
        intent.putExtra(EXTRA_SPECIAL_FOLDER, specialFolder);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    public static Intent actionDisplayMessageIntent(Context context,
                                                    MessageReference messageReference) {
        Intent intent = new Intent(context, MessageList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_MESSAGE_REFERENCE, messageReference);
        return intent;
    }


    private enum DisplayMode {
        MESSAGE_LIST,
        MESSAGE_VIEW,
        SPLIT_VIEW
    }


    private StorageManager.StorageListener mStorageListener = new StorageListenerImplementation();

    //    private ActionBar mActionBar;
//    private View mActionBarMessageList;
//    private View mActionBarMessageView;
//    private MessageTitleView mActionBarSubject;
//    private TextView mActionBarTitle;
//    private TextView mActionBarSubTitle;
//    private TextView mActionBarUnread;
    private Menu mMenu;

    private ViewGroup mMessageViewContainer;
    private View mMessageViewPlaceHolder;

    private MessageListFragment mMessageListFragment;
    private MessageViewFragment mMessageViewFragment;
    private int mFirstBackStackId = -1;

    private Account mAccount;
    private String mFolderName;
    private LocalSearch mSearch;
    private boolean mSingleFolderMode;
    private boolean mSingleAccountMode;

    //    private ProgressBar mActionBarProgress;
    private MenuItem mMenuButtonCheckMail;
    private View mActionButtonIndeterminateProgress;
    private int mLastDirection = (K9.messageViewShowNext()) ? NEXT : PREVIOUS;

    private DrawerLayout mDrawerLayout;
    private ImageView mMenuSetting;
    private TextView mTitle;
    private LinearLayout mLayoutExpand;
    private LinearLayout mLayoutAcc;
    private ImageView mIconExpand;
    private TextView mCurrentName;
    private TextView mCurrentEmail;
    private TextView mCommonCancel;
    private RelativeLayout mLayoutSearch;
    private EditText mInputSearch;
    private ImageView mNew;
    private int mCurrentAcc;
    private int mLangCode;
    private SharedPreferences sharedPreferences;
    /**
     * {@code true} if the message list should be displayed as flat list (i.e. no threading)
     * regardless whether or not message threading was enabled in the settings. This is used for
     * filtered views, e.g. when only displaying the unread messages in a folder.
     */
    private boolean mNoThreading;
    private DisplayMode mDisplayMode;
    private MessageReference mMessageReference;
    /**
     * {@code true} when the message list was displayed once. This is used in
     * {@link #onBackPressed()} to decide whether to go from the message view to the message list or
     * finish the activity.
     */
    private boolean mMessageListWasDisplayed = false;
    private ViewSwitcher mViewSwitcher;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UpgradeDatabases.actionUpgradeDatabases(this, getIntent())) {
            finish();
            return;
        }
        sharedPreferences = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        mLangCode = sharedPreferences.getInt("Lang", 1);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (useSplitView()) {
            setContentView(vn.bhxh.bhxhmail.R.layout.split_message_list);
        } else {
            setContentView(vn.bhxh.bhxhmail.R.layout.message_list);
            mViewSwitcher = (ViewSwitcher) findViewById(vn.bhxh.bhxhmail.R.id.container);
            mLayoutExpand = (LinearLayout) findViewById(vn.bhxh.bhxhmail.R.id.layout_expand);
            mLayoutAcc = (LinearLayout) findViewById(vn.bhxh.bhxhmail.R.id.layout_acc);
            mViewSwitcher.setFirstInAnimation(AnimationUtils.loadAnimation(this, vn.bhxh.bhxhmail.R.anim.slide_in_left));
            mViewSwitcher.setFirstOutAnimation(AnimationUtils.loadAnimation(this, vn.bhxh.bhxhmail.R.anim.slide_out_right));
            mViewSwitcher.setSecondInAnimation(AnimationUtils.loadAnimation(this, vn.bhxh.bhxhmail.R.anim.slide_in_right));
            mViewSwitcher.setSecondOutAnimation(AnimationUtils.loadAnimation(this, vn.bhxh.bhxhmail.R.anim.slide_out_left));
            mViewSwitcher.setOnSwitchCompleteListener(this);
            mDrawerLayout = (DrawerLayout) findViewById(vn.bhxh.bhxhmail.R.id.drawer_layout);
            mMenuSetting = (ImageView) findViewById(vn.bhxh.bhxhmail.R.id.ic_menu);
            mIconExpand = (ImageView) findViewById(vn.bhxh.bhxhmail.R.id.ic_expand);
            mNew = (ImageView) findViewById(vn.bhxh.bhxhmail.R.id.common_ic_new);
            mCurrentName = (TextView) findViewById(vn.bhxh.bhxhmail.R.id.c_name);
            mCurrentEmail = (TextView) findViewById(vn.bhxh.bhxhmail.R.id.c_email);
            mTitle = (TextView) findViewById(vn.bhxh.bhxhmail.R.id.common_title);
            mLayoutSearch = (RelativeLayout) findViewById(vn.bhxh.bhxhmail.R.id.layout_search);
            mInputSearch = (EditText) findViewById(vn.bhxh.bhxhmail.R.id.input_search);
            mCommonCancel = (TextView) findViewById(R.id.common_cancel);
            mMenuSetting.setVisibility(View.VISIBLE);
            mMenuSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMenu();
                }
            });
            mTitle.setText(getString(R.string.c_inbox));
//            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            mInputSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        searchEmail();
                        return true;
                    }
                    return false;
                }
            });
            mNew.setVisibility(View.VISIBLE);
            mCommonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMessageListFragment != null) {
                        mMessageListFragment.selectAll(false);
                        mMessageListFragment.updateActionModeTitle();
                        showCancel(false);
                    }
                }
            });
        }
        initializeActionBar();

        // Enable gesture detection for MessageLists
        setupGestureDetector(this);

        if (!decodeExtras(getIntent())) {
            return;
        }

        findFragments();
        initializeDisplayMode(savedInstanceState);
        initializeLayout();
        initializeFragments();
        displayViews();

//        ChangeLog cl = new ChangeLog(this);
//        if (cl.isFirstRun()) {
//            cl.getLogDialog().show();
//        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

        if (mFirstBackStackId >= 0) {
            getFragmentManager().popBackStackImmediate(mFirstBackStackId,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            mFirstBackStackId = -1;
        }
        removeMessageListFragment();
        removeMessageViewFragment();

        mMessageReference = null;
        mSearch = null;
        mFolderName = null;

        if (!decodeExtras(intent)) {
            return;
        }

        initializeDisplayMode(null);
        initializeFragments();
        displayViews();
    }

    /**
     * Get references to existing fragments if the activity was restarted.
     */
    private void findFragments() {
        FragmentManager fragmentManager = getFragmentManager();
        mMessageListFragment = (MessageListFragment) fragmentManager.findFragmentById(
                vn.bhxh.bhxhmail.R.id.message_list_container);
        mMessageViewFragment = (MessageViewFragment) fragmentManager.findFragmentById(
                vn.bhxh.bhxhmail.R.id.message_view_container);
    }

    /**
     * Create fragment instances if necessary.
     *
     * @see #findFragments()
     */
    private void initializeFragments() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);

        boolean hasMessageListFragment = (mMessageListFragment != null);

        if (!hasMessageListFragment) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            mMessageListFragment = MessageListFragment.newInstance(mSearch, false,
                    (K9.isThreadedViewEnabled() && !mNoThreading));
            ft.add(vn.bhxh.bhxhmail.R.id.message_list_container, mMessageListFragment);
            ft.commit();
        }

        // Check if the fragment wasn't restarted and has a MessageReference in the arguments. If
        // so, open the referenced message.
        if (!hasMessageListFragment && mMessageViewFragment == null &&
                mMessageReference != null) {
            openMessage(mMessageReference);
        }
    }

    /**
     * Set the initial display mode (message list, message view, or split view).
     * <p>
     * <p><strong>Note:</strong>
     * This method has to be called after {@link #findFragments()} because the result depends on
     * the availability of a {@link MessageViewFragment} instance.
     * </p>
     *
     * @param savedInstanceState The saved instance state that was passed to the activity as argument to
     *                           {@link #onCreate(Bundle)}. May be {@code null}.
     */
    private void initializeDisplayMode(Bundle savedInstanceState) {
        if (useSplitView()) {
            mDisplayMode = DisplayMode.SPLIT_VIEW;
            return;
        }

        if (savedInstanceState != null) {
            DisplayMode savedDisplayMode =
                    (DisplayMode) savedInstanceState.getSerializable(STATE_DISPLAY_MODE);
            if (savedDisplayMode != DisplayMode.SPLIT_VIEW) {
                mDisplayMode = savedDisplayMode;
                return;
            }
        }

        if (mMessageViewFragment != null || mMessageReference != null) {
            mDisplayMode = DisplayMode.MESSAGE_VIEW;
        } else {
            mDisplayMode = DisplayMode.MESSAGE_LIST;
        }
    }

    private boolean useSplitView() {
        K9.SplitViewMode splitViewMode = K9.getSplitViewMode();
        int orientation = getResources().getConfiguration().orientation;

        return (splitViewMode == K9.SplitViewMode.ALWAYS ||
                (splitViewMode == K9.SplitViewMode.WHEN_IN_LANDSCAPE &&
                        orientation == Configuration.ORIENTATION_LANDSCAPE));
    }

    private void initializeLayout() {
        mMessageViewContainer = (ViewGroup) findViewById(vn.bhxh.bhxhmail.R.id.message_view_container);

        LayoutInflater layoutInflater = getLayoutInflater();
        mMessageViewPlaceHolder = layoutInflater.inflate(vn.bhxh.bhxhmail.R.layout.empty_message_view, mMessageViewContainer, false);
    }

    private void displayViews() {
        switch (mDisplayMode) {
            case MESSAGE_LIST: {
                showMessageList();
                break;
            }
            case MESSAGE_VIEW: {
                showMessageView();
                break;
            }
            case SPLIT_VIEW: {
                mMessageListWasDisplayed = true;
                if (mMessageViewFragment == null) {
                    showMessageViewPlaceHolder();
                } else {
                    MessageReference activeMessage = mMessageViewFragment.getMessageReference();
                    if (activeMessage != null) {
                        mMessageListFragment.setActiveMessage(activeMessage);
                    }
                }
                break;
            }
        }
    }

    private boolean decodeExtras(Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action) && intent.getData() != null) {
            Uri uri = intent.getData();
            List<String> segmentList = uri.getPathSegments();

            String accountId = segmentList.get(0);
            Collection<Account> accounts = Preferences.getPreferences(this).getAvailableAccounts();
            for (Account account : accounts) {
                if (String.valueOf(account.getAccountNumber()).equals(accountId)) {
                    String folderName = segmentList.get(1);
                    String messageUid = segmentList.get(2);
                    mMessageReference = new MessageReference(account.getUuid(), folderName, messageUid, null);
                    break;
                }
            }
        } else if (ACTION_SHORTCUT.equals(action)) {
            // Handle shortcut intents
            String specialFolder = intent.getStringExtra(EXTRA_SPECIAL_FOLDER);
            if (SearchAccount.UNIFIED_INBOX.equals(specialFolder)) {
                mSearch = SearchAccount.createUnifiedInboxAccount(this).getRelatedSearch();
            } else if (SearchAccount.ALL_MESSAGES.equals(specialFolder)) {
                mSearch = SearchAccount.createAllMessagesAccount(this).getRelatedSearch();
            }
        } else if (intent.getStringExtra(SearchManager.QUERY) != null) {
            // check if this intent comes from the system search ( remote )
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                //Query was received from Search Dialog
                String query = intent.getStringExtra(SearchManager.QUERY).trim();

                mSearch = new LocalSearch(getString(vn.bhxh.bhxhmail.R.string.search_results));
                mSearch.setManualSearch(true);
                mNoThreading = true;

                mSearch.or(new SearchSpecification.SearchCondition(SearchSpecification.SearchField.SENDER, SearchSpecification.Attribute.CONTAINS, query));
                mSearch.or(new SearchSpecification.SearchCondition(SearchSpecification.SearchField.SUBJECT, SearchSpecification.Attribute.CONTAINS, query));
                mSearch.or(new SearchSpecification.SearchCondition(SearchSpecification.SearchField.MESSAGE_CONTENTS, SearchSpecification.Attribute.CONTAINS, query));

                Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
                if (appData != null) {
                    mSearch.addAccountUuid(appData.getString(EXTRA_SEARCH_ACCOUNT));
                    // searches started from a folder list activity will provide an account, but no folder
                    if (appData.getString(EXTRA_SEARCH_FOLDER) != null) {
                        mSearch.addAllowedFolder(appData.getString(EXTRA_SEARCH_FOLDER));
                    }
                } else {
                    mSearch.addAccountUuid(LocalSearch.ALL_ACCOUNTS);
                }
            }
        } else {
            // regular LocalSearch object was passed
            mSearch = intent.getParcelableExtra(EXTRA_SEARCH);
            mNoThreading = intent.getBooleanExtra(EXTRA_NO_THREADING, false);
        }

        if (mMessageReference == null) {
            mMessageReference = intent.getParcelableExtra(EXTRA_MESSAGE_REFERENCE);
        }

        if (mMessageReference != null) {
            mSearch = new LocalSearch();
            mSearch.addAccountUuid(mMessageReference.getAccountUuid());
            mSearch.addAllowedFolder(mMessageReference.getFolderName());
        }

        if (mSearch == null) {
            // We've most likely been started by an old unread widget
            String accountUuid = intent.getStringExtra("account");
            String folderName = intent.getStringExtra("folder");

            mSearch = new LocalSearch(folderName);
            mSearch.addAccountUuid((accountUuid == null) ? "invalid" : accountUuid);
            if (folderName != null) {
                mSearch.addAllowedFolder(folderName);
            }
        }

        Preferences prefs = Preferences.getPreferences(getApplicationContext());

        String[] accountUuids = mSearch.getAccountUuids();
        if (mSearch.searchAllAccounts()) {
            List<Account> accounts = prefs.getAccounts();
            mSingleAccountMode = (accounts.size() == 1);
            if (mSingleAccountMode) {
                mAccount = accounts.get(0);
            }
        } else {
            mSingleAccountMode = (accountUuids.length == 1);
            if (mSingleAccountMode) {
                mAccount = prefs.getAccount(accountUuids[0]);
            }
        }
        mSingleFolderMode = mSingleAccountMode && (mSearch.getFolderNames().size() == 1);

        if (mSingleAccountMode && (mAccount == null || !mAccount.isAvailable(this))) {
            Log.i(K9.LOG_TAG, "not opening MessageList of unavailable account");
            onAccountUnavailable();
            return false;
        }

        if (mSingleFolderMode) {
            mFolderName = mSearch.getFolderNames().get(0);
        }

        // now we know if we are in single account mode and need a subtitle
//        mActionBarSubTitle.setVisibility((!mSingleFolderMode) ? View.GONE : View.VISIBLE);

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();

        StorageManager.getInstance(getApplication()).removeListener(mStorageListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLangCode != sharedPreferences.getInt("Lang", 1)) {
            Intent intent = new Intent(this, MessageList.class);
            startActivity(intent);
            finish();
        }

        if (!(this instanceof Search)) {
            Search.setActive(false);
        }

        if (mAccount != null && !mAccount.isAvailable(this)) {
            onAccountUnavailable();
            return;
        }
        StorageManager.getInstance(getApplication()).addListener(mStorageListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(STATE_DISPLAY_MODE, mDisplayMode);
        outState.putBoolean(STATE_MESSAGE_LIST_WAS_DISPLAYED, mMessageListWasDisplayed);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mMessageListWasDisplayed = savedInstanceState.getBoolean(STATE_MESSAGE_LIST_WAS_DISPLAYED);
    }

    private void initializeActionBar() {
//        mActionBar = getActionBar();
//
//        mActionBar.setDisplayShowCustomEnabled(true);
//        mActionBar.setCustomView(R.layout.actionbar_custom);
//
//        View customView = mActionBar.getCustomView();
//        mActionBarMessageList = customView.findViewById(R.id.actionbar_message_list);
//        mActionBarMessageView = customView.findViewById(R.id.actionbar_message_view);
//        mActionBarSubject = (MessageTitleView) customView.findViewById(R.id.message_title_view);
//        mActionBarTitle = (TextView) customView.findViewById(R.id.actionbar_title_first);
//        mActionBarSubTitle = (TextView) customView.findViewById(R.id.actionbar_title_sub);
//        mActionBarUnread = (TextView) customView.findViewById(R.id.actionbar_unread_count);
//        mActionBarProgress = (ProgressBar) customView.findViewById(R.id.actionbar_progress);
        mActionButtonIndeterminateProgress = getActionButtonIndeterminateProgress();

//        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("InflateParams")
    private View getActionButtonIndeterminateProgress() {
        return getLayoutInflater().inflate(vn.bhxh.bhxhmail.R.layout.actionbar_indeterminate_progress_actionview, null);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean ret = false;
        if (KeyEvent.ACTION_DOWN == event.getAction()) {
            ret = onCustomKeyDown(event.getKeyCode(), event);
        }
        if (!ret) {
            ret = super.dispatchKeyEvent(event);
        }
        return ret;
    }

    @Override
    public void onBackPressed() {
        if (mDisplayMode == DisplayMode.MESSAGE_VIEW && mMessageListWasDisplayed) {
            showMessageList();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Handle hotkeys
     * <p>
     * <p>
     * This method is called by {@link #dispatchKeyEvent(KeyEvent)} before any view had the chance
     * to consume this key event.
     * </p>
     *
     * @param keyCode The value in {@code event.getKeyCode()}.
     * @param event   Description of the key event.
     * @return {@code true} if this event was consumed.
     */
    public boolean onCustomKeyDown(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP: {
                if (mMessageViewFragment != null && mDisplayMode != DisplayMode.MESSAGE_LIST &&
                        K9.useVolumeKeysForNavigationEnabled()) {
                    showPreviousMessage();
                    return true;
                } else if (mDisplayMode != DisplayMode.MESSAGE_VIEW &&
                        K9.useVolumeKeysForListNavigationEnabled()) {
                    mMessageListFragment.onMoveUp();
                    return true;
                }

                break;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                if (mMessageViewFragment != null && mDisplayMode != DisplayMode.MESSAGE_LIST &&
                        K9.useVolumeKeysForNavigationEnabled()) {
                    showNextMessage();
                    return true;
                } else if (mDisplayMode != DisplayMode.MESSAGE_VIEW &&
                        K9.useVolumeKeysForListNavigationEnabled()) {
                    mMessageListFragment.onMoveDown();
                    return true;
                }

                break;
            }
            case KeyEvent.KEYCODE_C: {
                mMessageListFragment.onCompose();
                return true;
            }
            case KeyEvent.KEYCODE_Q: {
                if (mMessageListFragment != null && mMessageListFragment.isSingleAccountMode()) {
                    onShowFolderList();
                }
                return true;
            }
            case KeyEvent.KEYCODE_O: {
                mMessageListFragment.onCycleSort();
                return true;
            }
            case KeyEvent.KEYCODE_I: {
                mMessageListFragment.onReverseSort();
                return true;
            }
            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_D: {
                if (mDisplayMode == DisplayMode.MESSAGE_LIST) {
                    mMessageListFragment.onDelete();
                } else if (mMessageViewFragment != null) {
                    mMessageViewFragment.onDelete();
                }
                return true;
            }
            case KeyEvent.KEYCODE_S: {
                mMessageListFragment.toggleMessageSelect();
                return true;
            }
            case KeyEvent.KEYCODE_G: {
                if (mDisplayMode == DisplayMode.MESSAGE_LIST) {
                    mMessageListFragment.onToggleFlagged();
                } else if (mMessageViewFragment != null) {
                    mMessageViewFragment.onToggleFlagged();
                }
                return true;
            }
            case KeyEvent.KEYCODE_M: {
                if (mDisplayMode == DisplayMode.MESSAGE_LIST) {
                    mMessageListFragment.onMove();
                } else if (mMessageViewFragment != null) {
                    mMessageViewFragment.onMove();
                }
                return true;
            }
            case KeyEvent.KEYCODE_V: {
                if (mDisplayMode == DisplayMode.MESSAGE_LIST) {
                    mMessageListFragment.onArchive();
                } else if (mMessageViewFragment != null) {
                    mMessageViewFragment.onArchive();
                }
                return true;
            }
            case KeyEvent.KEYCODE_Y: {
                if (mDisplayMode == DisplayMode.MESSAGE_LIST) {
                    mMessageListFragment.onCopy();
                } else if (mMessageViewFragment != null) {
                    mMessageViewFragment.onCopy();
                }
                return true;
            }
            case KeyEvent.KEYCODE_Z: {
                if (mDisplayMode == DisplayMode.MESSAGE_LIST) {
                    mMessageListFragment.onToggleRead();
                } else if (mMessageViewFragment != null) {
                    mMessageViewFragment.onToggleRead();
                }
                return true;
            }
            case KeyEvent.KEYCODE_F: {
                if (mMessageViewFragment != null) {
                    mMessageViewFragment.onForward();
                }
                return true;
            }
            case KeyEvent.KEYCODE_A: {
                if (mMessageViewFragment != null) {
                    mMessageViewFragment.onReplyAll();
                }
                return true;
            }
            case KeyEvent.KEYCODE_R: {
                if (mMessageViewFragment != null) {
                    mMessageViewFragment.onReply();
                }
                return true;
            }
            case KeyEvent.KEYCODE_J:
            case KeyEvent.KEYCODE_P: {
                if (mMessageViewFragment != null) {
                    showPreviousMessage();
                }
                return true;
            }
            case KeyEvent.KEYCODE_N:
            case KeyEvent.KEYCODE_K: {
                if (mMessageViewFragment != null) {
                    showNextMessage();
                }
                return true;
            }
            /* FIXME
            case KeyEvent.KEYCODE_Z: {
                mMessageViewFragment.zoom(event);
                return true;
            }*/
            case KeyEvent.KEYCODE_H: {
                Toast toast = Toast.makeText(this, vn.bhxh.bhxhmail.R.string.message_list_help_key, Toast.LENGTH_LONG);
                toast.show();
                return true;
            }

        }

        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Swallow these events too to avoid the audible notification of a volume change
        if (K9.useVolumeKeysForListNavigationEnabled()) {
            if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                if (K9.DEBUG)
                    Log.v(K9.LOG_TAG, "Swallowed key up.");
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void onAccounts() {
        Accounts.listAccounts(this);
        finish();
    }

    private void onShowFolderList() {
        FolderList.actionHandleAccount(this, mAccount);
        finish();
    }

    private void onEditPrefs() {
        Prefs.actionPrefs(this);
    }

    private void onEditAccount() {
        AccountSettings.actionSettings(this, mAccount);
    }

    @Override
    public boolean onSearchRequested() {
        return mMessageListFragment.onSearchRequested();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home: {
                goBack();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.compose: {
                mMessageListFragment.onCompose();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.toggle_message_view_theme: {
                onToggleTheme();
                return true;
            }
            // MessageList
            case vn.bhxh.bhxhmail.R.id.check_mail: {
                mMessageListFragment.checkMail();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.set_sort_date: {
                mMessageListFragment.changeSort(SortType.SORT_DATE);
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.set_sort_arrival: {
                mMessageListFragment.changeSort(SortType.SORT_ARRIVAL);
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.set_sort_subject: {
                mMessageListFragment.changeSort(SortType.SORT_SUBJECT);
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.set_sort_sender: {
                mMessageListFragment.changeSort(SortType.SORT_SENDER);
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.set_sort_flag: {
                mMessageListFragment.changeSort(SortType.SORT_FLAGGED);
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.set_sort_unread: {
                mMessageListFragment.changeSort(SortType.SORT_UNREAD);
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.set_sort_attach: {
                mMessageListFragment.changeSort(SortType.SORT_ATTACHMENT);
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.select_all: {
                mMessageListFragment.selectAll();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.app_settings: {
                onEditPrefs();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.account_settings: {
                onEditAccount();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.search: {
                mMessageListFragment.onSearchRequested();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.search_remote: {
                mMessageListFragment.onRemoteSearch();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.mark_all_as_read: {
                mMessageListFragment.confirmMarkAllAsRead();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.show_folder_list: {
                onShowFolderList();
                return true;
            }
            // MessageView
            case vn.bhxh.bhxhmail.R.id.next_message: {
                showNextMessage();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.previous_message: {
                showPreviousMessage();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.delete: {
                mMessageViewFragment.onDelete();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.reply: {
                mMessageViewFragment.onReply();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.reply_all: {
                mMessageViewFragment.onReplyAll();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.forward: {
                mMessageViewFragment.onForward();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.share: {
                mMessageViewFragment.onSendAlternate();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.toggle_unread: {
                mMessageViewFragment.onToggleRead();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.archive:
            case vn.bhxh.bhxhmail.R.id.refile_archive: {
                mMessageViewFragment.onArchive();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.spam:
            case vn.bhxh.bhxhmail.R.id.refile_spam: {
                mMessageViewFragment.onSpam();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.move:
            case vn.bhxh.bhxhmail.R.id.refile_move: {
                mMessageViewFragment.onMove();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.copy:
            case vn.bhxh.bhxhmail.R.id.refile_copy: {
                mMessageViewFragment.onCopy();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.select_text: {
                mMessageViewFragment.onSelectText();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.show_headers:
            case vn.bhxh.bhxhmail.R.id.hide_headers: {
                mMessageViewFragment.onToggleAllHeadersView();
                updateMenu();
                return true;
            }
        }

        if (!mSingleFolderMode) {
            // None of the options after this point are "safe" for search results
            //TODO: This is not true for "unread" and "starred" searches in regular folders
            return false;
        }

        switch (itemId) {
            case vn.bhxh.bhxhmail.R.id.send_messages: {
                mMessageListFragment.onSendPendingMessages();
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.folder_settings: {
                if (mFolderName != null) {
                    FolderSettings.actionSettings(this, mAccount, mFolderName);
                }
                return true;
            }
            case vn.bhxh.bhxhmail.R.id.expunge: {
                mMessageListFragment.onExpunge();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(vn.bhxh.bhxhmail.R.menu.message_list_option, menu);
        mMenu = menu;
        mMenuButtonCheckMail = menu.findItem(vn.bhxh.bhxhmail.R.id.check_mail);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        configureMenu(menu);
        return true;
    }

    /**
     * Hide menu items not appropriate for the current context.
     * <p>
     * <p><strong>Note:</strong>
     * Please adjust the comments in {@code res/menu/message_list_option.xml} if you change the
     * visibility of a menu item in this method.
     * </p>
     *
     * @param menu The {@link Menu} instance that should be modified. May be {@code null}; in that case
     *             the method does nothing and immediately returns.
     */
    private void configureMenu(Menu menu) {
        if (menu == null) {
            return;
        }

        // Set visibility of account/folder settings menu items
        if (mMessageListFragment == null) {
            menu.findItem(vn.bhxh.bhxhmail.R.id.account_settings).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.folder_settings).setVisible(false);
        } else {
            menu.findItem(vn.bhxh.bhxhmail.R.id.account_settings).setVisible(
                    mMessageListFragment.isSingleAccountMode());
            menu.findItem(vn.bhxh.bhxhmail.R.id.folder_settings).setVisible(
                    mMessageListFragment.isSingleFolderMode());
        }

        /*
         * Set visibility of menu items related to the message view
         */

        if (mDisplayMode == DisplayMode.MESSAGE_LIST
                || mMessageViewFragment == null
                || !mMessageViewFragment.isInitialized()) {
            menu.findItem(vn.bhxh.bhxhmail.R.id.next_message).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.previous_message).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.single_message_options).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.delete).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.compose).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.archive).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.move).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.copy).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.spam).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.refile).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.toggle_unread).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.select_text).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.toggle_message_view_theme).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.show_headers).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.hide_headers).setVisible(false);
        } else {
            // hide prev/next buttons in split mode
            if (mDisplayMode != DisplayMode.MESSAGE_VIEW) {
                menu.findItem(vn.bhxh.bhxhmail.R.id.next_message).setVisible(false);
                menu.findItem(vn.bhxh.bhxhmail.R.id.previous_message).setVisible(false);
            } else {
                MessageReference ref = mMessageViewFragment.getMessageReference();
                boolean initialized = (mMessageListFragment != null &&
                        mMessageListFragment.isLoadFinished());
                boolean canDoPrev = (initialized && !mMessageListFragment.isFirst(ref));
                boolean canDoNext = (initialized && !mMessageListFragment.isLast(ref));

                MenuItem prev = menu.findItem(vn.bhxh.bhxhmail.R.id.previous_message);
                prev.setEnabled(canDoPrev);
                prev.getIcon().setAlpha(canDoPrev ? 255 : 127);

                MenuItem next = menu.findItem(vn.bhxh.bhxhmail.R.id.next_message);
                next.setEnabled(canDoNext);
                next.getIcon().setAlpha(canDoNext ? 255 : 127);
            }

            MenuItem toggleTheme = menu.findItem(vn.bhxh.bhxhmail.R.id.toggle_message_view_theme);
            if (K9.useFixedMessageViewTheme()) {
                toggleTheme.setVisible(false);
            } else {
                // Set title of menu item to switch to dark/light theme
                if (K9.getK9MessageViewTheme() == K9.Theme.DARK) {
                    toggleTheme.setTitle(vn.bhxh.bhxhmail.R.string.message_view_theme_action_light);
                } else {
                    toggleTheme.setTitle(vn.bhxh.bhxhmail.R.string.message_view_theme_action_dark);
                }
                toggleTheme.setVisible(true);
            }

            // Set title of menu item to toggle the read state of the currently displayed message
            if (mMessageViewFragment.isMessageRead()) {
                menu.findItem(vn.bhxh.bhxhmail.R.id.toggle_unread).setTitle(vn.bhxh.bhxhmail.R.string.mark_as_unread_action);
            } else {
                menu.findItem(vn.bhxh.bhxhmail.R.id.toggle_unread).setTitle(vn.bhxh.bhxhmail.R.string.mark_as_read_action);
            }

            // Jellybean has built-in long press selection support
            menu.findItem(vn.bhxh.bhxhmail.R.id.select_text).setVisible(Build.VERSION.SDK_INT < 16);

            menu.findItem(vn.bhxh.bhxhmail.R.id.delete).setVisible(K9.isMessageViewDeleteActionVisible());

            /*
             * Set visibility of copy, move, archive, spam in action bar and refile submenu
             */
            if (mMessageViewFragment.isCopyCapable()) {
                menu.findItem(vn.bhxh.bhxhmail.R.id.copy).setVisible(K9.isMessageViewCopyActionVisible());
                menu.findItem(vn.bhxh.bhxhmail.R.id.refile_copy).setVisible(true);
            } else {
                menu.findItem(vn.bhxh.bhxhmail.R.id.copy).setVisible(false);
                menu.findItem(vn.bhxh.bhxhmail.R.id.refile_copy).setVisible(false);
            }

            if (mMessageViewFragment.isMoveCapable()) {
                boolean canMessageBeArchived = mMessageViewFragment.canMessageBeArchived();
                boolean canMessageBeMovedToSpam = mMessageViewFragment.canMessageBeMovedToSpam();

                menu.findItem(vn.bhxh.bhxhmail.R.id.move).setVisible(K9.isMessageViewMoveActionVisible());
                menu.findItem(vn.bhxh.bhxhmail.R.id.archive).setVisible(canMessageBeArchived &&
                        K9.isMessageViewArchiveActionVisible());
                menu.findItem(vn.bhxh.bhxhmail.R.id.spam).setVisible(canMessageBeMovedToSpam &&
                        K9.isMessageViewSpamActionVisible());

                menu.findItem(vn.bhxh.bhxhmail.R.id.refile_move).setVisible(true);
                menu.findItem(vn.bhxh.bhxhmail.R.id.refile_archive).setVisible(canMessageBeArchived);
                menu.findItem(vn.bhxh.bhxhmail.R.id.refile_spam).setVisible(canMessageBeMovedToSpam);
            } else {
                menu.findItem(vn.bhxh.bhxhmail.R.id.move).setVisible(false);
                menu.findItem(vn.bhxh.bhxhmail.R.id.archive).setVisible(false);
                menu.findItem(vn.bhxh.bhxhmail.R.id.spam).setVisible(false);

                menu.findItem(vn.bhxh.bhxhmail.R.id.refile).setVisible(false);
            }

            if (mMessageViewFragment.allHeadersVisible()) {
                menu.findItem(vn.bhxh.bhxhmail.R.id.show_headers).setVisible(false);
            } else {
                menu.findItem(vn.bhxh.bhxhmail.R.id.hide_headers).setVisible(false);
            }
        }


        /*
         * Set visibility of menu items related to the message list
         */

        // Hide both search menu items by default and enable one when appropriate
        menu.findItem(vn.bhxh.bhxhmail.R.id.search).setVisible(false);
        menu.findItem(vn.bhxh.bhxhmail.R.id.search_remote).setVisible(false);

        if (mDisplayMode == DisplayMode.MESSAGE_VIEW || mMessageListFragment == null ||
                !mMessageListFragment.isInitialized()) {
            menu.findItem(vn.bhxh.bhxhmail.R.id.check_mail).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.set_sort).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.select_all).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.send_messages).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.expunge).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.mark_all_as_read).setVisible(false);
            menu.findItem(vn.bhxh.bhxhmail.R.id.show_folder_list).setVisible(false);
        } else {
            menu.findItem(vn.bhxh.bhxhmail.R.id.set_sort).setVisible(true);
            menu.findItem(vn.bhxh.bhxhmail.R.id.select_all).setVisible(true);
            menu.findItem(vn.bhxh.bhxhmail.R.id.compose).setVisible(true);
            menu.findItem(vn.bhxh.bhxhmail.R.id.mark_all_as_read).setVisible(
                    mMessageListFragment.isMarkAllAsReadSupported());

            if (!mMessageListFragment.isSingleAccountMode()) {
                menu.findItem(vn.bhxh.bhxhmail.R.id.expunge).setVisible(false);
                menu.findItem(vn.bhxh.bhxhmail.R.id.send_messages).setVisible(false);
                menu.findItem(vn.bhxh.bhxhmail.R.id.show_folder_list).setVisible(false);
            } else {
                menu.findItem(vn.bhxh.bhxhmail.R.id.send_messages).setVisible(mMessageListFragment.isOutbox());
                menu.findItem(vn.bhxh.bhxhmail.R.id.expunge).setVisible(mMessageListFragment.isRemoteFolder() &&
                        mMessageListFragment.isAccountExpungeCapable());
                menu.findItem(vn.bhxh.bhxhmail.R.id.show_folder_list).setVisible(true);
            }

            menu.findItem(vn.bhxh.bhxhmail.R.id.check_mail).setVisible(mMessageListFragment.isCheckMailSupported());

            // If this is an explicit local search, show the option to search on the server
            if (!mMessageListFragment.isRemoteSearch() &&
                    mMessageListFragment.isRemoteSearchAllowed()) {
                menu.findItem(vn.bhxh.bhxhmail.R.id.search_remote).setVisible(true);
            } else if (!mMessageListFragment.isManualSearch()) {
                menu.findItem(vn.bhxh.bhxhmail.R.id.search).setVisible(true);
            }
        }
    }

    protected void onAccountUnavailable() {
        finish();
        // TODO inform user about account unavailability using Toast
        Accounts.listAccounts(this);
    }

    public void setActionBarTitle(String title) {
//        mActionBarTitle.setText(title);
    }

    public void setActionBarSubTitle(String subTitle) {
//        mActionBarSubTitle.setText(subTitle);
    }

    public void setActionBarUnread(int unread) {
//        if (unread == 0) {
//            mActionBarUnread.setVisibility(View.GONE);
//        } else {
//            mActionBarUnread.setVisibility(View.VISIBLE);
//            mActionBarUnread.setText(String.format("%d", unread));
//        }
    }

    @Override
    public void setMessageListTitle(String title) {
        setActionBarTitle(title);
    }

    @Override
    public void setMessageListSubTitle(String subTitle) {
        setActionBarSubTitle(subTitle);
    }

    @Override
    public void setUnreadCount(int unread) {
        setActionBarUnread(unread);
    }

    @Override
    public void setMessageListProgress(int progress) {
        setProgress(progress);
    }

    @Override
    public void openMessage(MessageReference messageReference) {
        Preferences prefs = Preferences.getPreferences(getApplicationContext());
        Account account = prefs.getAccount(messageReference.getAccountUuid());
        String folderName = messageReference.getFolderName();

        if (folderName.equals(account.getDraftsFolderName())) {
            MessageActions.actionEditDraft(this, messageReference);
        } else {
            mMessageViewContainer.removeView(mMessageViewPlaceHolder);

            if (mMessageListFragment != null) {
                mMessageListFragment.setActiveMessage(messageReference);
            }

            MessageViewFragment fragment = MessageViewFragment.newInstance(messageReference);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(vn.bhxh.bhxhmail.R.id.message_view_container, fragment);
            mMessageViewFragment = fragment;
            ft.commit();

            if (mDisplayMode != DisplayMode.SPLIT_VIEW) {
                showMessageView();
            }
        }
    }

    @Override
    public void onResendMessage(MessageReference messageReference) {
        MessageActions.actionEditDraft(this, messageReference);
    }

    @Override
    public void onForward(MessageReference messageReference) {
        onForward(messageReference, null);
    }

    @Override
    public void onForward(MessageReference messageReference, Parcelable decryptionResultForReply) {
        MessageActions.actionForward(this, messageReference, decryptionResultForReply);
    }

    @Override
    public void onReply(MessageReference messageReference) {
        onReply(messageReference, null);
    }

    @Override
    public void onReply(MessageReference messageReference, Parcelable decryptionResultForReply) {
        MessageActions.actionReply(this, messageReference, false, decryptionResultForReply);
    }

    @Override
    public void onReplyAll(MessageReference messageReference) {
        onReplyAll(messageReference, null);
    }

    @Override
    public void onReplyAll(MessageReference messageReference, Parcelable decryptionResultForReply) {
        MessageActions.actionReply(this, messageReference, true, decryptionResultForReply);
    }

    @Override
    public void onCompose(Account account) {
        MessageActions.actionCompose(this, account);
    }

    @Override
    public void showMoreFromSameSender(String senderAddress) {
        LocalSearch tmpSearch = new LocalSearch("From " + senderAddress);
        tmpSearch.addAccountUuids(mSearch.getAccountUuids());
        tmpSearch.and(SearchSpecification.SearchField.SENDER, senderAddress, SearchSpecification.Attribute.CONTAINS);

        MessageListFragment fragment = MessageListFragment.newInstance(tmpSearch, false, false);

        addMessageListFragment(fragment, true);
    }

    @Override
    public void onBackStackChanged() {
        findFragments();

        if (mDisplayMode == DisplayMode.SPLIT_VIEW) {
            showMessageViewPlaceHolder();
        }

        configureMenu(mMenu);
    }

    @Override
    public void onSwipeRightToLeft(MotionEvent e1, MotionEvent e2) {
        if (mMessageListFragment != null && mDisplayMode != DisplayMode.MESSAGE_VIEW) {
            mMessageListFragment.onSwipeRightToLeft(e1, e2);
        }
    }

    @Override
    public void onSwipeLeftToRight(MotionEvent e1, MotionEvent e2) {
        if (mMessageListFragment != null && mDisplayMode != DisplayMode.MESSAGE_VIEW) {
            mMessageListFragment.onSwipeLeftToRight(e1, e2);
        }
    }

    private final class StorageListenerImplementation implements StorageManager.StorageListener {
        @Override
        public void onUnmount(String providerId) {
            if (mAccount != null && providerId.equals(mAccount.getLocalStorageProviderId())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onAccountUnavailable();
                    }
                });
            }
        }

        @Override
        public void onMount(String providerId) {
            // no-op
        }
    }

    private void addMessageListFragment(MessageListFragment fragment, boolean addToBackStack) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.replace(vn.bhxh.bhxhmail.R.id.message_list_container, fragment);
        if (addToBackStack)
            ft.addToBackStack(null);

        mMessageListFragment = fragment;

        int transactionId = ft.commit();
        if (transactionId >= 0 && mFirstBackStackId < 0) {
            mFirstBackStackId = transactionId;
        }
    }

    @Override
    public boolean startSearch(Account account, String folderName) {
        // If this search was started from a MessageList of a single folder, pass along that folder info
        // so that we can enable remote search.
        if (account != null && folderName != null) {
            final Bundle appData = new Bundle();
            appData.putString(EXTRA_SEARCH_ACCOUNT, account.getUuid());
            appData.putString(EXTRA_SEARCH_FOLDER, folderName);
            startSearch(null, false, appData, false);
        } else {
            // TODO Handle the case where we're searching from within a search result.
            startSearch(null, false, null, false);
        }

        return true;
    }

    @Override
    public void showThread(Account account, String folderName, long threadRootId) {
        showMessageViewPlaceHolder();

        LocalSearch tmpSearch = new LocalSearch();
        tmpSearch.addAccountUuid(account.getUuid());
        tmpSearch.and(SearchSpecification.SearchField.THREAD_ID, String.valueOf(threadRootId), SearchSpecification.Attribute.EQUALS);

        MessageListFragment fragment = MessageListFragment.newInstance(tmpSearch, true, false);
        addMessageListFragment(fragment, true);
    }

    private void showMessageViewPlaceHolder() {
        removeMessageViewFragment();

        // Add placeholder view if necessary
        if (mMessageViewPlaceHolder.getParent() == null) {
            mMessageViewContainer.addView(mMessageViewPlaceHolder);
        }

        mMessageListFragment.setActiveMessage(null);
    }

    /**
     * Remove MessageViewFragment if necessary.
     */
    private void removeMessageViewFragment() {
        if (mMessageViewFragment != null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(mMessageViewFragment);
            mMessageViewFragment = null;
            ft.commit();

            showDefaultTitleView();
        }
    }

    private void removeMessageListFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove(mMessageListFragment);
        mMessageListFragment = null;
        ft.commit();
    }

    @Override
    public void remoteSearchStarted() {
        // Remove action button for remote search
        configureMenu(mMenu);
    }

    @Override
    public void goBack() {
        FragmentManager fragmentManager = getFragmentManager();
        if (mDisplayMode == DisplayMode.MESSAGE_VIEW) {
            showMessageList();
        } else if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else if (mMessageListFragment.isManualSearch()) {
            finish();
        } else if (!mSingleFolderMode) {
            onAccounts();
        } else {
            onShowFolderList();
        }
    }

    @Override
    public void enableActionBarProgress(boolean enable) {
        if (mMenuButtonCheckMail != null && mMenuButtonCheckMail.isVisible()) {
//            mActionBarProgress.setVisibility(ProgressBar.GONE);
            if (enable) {
                mMenuButtonCheckMail
                        .setActionView(mActionButtonIndeterminateProgress);
            } else {
                mMenuButtonCheckMail.setActionView(null);
            }
        } else {
            if (mMenuButtonCheckMail != null)
                mMenuButtonCheckMail.setActionView(null);
//            if (enable) {
//                mActionBarProgress.setVisibility(ProgressBar.VISIBLE);
//            } else {
//                mActionBarProgress.setVisibility(ProgressBar.GONE);
//            }
        }
    }

    @Override
    public void displayMessageSubject(String subject) {
        if (mDisplayMode == DisplayMode.MESSAGE_VIEW) {
//            mActionBarSubject.setText(subject);
        }
    }

    @Override
    public void showNextMessageOrReturn() {
        if (K9.messageViewReturnToList() || !showLogicalNextMessage()) {
            if (mDisplayMode == DisplayMode.SPLIT_VIEW) {
                showMessageViewPlaceHolder();
            } else {
                showMessageList();
            }
        }
    }

    /**
     * Shows the next message in the direction the user was displaying messages.
     *
     * @return {@code true}
     */
    private boolean showLogicalNextMessage() {
        boolean result = false;
        if (mLastDirection == NEXT) {
            result = showNextMessage();
        } else if (mLastDirection == PREVIOUS) {
            result = showPreviousMessage();
        }

        if (!result) {
            result = showNextMessage() || showPreviousMessage();
        }

        return result;
    }

    @Override
    public void setProgress(boolean enable) {
        setProgressBarIndeterminateVisibility(enable);
    }

    @Override
    public void messageHeaderViewAvailable(MessageHeader header) {
//        mActionBarSubject.setMessageHeader(header);
    }

    private boolean showNextMessage() {
        MessageReference ref = mMessageViewFragment.getMessageReference();
        if (ref != null) {
            if (mMessageListFragment.openNext(ref)) {
                mLastDirection = NEXT;
                return true;
            }
        }
        return false;
    }

    private boolean showPreviousMessage() {
        MessageReference ref = mMessageViewFragment.getMessageReference();
        if (ref != null) {
            if (mMessageListFragment.openPrevious(ref)) {
                mLastDirection = PREVIOUS;
                return true;
            }
        }
        return false;
    }

    private void showMessageList() {
        mMessageListWasDisplayed = true;
        mDisplayMode = DisplayMode.MESSAGE_LIST;
        mViewSwitcher.showFirstView();

        mMessageListFragment.setActiveMessage(null);

        showDefaultTitleView();
        configureMenu(mMenu);
    }

    private void showMessageView() {
        mDisplayMode = DisplayMode.MESSAGE_VIEW;

        if (!mMessageListWasDisplayed) {
            mViewSwitcher.setAnimateFirstView(false);
        }
        mViewSwitcher.showSecondView();

        showMessageTitleView();
        configureMenu(mMenu);
    }

    @Override
    public void updateMenu() {
        invalidateOptionsMenu();
    }

    @Override
    public void disableDeleteAction() {
        mMenu.findItem(vn.bhxh.bhxhmail.R.id.delete).setEnabled(false);
    }

    private void onToggleTheme() {
        if (K9.getK9MessageViewTheme() == K9.Theme.DARK) {
            K9.setK9MessageViewThemeSetting(K9.Theme.LIGHT);
        } else {
            K9.setK9MessageViewThemeSetting(K9.Theme.DARK);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Context appContext = getApplicationContext();
                Preferences prefs = Preferences.getPreferences(appContext);
                StorageEditor editor = prefs.getStorage().edit();
                K9.save(editor);
                editor.commit();
            }
        }).start();

        recreate();
    }

    private void showDefaultTitleView() {
//        mActionBarMessageView.setVisibility(View.GONE);
//        mActionBarMessageList.setVisibility(View.VISIBLE);

        if (mMessageListFragment != null) {
            mMessageListFragment.updateTitle();
        }

//        mActionBarSubject.setMessageHeader(null);
    }

    private void showMessageTitleView() {
//        mActionBarMessageList.setVisibility(View.GONE);
//        mActionBarMessageView.setVisibility(View.VISIBLE);

        if (mMessageViewFragment != null) {
            displayMessageSubject(null);
            mMessageViewFragment.updateTitle();
        }
    }

    @Override
    public void onSwitchComplete(int displayedChild) {
        if (displayedChild == 0) {
            removeMessageViewFragment();
        }
    }

    @Override
    public void startIntentSenderForResult(IntentSender intent, int requestCode, Intent fillInIntent,
                                           int flagsMask, int flagsValues, int extraFlags) throws SendIntentException {
        requestCode |= REQUEST_MASK_PENDING_INTENT;
        super.startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues, extraFlags);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode & REQUEST_MASK_PENDING_INTENT) == REQUEST_MASK_PENDING_INTENT) {
            requestCode ^= REQUEST_MASK_PENDING_INTENT;
            if (mMessageViewFragment != null) {
                mMessageViewFragment.onPendingIntentResult(requestCode, resultCode, data);
            }
        }

    }

    public void clickInbox(View view) {
        setTitleMail(getString(vn.bhxh.bhxhmail.R.string.c_inbox));
        onOpenFolder("Inbox");
    }

    public void clickSearch(View view) {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        mLayoutSearch.setVisibility(View.VISIBLE);
    }

    private void onOpenFolder(String folder) {
        mLayoutSearch.setVisibility(View.GONE);
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        LocalSearch search = new LocalSearch(folder);
        search.addAccountUuid(mAccount.getUuid());
        search.addAllowedFolder(folder);
        MessageList.actionDisplaySearch(this, search, false, false);
    }

    private void showMenu() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
        mLayoutAcc.removeAllViews();
        List<Account> accounts = Preferences.getPreferences(this).getAccounts();
        for (int i = 0; i < accounts.size(); i++) {
            final Account account = accounts.get(i);
            View view = View.inflate(this, vn.bhxh.bhxhmail.R.layout.item_acc, null);
            TextView name = (TextView) view.findViewById(vn.bhxh.bhxhmail.R.id.menu_acc_name);
            TextView email = (TextView) view.findViewById(vn.bhxh.bhxhmail.R.id.menu_acc_email);
            LinearLayout background = (LinearLayout) view.findViewById(vn.bhxh.bhxhmail.R.id.menu_layout_item_acc);
            name.setText(account.getName());
            email.setText(account.getEmail());
            if (i == mCurrentAcc) {
                background.setBackgroundResource(vn.bhxh.bhxhmail.R.color.gray212121);
                mCurrentName.setText(account.getName());
                mCurrentEmail.setText(account.getEmail());
            } else {
                background.setBackgroundColor(Color.TRANSPARENT);
            }
            final int position = i;
            background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentAcc = position;
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                    onOpenAccount(account);
                }
            });
            mLayoutAcc.addView(view);
        }

    }

    public void onExpand(View view) {
        if (mLayoutExpand.getVisibility() == View.VISIBLE) {
            mIconExpand.setImageResource(vn.bhxh.bhxhmail.R.drawable.ic_expand_down);
            mLayoutExpand.setVisibility(View.GONE);
        } else {
            mIconExpand.setImageResource(vn.bhxh.bhxhmail.R.drawable.ic_expand_up);
            mLayoutExpand.setVisibility(View.VISIBLE);
        }
    }

    private boolean onOpenAccount(BaseAccount account) {
        if (account instanceof SearchAccount) {
            SearchAccount searchAccount = (SearchAccount) account;
            MessageList.actionDisplaySearch(this, searchAccount.getRelatedSearch(), false, false);
        } else {
            Account realAccount = (Account) account;
            if (!realAccount.isEnabled()) {
//                onActivateAccount(realAccount);
                return false;
            } else if (!realAccount.isAvailable(this)) {
                String toastText = getString(vn.bhxh.bhxhmail.R.string.account_unavailable, account.getDescription());
                Toast toast = Toast.makeText(getApplication(), toastText, Toast.LENGTH_SHORT);
                toast.show();

                Log.i(K9.LOG_TAG, "refusing to open account that is not available");
                return false;
            }
            if (K9.FOLDER_NONE.equals(realAccount.getAutoExpandFolderName())) {
                FolderList.actionHandleAccount(this, realAccount);
            } else {
                LocalSearch search = new LocalSearch(realAccount.getAutoExpandFolderName());
                search.addAllowedFolder(realAccount.getAutoExpandFolderName());
                search.addAccountUuid(realAccount.getUuid());
                MessageList.actionDisplaySearch(this, search, false, true);
            }
        }
        return true;
    }

    private void searchEmail(){

        String query = mInputSearch.getText().toString();
        if(query == null || query.trim().isEmpty()){
            return;
        }
        Intent intent = new Intent(this, Search.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, query);
        Bundle bundle = new Bundle();
        List<Account> accounts = Preferences.getPreferences(this).getAccounts();
        bundle.putString(EXTRA_SEARCH_ACCOUNT,accounts.get(mCurrentAcc).getUuid());
        String folderName = (mMessageListFragment.getCurrentFolder() != null) ? mMessageListFragment.getCurrentFolder().name : null;
        bundle.putString(EXTRA_SEARCH_FOLDER,folderName);
        intent.putExtra(SearchManager.APP_DATA, bundle);
        startActivity(intent);

    }

    public void onNewAcc(View view){
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        startActivity(new Intent(MessageList.this, AccountSetupTypes.class));
    }

    public void onSentMail(View view) {
        setTitleMail(getString(vn.bhxh.bhxhmail.R.string.c_sended));
        onOpenFolder("Sent");
    }

    public void onSpamMail(View view) {
        setTitleMail(getString(vn.bhxh.bhxhmail.R.string.c_spam));
        if (mAccount != null && mAccount.getEmail().contains("@gmail")) {
            onOpenFolder("[Gmail]/Spam");
        } else {
            onOpenFolder("Spam");
        }
    }

    public void onTrashMail(View view) {
        setTitleMail(getString(vn.bhxh.bhxhmail.R.string.c_trash));
        onOpenFolder("Trash");
    }

    public void openAttachDownloads(View view) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), K9.FOLDER_ATTACH);
        Uri uri_path = Uri.fromFile(file);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri_path, "resource/folder");
        startActivity(intent);
    }

    public void logOut(View view) {
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        final List<Account> accounts = Preferences.getPreferences(this).getAccounts();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.c_logout_title));
        builder.setMessage(vn.bhxh.bhxhmail.R.string.c_logout_confirm);
        builder.setPositiveButton(R.string.c_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (accounts.get(mCurrentAcc) instanceof Account) {
                    Account realAccount = (Account) accounts.get(mCurrentAcc);
                    try {
                        realAccount.getLocalStore().delete();
                    } catch (Exception e) {
                    }
                    MessagingController.getInstance(getApplication())
                            .deleteAccount(realAccount);
                    Preferences.getPreferences(MessageList.this)
                            .deleteAccount(realAccount);
                    K9.setServicesEnabled(MessageList.this);
                    if (accounts.size() == 1) {
                        startActivity(new Intent(MessageList.this, AccountSetupTypes.class));
                        finish();
                    }else {
                        refresh();
                    }
                }
            }
        }).setNegativeButton(vn.bhxh.bhxhmail.R.string.c_back, null).show();
    }

    private void refresh() {
        List<Account> accounts = Preferences.getPreferences(this).getAccounts();
        onOpenAccount(accounts.get(0));
    }

    private void setTitleMail(String title) {
        mTitle.setText(title);
    }

    public void onCompose(View view) {
        mMessageListFragment.onCompose();
    }


    public void clickSetting(View view){
        final List<Account> accounts = Preferences.getPreferences(this).getAccounts();
        Intent intent = new Intent(this,SettingOptionsActivity.class);
        intent.putExtra(EditIdentity.EXTRA_ACCOUNT, accounts.get(mCurrentAcc).getUuid());
        intent.putExtra(EditIdentity.EXTRA_IDENTITY_INDEX, 0);
        mDrawerLayout.closeDrawer(Gravity.LEFT);
        startActivity(intent);
    }

    public void showCancel(boolean b) {
        if (mCommonCancel != null) {
            mCommonCancel.setVisibility(b ? View.VISIBLE : View.GONE);
            mMenuSetting.setVisibility(b ? View.GONE : View.VISIBLE);
        }
    }
}
