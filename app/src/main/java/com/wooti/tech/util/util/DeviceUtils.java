package com.wooti.tech.util.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;


public class DeviceUtils {

	private static final String TAG = "DeviceUtils";


	public static void setClipBoardLink(Context context , String link){
		ClipboardManager clipboardManager = (ClipboardManager)context.getSystemService(context.CLIPBOARD_SERVICE);
		ClipData clipData = ClipData.newPlainText("label", link);
		clipboardManager.setPrimaryClip(clipData);
		Toast.makeText(context, "token 이 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show();
	}
}
