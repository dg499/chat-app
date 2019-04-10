package com.android.chatapp;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class FilesManager {

    private static File getMainPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), mContext.getApplicationContext().getString(R.string.app_name));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * @return String path
     */
    public static String getFileRecordPath(Context mContext) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HHmmss", Locale.getDefault()).format(new Date());
        return String.format(getAudiosSentPathString(mContext) + File.separator + "record-%s", timeStamp + ".mp3");
    }

    /**
     * @param mContext
     * @return sent Audio path string
     */
    private static String getAudiosSentPathString(Context mContext) {
        return String.valueOf(getAudiosSentPath(mContext));
    }

    private static File getAudiosSentPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getAudiosPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.directory_sent));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }


    private static File getAudiosPath(Context mContext) {

        // External sdcard location
        File mediaStorageDir = new File(getMainPath(mContext), mContext.getApplicationContext().getString(R.string.app_name) + " " + mContext.getApplicationContext().getString(R.string.audios_directory));
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                AppHelper.LogCat("Oops! Failed create " + mContext.getApplicationContext().getString(R.string.app_name) + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }


    public static boolean isFileAudiosSentExists(Context mContext, String Id) {
        File file = new File(getAudiosSentPathString(mContext), Id);
        return file.exists();
    }


    public static boolean isFileRecordExists(String Path) {
        File file = new File(Path);
        return file.exists();
    }

    public static File getFileRecord(String Path) {
        return new File(Path);
    }


    public static String getDataCached(String Identifier) {
        return String.format("Data-%s", Identifier);
    }

    public static String getProfileImage(String Identifier) {
        return String.format("IMG-Profile-%s", Identifier + ".jpg");
    }

    public static String getImage(String Identifier) {
        return String.format("IMG-%s", Identifier + ".jpg");
    }

    public static String getAudio(String Identifier) {
        return String.format("AUD-%s", Identifier + ".mp3");
    }

    public static String getDocument(String Identifier) {
        return String.format("DOC-%s", Identifier + ".pdf");
    }

    public static String getVideo(String Identifier) {
        return String.format("VID-%s", Identifier + ".mp4");
    }


    /**
     * method to get mime type of files
     *
     * @param url
     * @return
     */
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


    public static String getFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {


        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileInputStream var2 = new FileInputStream(src);
        FileOutputStream var3 = new FileOutputStream(dst);
        byte[] var4 = new byte[1024];

        int var5;
        while ((var5 = var2.read(var4)) > 0) {
            var3.write(var4, 0, var5);
        }
        var2.close();
        var3.close();
    }


}
