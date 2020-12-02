package com.brc.acctrl.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.format.DateFormat;

import com.brc.acctrl.events.RefreshEvents;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Locale;

public class CrashHandler implements UncaughtExceptionHandler {
	private UncaughtExceptionHandler m_defaultHandler;
	private Context m_appContext;

	public CrashHandler(Context appContext,
			UncaughtExceptionHandler defaultHandler) {
		m_defaultHandler = defaultHandler;
		m_appContext = appContext;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		try {
			StringBuilder reportBuilder = new StringBuilder();
			reportBuilder.append("App Crashed on : ")
					.append(new Date().toString()).append('\n').append('\n');

			reportBuilder.append("Informations :").append('\n');

			addSystemInformation(reportBuilder);

			reportBuilder.append('\n').append('\n');
			reportBuilder.append("Stack:\n");

			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			ex.printStackTrace(printWriter);
			reportBuilder.append(stringWriter.toString());
			printWriter.close();

			reportBuilder.append('\n');
			reportBuilder.append("****  End of Crash Report ***\n");

			dumpDataToFile(reportBuilder.toString());
		} catch (Exception exIgnore) {
			exIgnore.printStackTrace();
		}

		// 这里不执行会弹出一个错误对话框，但是很快又结束了，不影响后续流程
//		m_defaultHandler.uncaughtException(thread, ex);

		EventBus.getDefault().post(new RefreshEvents.RestartAPPEvent());
	}

	@SuppressLint("NewApi")
	private void addSystemInformation(StringBuilder messageBuilder) {
		messageBuilder.append("Locale: ").append(Locale.getDefault())
				.append('\n');
		try {
			PackageManager pm = m_appContext.getPackageManager();
			PackageInfo pi;
			pi = pm.getPackageInfo(m_appContext.getPackageName(), 0);
			messageBuilder.append("Version: ").append(pi.versionName)
					.append('\n');
			messageBuilder.append("Package: ").append(pi.packageName)
					.append('\n');
		} catch (Exception ex) {
			messageBuilder.append("Fail to get Version information.");
		}
		messageBuilder.append("Phone Model: ").append(android.os.Build.MODEL)
				.append('\n');
		messageBuilder.append("Android Version: ")
				.append(android.os.Build.VERSION.RELEASE).append('\n');
		messageBuilder.append("Board: ").append(android.os.Build.BOARD)
				.append('\n');
		messageBuilder.append("Brand: ").append(android.os.Build.BRAND)
				.append('\n');
		messageBuilder.append("Device: ").append(android.os.Build.DEVICE)
				.append('\n');
		messageBuilder.append("Host: ").append(android.os.Build.HOST)
				.append('\n');
		messageBuilder.append("ID: ").append(android.os.Build.ID).append('\n');
		messageBuilder.append("Model: ").append(android.os.Build.MODEL)
				.append('\n');
		messageBuilder.append("Product: ").append(android.os.Build.PRODUCT)
				.append('\n');
		messageBuilder.append("Type: ").append(android.os.Build.TYPE)
				.append('\n');

		try {
			ActivityManager activityManager = (ActivityManager) m_appContext
					.getSystemService(Context.ACTIVITY_SERVICE);
			MemoryInfo memoryInfo = new MemoryInfo();
			activityManager.getMemoryInfo(memoryInfo);

			messageBuilder.append("Total Internal memory: ")
					.append(memoryInfo.totalMem / 1024).append(" KB \n");

			messageBuilder.append("Available Internal memory: ")
					.append(memoryInfo.availMem / 1024).append(" KB \n");
		} catch (Exception ex) {
			messageBuilder.append("Fail to get memory usage information.");
		}
	}

	private void dumpDataToFile(String errorMessage) {
		try {
			File file = new File(m_appContext.getExternalFilesDir(null),
					DateFormat.format("/CrushDump_yyyy-MM-dd_hh-mm-ss",
							new Date()) + ".log");
			file.createNewFile();

			FileWriter fileWriter = new FileWriter(file, false);
			fileWriter.write(errorMessage);
			fileWriter.flush();
			fileWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
