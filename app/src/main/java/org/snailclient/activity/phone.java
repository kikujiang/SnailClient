package org.snailclient.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
/**
 * 
 * 类的描述：手机信息类
 * 
 * @author zhll
 * 
 * @Time 2015-07-08
 * 
 * 
 */
public class phone extends Service {
	private long totalCpu2;
	private long idleCpu2;
	private long idleCpu = 0;
	private long totalCpu = 0;
	public String model;
	public String mac;

	public phone(String mac) {
		if(mac!=null){
		this.mac = mac;
		model = android.os.Build.MODEL;
		model = model.replace(" ", "");
		}
	}

	public static String getsd() {
		File sd = Environment.getExternalStorageDirectory();
		String path = sd.getAbsolutePath();
		if (path.equals("/storage/emulated/0")) {
			path = "/mnt/sdcard";
		}
		return path;
	}

	public String gettotalCpu() throws IOException {
		String totalCpuRatio = "";
		DecimalFormat fomart = new DecimalFormat();
		// fomart.setGroupingUsed(false);
		fomart.setMaximumFractionDigits(2);
		fomart.setMinimumFractionDigits(2);
		readCpuStat();
		totalCpuRatio = fomart
				.format(100 * ((double) ((totalCpu - idleCpu) - (totalCpu2 - idleCpu2)) / (double) (totalCpu - totalCpu2)));
		idleCpu2 = idleCpu;
		totalCpu2 = totalCpu;
		return totalCpuRatio;
	}

	/**
	 * read the status of CPU.
	 * 
	 * @throws FileNotFoundException
	 */
	public void readCpuStat() {
		try {
			// monitor total and idle cpu stat of certain process
			RandomAccessFile cpuInfo = new RandomAccessFile("/proc/stat", "r");
			String[] toks = cpuInfo.readLine().split("\\s+");
			idleCpu = Long.parseLong(toks[4]);
			totalCpu = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
					+ Long.parseLong(toks[3]) + Long.parseLong(toks[4])
					+ Long.parseLong(toks[6]) + Long.parseLong(toks[5])
					+ Long.parseLong(toks[7]);
			cpuInfo.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	public String rungetPid(String file, String APKName) throws IOException {
		String line = null;
		Process p = Runtime.getRuntime().exec(file);
		BufferedReader bf = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		String pl = null;
		while ((pl = bf.readLine()) != null) {
			if (pl.contains(APKName)) {
				line = pl;
				Log.d("zlulan", "pid" + line);
				bf.close();
				break;
			}
		}
		return line;
	}

	public String getPid(String APKName) throws IOException {
		String cmd = "ps";
		String[] pid = null;
		String line = null;
		while (true) {
			line = rungetPid(cmd, APKName);
			if (line != null) {
				pid = line.split(" {1,}");
				return pid[1];
			} else
				return null;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
