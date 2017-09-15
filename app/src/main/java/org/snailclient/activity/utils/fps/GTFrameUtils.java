package org.snailclient.activity.utils.fps;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

/**
 * Created by wubo1 on 2016/7/11.
 */

public class GTFrameUtils {

	public static final String TAG = "Fps";

	private static int pid = 0;
	public static boolean hasSu = false;

	public static boolean isHasSu() {
		Log.d(TAG, "isHasSu state is:" + hasSu);
		return hasSu;
	}

	public static void setHasSu(boolean hasSu) {
		GTFrameUtils.hasSu = hasSu;
		Log.d(TAG, "setHasSu state is:" + GTFrameUtils.hasSu);
	}

	public static void setPid(Context context) {
		setHasSu(false);
		try {
			ProcessBuilder execBuilder = null;
			if (pid == 0) {
				execBuilder = new ProcessBuilder("su", "-c", "ps");
				execBuilder.redirectErrorStream(true);
				Process exec = null;
				exec = execBuilder.start();
				InputStream is = exec.getInputStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));

				String line = "";
				while ((line = reader.readLine()) != null) {
					if (line.contains("surfaceflinger")) {
						String regEx = "\\s[0-9][0-9]*\\s";
						Pattern pat = Pattern.compile(regEx);
						Matcher mat = pat.matcher(line);
						if (mat.find()) {
							String temp = mat.group();
							temp = temp.replaceAll("\\s", "");
							pid = Integer.parseInt(temp);
						}
						break;
					}
				}
			}

			if (pid == 0) {
				if (ProcessUtils.getProcessPID("system_server") != -1) {
					pid = ProcessUtils.getProcessPID("system_server");
				} else {
					pid = ProcessUtils.getProcessPID("system");
				}

			}
			setHasSu(true);
		} catch (Exception e) {
			setHasSu(false);
		}

		Log.d("pid: ", String.valueOf(pid));
	}
}
