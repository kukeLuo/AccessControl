package com.brc.acctrl.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class InstallApkUtils {

	public static void install(Context context, String filePath) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			startInstallN(context, filePath);
		} else {
			startInstall(context, filePath);
		}
		/*File file = new File(filePath);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		// 仅需改变这一行
		FileProvider7.setIntentDataAndType(context,
				intent, "application/vnd.android.package-archive", file, true);
		context.startActivity(intent);*/
	}

	/**
	 *android1.x-6.x
	 *@param path 文件的路径
	 */
	public static void startInstall(Context context, String path) {
		Intent install = new Intent(Intent.ACTION_VIEW);
		install.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
		install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(install);
	}

	/**
	 * android7.x
	 * @param path 文件路径
	 */
	public static void startInstallN(Context context, String path) {
		//参数1 上下文, 参数2 在AndroidManifest中的android:authorities值, 参数3  共享的文件
		Uri apkUri = FileProvider.getUriForFile(context, "com.brc.acctrl.fileProvider", new File(path));
		Intent install = new Intent(Intent.ACTION_VIEW);
		//由于没有在Activity环境下启动Activity,设置下面的标签
		install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//添加这一句表示对目标应用临时授权该Uri所代表的文件
		install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		install.setDataAndType(apkUri, "application/vnd.android.package-archive");
		context.startActivity(install);
	}


	/**
	 * 执行具体的静默安装逻辑，需要手机ROOT。
	 *安装成功返回true，安装失败返回false。
	 */
	public boolean install(String apkPath) {

		Boolean result =false;
		DataOutputStream dataOutputStream =null;
		BufferedReader errorStream =null;
		try{

			// 申请su权限
			Process process = Runtime.getRuntime().exec("su");
			dataOutputStream =new DataOutputStream(process.getOutputStream());
			// 执行pm install命令
			String command ="pm install -r "+ apkPath +"\n";
			dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
			dataOutputStream.flush();
			dataOutputStream.writeBytes("exit\n");
			dataOutputStream.flush();
			process.waitFor();
			errorStream =new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String msg ="";
			String line;
			// 读取命令的执行结果
			while((line = errorStream.readLine()) !=null) {
				msg += line;
			}
			// 如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
			if(!msg.contains("Failure")) {
				result =true;
			}
		}catch(Exception e) {
			Log.e("TAG",e.getMessage(),e);
		}finally{
			try{
				if(dataOutputStream !=null) {
					dataOutputStream.close();
				}
				if(errorStream !=null) {
					errorStream.close();
				}
			}catch(IOException e) {
				Log.e("TAG",e.getMessage(),e);
			}
		}
		return result;

	}

	/**
	 * 静默安装 并启动
	 *
	 * @return
	 */
	public static boolean silentInstall(String tempPath) {
		File file = new File(tempPath);
		boolean result = false;
		Process process = null;
		OutputStream out = null;
		if (file.exists()) {
			System.out.println(file.getPath() + "==");
			try {
				process = Runtime.getRuntime().exec("su");
				out = process.getOutputStream();
				DataOutputStream dataOutputStream = new DataOutputStream(out);
				// 获取文件所有权限
				dataOutputStream.writeBytes("chmod 777 " + file.getPath()
						+ "\n");
				// 进行静默安装命令
				dataOutputStream
						.writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r "
								+ file.getPath());
				dataOutputStream.flush();
				// 关闭流操作
				dataOutputStream.close();
				out.close();
				int value = process.waitFor();
				Log.e("安装","安装了成功");
				// 代表成功
				if (value == 0) {
					result = true;
					// 失败
				} else if (value == 1) {
					result = false;
					// 未知情况
				} else {
					result = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!result) {
				result = true;
			}
		}
		return result;
	}



}
