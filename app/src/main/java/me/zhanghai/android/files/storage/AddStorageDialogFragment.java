package me.zhanghai.android.files.storage;

import static me.zhanghai.android.files.util.IntentUtils.createIntent;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import me.zhanghai.android.files.R;
import me.zhanghai.android.files.compat.DocumentsContractCompat;
import me.zhanghai.android.files.util.IntentUtils;

public class AddStorageDialogFragment extends AppCompatDialogFragment {
    private static final String DOCUMENT_ID_PRIMARY = "primary";
    private static final String DOCUMENT_ID_PRIMARY_ANDROID_DATA = "primary:Android/data";
    private static final String DOCUMENT_ID_PRIMARY_ANDROID_OBB = "primary:Android/obb";

    private static final Uri TREE_URI_PRIMARY_ANDROID = DocumentsContract.buildTreeDocumentUri(
            DocumentsContractCompat.EXTERNAL_STORAGE_PROVIDER_AUTHORITY, DOCUMENT_ID_PRIMARY
    );
    public static final Uri DOCUMENT_URI_ANDROID_DATA = DocumentsContract.buildDocumentUriUsingTree(
            TREE_URI_PRIMARY_ANDROID, DOCUMENT_ID_PRIMARY_ANDROID_DATA
    );

    public static final Uri DOCUMENT_URI_ANDROID_OBB = DocumentsContract.buildDocumentUriUsingTree(
            TREE_URI_PRIMARY_ANDROID, DOCUMENT_ID_PRIMARY_ANDROID_OBB
    );

    private static final int[] STORAGE_TITLES = {
            R.string.storage_add_storage_android_data,
            R.string.storage_add_storage_android_obb,
            R.string.storage_add_storage_document_tree,
            R.string.storage_add_storage_ftp_server,
            R.string.storage_add_storage_sftp_server,
            R.string.storage_add_storage_smb_server,
            R.string.storage_add_storage_webdav_server
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.storage_add_storage_title);

        String[] items = new String[STORAGE_TITLES.length];
        for (int i = 0; i < STORAGE_TITLES.length; i++) {
            items[i] = getString(STORAGE_TITLES[i]);
        }

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivitySafe(which);
                requireActivity().finish();
            }
        });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        requireActivity().finish();
    }


    private Bundle putArgs(Bundle args, int titleResId, String documentUri) {
        args.putInt("titleResId", titleResId);
        args.putString("documentUri", documentUri);
        return args;
    }

    private Intent createDocumentManagerShortcutIntent(int titleResId, String documentUri) {
        Bundle args = new Bundle();
        putArgs(args, titleResId, documentUri);
        return createIntent(requireContext(), AddDocumentManagerShortcutActivity.class).putExtras(args);
    }

    private Intent createFtpServerIntent() {
        return createIntent(requireContext(), EditFtpServerActivity.class).putExtras(new Bundle());
    }

    private Intent createSftpServerIntent() {
        return createIntent(requireContext(), EditSftpServerActivity.class).putExtras(new Bundle());
    }

    private Intent createSmbServerIntent() {
        return createIntent(requireContext(), AddLanSmbServerActivity.class);
    }

    private Intent createWebDavServerIntent() {
        return createIntent(requireContext(), EditWebDavServerActivity.class).putExtras(new Bundle());
    }

    private void startActivitySafe(int which) {
        if (which == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().startActivity(createDocumentManagerShortcutIntent(
                    R.string.storage_add_storage_android_data,
                    DOCUMENT_URI_ANDROID_DATA.toString()));
        } else if (which == 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().startActivity(createDocumentManagerShortcutIntent(
                    R.string.storage_add_storage_android_obb,
                    DOCUMENT_URI_ANDROID_OBB.toString()));
        } else if (which == 2) {
            startActivity(createIntent(requireContext(), AddDocumentTreeActivity.class));
        } else if (which == 3) {
            startActivity(createFtpServerIntent());
        } else if (which == 4) {
            startActivity(createSftpServerIntent());
        } else if (which == 5) {
            startActivity(createSmbServerIntent());
        } else if (which == 6) {
            startActivity(createWebDavServerIntent());
        }
    }
}


