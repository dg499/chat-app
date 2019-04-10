package com.android.chatapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

class PermissionHandler {
    public static final int PERMISSION_REQUEST_CODE = 0x009;
    public static final int CONTACTS_PERMISSION_REQUEST_CODE = 0x009;
    public static final int SELECT_ADD_NEW_CONTACT = 0x010;

    public static boolean checkPermission(Activity activity, String permission) {
        if (AppHelper.isAndroid6()) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static void requestPermission(Activity mActivity, String permission) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
            String title = null;
            String Message = null;
            switch (permission) {

                case Manifest.permission.RECORD_AUDIO:
                    title = mActivity.getString(R.string.audio_permission);
                    Message = mActivity.getString(R.string.record_audio_permission_message);
                    break;
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    title = mActivity.getString(R.string.location_permission);
                    Message = mActivity.getString(R.string.location_permission_message);
                    break;
                case Manifest.permission.MODIFY_AUDIO_SETTINGS:
                    title = mActivity.getString(R.string.camera_permission);
                    Message = mActivity.getString(R.string.settings_audio_permission_message);
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    title = mActivity.getString(R.string.storage_permission);
                    Message = mActivity.getString(R.string.write_storage_permission_message);
                    break;
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                    title = mActivity.getString(R.string.storage_permission);
                    Message = mActivity.getString(R.string.read_storage_permission_message);
                    break;
                case Manifest.permission.READ_CONTACTS:
                    title = mActivity.getString(R.string.contacts_permission);
                    Message = mActivity.getString(R.string.read_contacts_permission_message);
                    break;
                case Manifest.permission.WRITE_CONTACTS:
                    title = mActivity.getString(R.string.contacts_permission);
                    Message = mActivity.getString(R.string.write_contacts_permission_message);
                    break;

                case Manifest.permission.RECEIVE_SMS:
                    title = mActivity.getString(R.string.receive_sms_permission);
                    Message = mActivity.getString(R.string.receive_sms_permission_message);
                    break;

                case Manifest.permission.READ_SMS:
                    title = mActivity.getString(R.string.read_sms_permission);
                    Message = mActivity.getString(R.string.read_sms_permission_message);
                    break;
                case Manifest.permission.CALL_PHONE:
                    title = mActivity.getString(R.string.call_phone_permission);
                    Message = mActivity.getString(R.string.call_phone_permission_message);
                    break;
                case Manifest.permission.GET_ACCOUNTS:
                    title = mActivity.getString(R.string.get_accounts_permission);
                    Message = mActivity.getString(R.string.get_accounts_permission_message);
                    break;

            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(title);
            builder.setMessage(Message);
            builder.setPositiveButton(mActivity.getString(R.string.yes), (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                intent.setData(uri);
                if (permission.equals(Manifest.permission.READ_CONTACTS)) {
                    mActivity.startActivityForResult(intent, CONTACTS_PERMISSION_REQUEST_CODE);
                } else {
                    mActivity.startActivityForResult(intent, PERMISSION_REQUEST_CODE);
                }
            });
            builder.setNegativeButton(R.string.no_thanks, (dialog, which) -> {
                if (permission.equals(Manifest.permission.READ_CONTACTS)) {

                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else {
            if (permission.equals(Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(mActivity, new String[]{permission}, CONTACTS_PERMISSION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(mActivity, new String[]{permission}, PERMISSION_REQUEST_CODE);
            }
        }
    }


}
