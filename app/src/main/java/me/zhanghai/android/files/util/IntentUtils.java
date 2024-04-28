package me.zhanghai.android.files.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.core.app.ShareCompat;

import java.util.ArrayList;
import java.util.List;

public class IntentUtils {

    private static Context application;
    private static ClassLoader appClassLoader;
    private static android.content.pm.PackageManager packageManager;



    public static <T extends Context> Intent createIntent(Context application, Class<?> cls) {
        return new Intent(application, cls);
    }

    public static Intent createSendTextIntent(Context application, CharSequence text, String htmlText) {
        return ShareCompat.IntentBuilder.from((Activity) application)
                .setType("text/plain")
                .setText(text)
                .setHtmlText(htmlText)
                .getIntent()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    }

    public static Intent createLaunchApp(String packageName) {
        return packageManager.getLaunchIntentForPackage(packageName);
    }

    public static Intent createPickImage(boolean allowMultiple) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        if (allowMultiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        return intent;
    }

   /* public static Intent createPickOrCaptureImageWithChooser(boolean allowPickMultiple, Uri captureOutputUri) {
        return createPickImage(allowPickMultiple).createChooser(captureOutputUri.createCaptureImage());
    }

    public static Intent createDocumentManagerViewDirectoryIntent(Uri uri) {
        Intent intent = uri.createViewIntent("vnd.android.document/directory");
        String packageName = DocumentsContractCompat.getDocumentsUiPackage();
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        return intent;
    }*/

    public static Intent createSyncSettings(String[] authorities, String[] accountTypes) {
        Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
        if (authorities != null && authorities.length > 0) {
            intent.putExtra(Settings.EXTRA_AUTHORITIES, authorities);
        }
        if (accountTypes != null && accountTypes.length > 0) {
            intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, accountTypes);
        }
        return intent;
    }

    public static Intent createViewAppInMarket(String packageName) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
    }

    public static Intent createViewLocation(float latitude, float longitude, String label) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + latitude + "," + longitude + "(" + Uri.encode(label) + ")"));
    }

    public static <T extends Parcelable> T getParcelableExtraSafe(Intent intent, String key) {
        intent.setExtrasClassLoader(appClassLoader);
        return intent.getParcelableExtra(key);
    }

    public static Parcelable[] getParcelableArrayExtraSafe(Intent intent, String key) {
        intent.setExtrasClassLoader(appClassLoader);
        return intent.getParcelableArrayExtra(key);
    }

    public static <T extends Parcelable> ArrayList<T> getParcelableArrayListExtraSafe(Intent intent, String key) {
        intent.setExtrasClassLoader(appClassLoader);
        return intent.getParcelableArrayListExtra(key);
    }

    public static Intent withChooser(Context context, CharSequence title, Intent... initialIntents) {
        Intent chooserIntent = Intent.createChooser(initialIntents[0], title);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, initialIntents);
        return chooserIntent;
    }

    public static Intent createEditIntent(Uri uri, String mimeType) {
        return new Intent(Intent.ACTION_EDIT)
                .setDataAndType(uri, mimeType)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    public static Intent createSendImageIntent(Context application, Uri uri, CharSequence text) {
        return createSendStreamIntent(application, new ArrayList<Uri>() {{ add(uri); }}, "image/*", text);
    }

    public static Intent createSendStreamIntent(Context application, List<Uri> uris, String mimeType, CharSequence text) {
        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from((Activity) application);
        builder.setType(mimeType);
        for (Uri uri : uris) {
            builder.addStream(uri);
        }
        if (text != null) {
            builder.setChooserTitle(text);
        }
        return builder.getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    }


    public static Intent createViewIntent(Uri uri) {
        return new Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    public static Intent createViewIntent(Uri uri, String mimeType) {
        return new Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, mimeType)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    public static Intent createInstallPackageIntent(Uri uri) {
        return new Intent(Intent.ACTION_INSTALL_PACKAGE)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    public static Intent createCaptureImage(Uri uri) {
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, uri);
    }
}

