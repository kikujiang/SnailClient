package com.monitor.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.util.Log;

/**
 * 
 * 类的描述：root权限下读取帧数
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class Fps {
	boolean isf = false;
	// long[] A=new long[127];
	ArrayList<Float> B = new ArrayList<Float>();
	ArrayList<Float> J = new ArrayList<Float>();
	ArrayList<Float> J1 = new ArrayList<Float>();
	ArrayList<Float> B1 = new ArrayList<Float>();
	// long[] C=new long[127];
	float refresh_period = 0;
	double pending_fence_timestamp = (1 << 63) - 1;
	double nanoseconds_per_second = (long) 1E9;

	public void run() {
		Process pro = null;
		;
		try {
			pro = Runtime.getRuntime().exec("su");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		BufferedReader bf = new BufferedReader(new InputStreamReader(
				pro.getInputStream()));
		DataOutputStream os = null;
		os = new DataOutputStream(pro.getOutputStream());
		try {
			os.writeBytes("export LD_LIBRARY_PATH=/vendor/lib:/system/lib"
					+ "\n");
			os.flush();
			os.writeBytes("dumpsys SurfaceFlinger --latency SurfaceView\n");
			os.flush();
			os.writeBytes("exit" + "\n");
			String line = null;
			int i = 0;
			while ((line = bf.readLine()) != null) {
				if (line.length() > 1) {
					if (line.matches("[0-9]+")) {
						isf = true;
					}
					String[] lines = line.split("\\s+");
					if (isf) {
						if (lines.length < 2) {
							refresh_period = (float) (Double
									.parseDouble(lines[0]) / nanoseconds_per_second);
							// System.out.print("refresh_period为"+refresh_period);
						} else {
							if (Long.parseLong(lines[1]) == pending_fence_timestamp) {
								continue;
							}
							B.add((float) (Long.parseLong(lines[1]) / nanoseconds_per_second));
						}
					}
				}
			}
			bf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public float _CalculateResults() {
		run();
		isf = false;
		int jank = 0;
		int frame_count = 0;
		float fps = 100;
		frame_count = B.size();
		B1 = _GetNormalizedDeltas(B, true);
		if (B1.size() <= B.size() && B1.size() > 0) {
			frame_count = B1.size();
		}
		_GetNormalizedDeltas(B1, false);
		if (J.size() > 0) {
			jank = J.size();

		}
		if (B.size() > 0) {
			float time = (B.get(B.size() - 1)) - (B.get(0));
			fps = frame_count / time;
			jank = (int) (jank / time);
		}
		J.clear();
		B.clear();
		B1.clear();
		J1.clear();
		return fps;
	}

	public ArrayList<Float> _GetNormalizedDeltas(ArrayList<Float> B,
			Boolean isjank) {
		ArrayList<Float> B1 = new ArrayList<Float>();
		for (int i = 1; i < B.size(); i++) {
			float time = B.get(i) - B.get(i - 1);
			float tmp = (float) (time / refresh_period);
			if (tmp < 0) {
				tmp = (float) (time / refresh_period);
			}
			if (isjank) {
				if (tmp >= 0.5) {
					B1.add(tmp);
				}
			} else {
				if (tmp > 1 && tmp < 20)
					J.add(tmp);
			}
		}
		return B1;
	}

	public float totalfps() {
		float total = 0;
		for (int i = 0; i < 5; i++) {
			float tmp = _CalculateResults();
			try {
				Thread.sleep(200);
				total += tmp;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		total = total / 5;
		Log.d("zlulan", "fps为" + total + "\n");
		return total;
	}
}
