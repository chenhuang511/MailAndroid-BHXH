package vn.bhxh.bhxhmail.ui.messageview;


import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import vn.bhxh.bhxhmail.K9;
import vn.bhxh.bhxhmail.R;
import vn.bhxh.bhxhmail.helper.SizeFormatter;
import vn.bhxh.bhxhmail.mailstore.AttachmentViewInfo;


public class AttachmentView extends FrameLayout implements OnClickListener, OnLongClickListener {
    private AttachmentViewInfo attachment;
    private AttachmentViewCallback callback;

    private Button viewButton;
    private Button downloadButton;
    private RelativeLayout layoutAttach;
    private AlertDialog alertDialog;


    public AttachmentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttachmentView(Context context) {
        super(context);
    }

    public AttachmentViewInfo getAttachment() {
        return attachment;
    }

    public void enableButtons() {
        viewButton.setEnabled(true);
        downloadButton.setEnabled(true);
    }

    public void disableButtons() {
        viewButton.setEnabled(false);
        downloadButton.setEnabled(false);
    }

    public void setAttachment(AttachmentViewInfo attachment) {
        this.attachment = attachment;

        displayAttachmentInformation();
    }

    private void displayAttachmentInformation() {
        viewButton = (Button) findViewById(R.id.view);
        downloadButton = (Button) findViewById(R.id.download);
        layoutAttach = (RelativeLayout) findViewById(R.id.layout_attach);

        if (attachment.size > K9.MAX_ATTACHMENT_DOWNLOAD_SIZE) {
            viewButton.setVisibility(View.GONE);
            downloadButton.setVisibility(View.GONE);
        }

        viewButton.setOnClickListener(this);
        downloadButton.setOnClickListener(this);
        layoutAttach.setOnClickListener(this);
        downloadButton.setOnLongClickListener(this);

        TextView attachmentName = (TextView) findViewById(R.id.attachment_name);
        attachmentName.setText(attachment.displayName);

        setAttachmentSize(attachment.size);

        refreshThumbnail();
    }

    private void setAttachmentSize(long size) {
        TextView attachmentSize = (TextView) findViewById(R.id.attachment_info);
        if (size == AttachmentViewInfo.UNKNOWN_SIZE) {
            attachmentSize.setText("");
        } else {
            String text = SizeFormatter.formatSize(getContext(), size);
            attachmentSize.setText(text);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.view: {
                alertDialog.dismiss();
                onViewButtonClick();
                break;
            }
            case R.id.download: {
                alertDialog.dismiss();
                onSaveButtonClick();
                break;
            }
            case R.id.layout_attach: {
                showAction();
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (view.getId() == R.id.download) {
//            onSaveButtonLongClick();
            return true;
        }

        return false;
    }

    private void onViewButtonClick() {
        callback.onViewAttachment(attachment);
    }

    private void onSaveButtonClick() {
        callback.onSaveAttachment(attachment);
    }

    private void onSaveButtonLongClick() {
        callback.onSaveAttachmentToUserProvidedDirectory(attachment);
    }

    public void setCallback(AttachmentViewCallback callback) {
        this.callback = callback;
    }

    public void refreshThumbnail() {
        ImageView thumbnailView = (ImageView) findViewById(R.id.attachment_icon);
        Glide.with(getContext())
                .load(attachment.uri)
                .placeholder(R.drawable.attached_image_placeholder)
                .centerCrop()
                .into(thumbnailView);
    }

    public void showAction() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = inflate(getContext(), R.layout.layout_attach_action, null);
        alertDialog = builder.create();
        alertDialog.setView(view);
        view.findViewById(R.id.view).setOnClickListener(this);
        view.findViewById(R.id.download).setOnClickListener(this);
        view.findViewById(R.id.attach_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
