package cn.skywm.mihu.files;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @(#)FileUtil.java
 * 文件工具类,文件读取写入
 * Created by april on 2021/11/26.
 *
 * @author april
 * @Version: 1.0
 */
public class FileUtil {
	private static final Logger logger = Logger.getLogger(FileUtil.class);
	public List<String> list=new ArrayList<String>();

	/**
	 *  getFileEncode 获取当前文件编码
	 *  <p>
	 *     传如文件名称,加载文件并获取该文件所属的编码
	 *  </p>
	 *
	 * @param fileName   文件名称
	 * @return String 类型 当前文件编码
	 */
	public static String getFileEncode(String fileName) {
		String charSet = null;
		FileInputStream fileIS;
		try { 
			fileIS = new FileInputStream(fileName);
			byte[] bf = new byte[3];
			fileIS.read(bf);
			fileIS.close();
			if (bf[0] == -17 && bf[1] == -69 && bf[2] == -65) {
				charSet = "UTF-8";
			} else if ((bf[0] == -1 && bf[1] == -2)) {
				charSet = "Unicode";
			} else if ((bf[0] == -2 && bf[1] == -1)) {
				charSet = "Unicode big endian";
			} else {
				charSet = "ANSI";
			}
		} catch (Exception e2) {
			logger.error("", e2);
		}

		return charSet;
	}



	/**
	 *  readFileByLines 按行读取文件
	 *  <p>
	 *     读取文件内容返回集合,根据参数判断第一行是否进行读取
	 *  </p>
	 *
	 * @param path  文件路径
	 * @param charsetName 读取编码
	 * @param flag  是否跳过第一行 1 跳过  0不跳过
	 * @return List<String> 类型 读取文件的集合
	 */
	public List<String> readFileByLines(String path, String charsetName,int flag) {
		List<String> lineList = new ArrayList<String>();
		BufferedReader br = null;
		InputStreamReader isReader = null;
		try {
			isReader = new InputStreamReader(new FileInputStream(path), charsetName);
			br = new BufferedReader(isReader);
			String tempString = null;
			// int line = 1;
			if(flag==1){
				br.readLine(); //跳过第一行
			}

			while ((tempString = br.readLine()) != null && tempString!="") {
				lineList.add(tempString);
			}
		} catch (UnsupportedEncodingException e1) {
			logger.error("解析文件编码异常", e1);
		} catch (FileNotFoundException e2) {
			logger.error("文件没有找到异常", e2);
		} catch (IOException e3) {
			logger.error("IO操作异常", e3);
		} finally {
			if (br != null) {
				try {
					br.close();
					br = null;
				} catch (IOException e) {
				}
			}
			if (isReader != null) {
				try {
					isReader.close();
					isReader = null;
				} catch (IOException e) {
				}
			}
		}
		return lineList;
	}

	/**
	 *  copyFile 复制文件
	 *  <p>
	 *     读取文件内容返回集合,根据参数判断第一行是否进行读取
	 *  </p>
	 *
	 * @param sourceFile  原文件
	 * @param targetFile 新文件
	 * @return boolean 类型 是否成功  true: 成功  flase 失败
	 */
	public static boolean copyFile(File sourceFile, File targetFile) throws IOException {
		boolean flag = false;
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {

			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}

			outBuff.flush();
			flag = true;
		} finally {

			if (inBuff != null) {
				inBuff.close();
			}
			if (outBuff != null) {
				outBuff.close();
			}
		}
		return flag;
	}


	/**
	 *  writeLineList 写入文件到指定目录
	 *  <p>
	 *     写入list集合到指定目录下的文件
	 *  </p>
	 *
	 * @param fileName  路径+文件名称
	 * @param lineList 内容集合
	 * @param charsetName  编码
	 * @return boolean 类型 是否成功  true: 成功  flase 失败
	 */
	public boolean writeLineList(String fileName, List<String> lineList, String charsetName) {
		boolean flag = false;
		BufferedWriter bw = null;
		OutputStreamWriter osWeiter = null;
		File outputFile = new File(fileName);
		if (outputFile.exists()) {
			outputFile.delete();
		}else{
			String path = fileName.substring(0, fileName.lastIndexOf("\\"));
			mkDirs(path);
		}


		File outputFile_temp = new File(fileName + ".temp");
		if (outputFile_temp.exists()) {
			outputFile_temp.delete();
		}
		try {
			osWeiter = new OutputStreamWriter(new FileOutputStream(outputFile_temp), charsetName);
			bw = new BufferedWriter(osWeiter);
			int num = 0;
			for (String line : lineList) {
				bw.write(line);
				bw.write("\n");
				num++;
				if (num >= 500) {
					bw.flush();
					num = 0;
				}
			}
			bw.flush();
			bw.close();
			bw = null;
			boolean renameFlag = outputFile_temp.renameTo(outputFile);
			if (renameFlag == false) {
				logger.error("rename file failed. ---> " + outputFile_temp.getAbsolutePath());
			}
			flag = true;
		} catch (UnsupportedEncodingException e1) {
			logger.error("", e1);
		} catch (FileNotFoundException e2) {
			logger.error("", e2);
		} catch (IOException e3) {
			logger.error("", e3);
		} finally {
			if (bw != null) {
				try {
					bw.close();
					bw = null;
				} catch (IOException e) {
				}
			}
			if (osWeiter != null) {
				try {
					osWeiter.close();
					osWeiter = null;
				} catch (IOException e) {
				}
			}
		}
		return flag;
	}

	/**
	 *  writeLineArr 按行写入文件对文件进行追加
	 *  <p>
	 *      写入数组到文件
	 *  </p>
	 *
	 * @param fileName  路径+文件名称
	 * @param arr 内容数组
	 * @param charsetName  编码
	 * @return boolean 类型 是否成功  true: 成功  flase 失败
	 */
	public boolean writeLineArr(String fileName, String[] arr, String charsetName) {
		boolean flag = false;
		BufferedWriter bw = null;
		OutputStreamWriter osWeiter = null;
		File outputFile = new File(fileName);
		if (outputFile.exists()) {
			outputFile.delete();
		}else{
			System.out.println(fileName+"=====");
			String path = fileName.substring(0, fileName.lastIndexOf("/"));
			mkDirs(path);
		}
		
		
		File outputFile_temp = new File(fileName + ".temp");
		if (outputFile_temp.exists()) {
			outputFile_temp.delete();
		}
		try {
			osWeiter = new OutputStreamWriter(new FileOutputStream(outputFile_temp), charsetName);
			bw = new BufferedWriter(osWeiter);
			int num = 0;
			for (String line : arr) {
				bw.write(line);
				bw.write("\n");
				num++;
				if (num >= 500) {
					bw.flush();
					num = 0;
				}
			}
			bw.flush();
			bw.close();
			bw = null;
			boolean renameFlag = outputFile_temp.renameTo(outputFile);
			if (renameFlag == false) {
				logger.error("rename file failed. ---> " + outputFile_temp.getAbsolutePath());
			}
			flag = true;
		} catch (UnsupportedEncodingException e1) {
			logger.error("", e1);
		} catch (FileNotFoundException e2) {
			logger.error("", e2);
		} catch (IOException e3) {
			logger.error("", e3);
		} finally {
			if (bw != null) {
				try {
					bw.close();
					bw = null;
				} catch (IOException e) {
				}
			}
			if (osWeiter != null) {
				try {
					osWeiter.close();
					osWeiter = null;
				} catch (IOException e) {
				}
			}
		}
		return flag;
	}

	/**
	 *  writeLineArr 按行写入文件对文件进行追加
	 *  <p>
	 *      写入set集合到文件
	 *  </p>
	 *
	 * @param fileName  路径+文件名称
	 * @param lineList 内容数组
	 * @param charsetName  编码
	 * @return boolean 类型 是否成功  true: 成功  flase 失败
	 */
	public boolean writeLineSet(String fileName, Set<String> lineList, String charsetName) {
		boolean flag = false;
		BufferedWriter bw = null;
		OutputStreamWriter osWeiter = null;
		File outputFile = new File(fileName);
		if (outputFile.exists()) {
			outputFile.delete();
		}else{
			String path = fileName.substring(0, fileName.lastIndexOf("\\"));
			mkDirs(path);
		}


		File outputFile_temp = new File(fileName + ".temp");
		if (outputFile_temp.exists()) {
			outputFile_temp.delete();
		}
		try {
			osWeiter = new OutputStreamWriter(new FileOutputStream(outputFile_temp), charsetName);
			bw = new BufferedWriter(osWeiter);
			int num = 0;
			for (String line : lineList) {
				bw.write(line);
				bw.write("\n");
				num++;
				if (num >= 500) {
					bw.flush();
					num = 0;
				}
			}
			bw.flush();
			bw.close();
			bw = null;
			boolean renameFlag = outputFile_temp.renameTo(outputFile);
			if (renameFlag == false) {
				logger.error("rename file failed. ---> " + outputFile_temp.getAbsolutePath());
			}
			flag = true;
		} catch (UnsupportedEncodingException e1) {
			logger.error("", e1);
		} catch (FileNotFoundException e2) {
			logger.error("", e2);
		} catch (IOException e3) {
			logger.error("", e3);
		} finally {
			if (bw != null) {
				try {
					bw.close();
					bw = null;
				} catch (IOException e) {
				}
			}
			if (osWeiter != null) {
				try {
					osWeiter.close();
					osWeiter = null;
				} catch (IOException e) {
				}
			}
		}
		return flag;
	}

	/**
	 *  mkDirs 创建目录
	 *  <p>
	 *      创建目录
	 *  </p>
	 *
	 * @param path  目录

	 * @return boolean 类型 是否成功  true: 成功  flase 失败
	 */

	public static boolean mkDirs(String path) {
		try {
			if(path == null) {
				return false;
			}
			File file = new File(path);
			if(!file.exists()) {
				return file.mkdirs();
			}
		}
		catch(Exception e) {
//			e.printStackTrace();
		}
		return false;
	}

	/**
	 *  writeLineMap 泛型定义对map进行文件写入
	 *  <p>
	 *      写入泛型map到文件
	 *  </p>
	 *
	 * @param fileName  路径+文件名称
	 * @param lineList 内容数组
	 * @param charsetName  编码
	 * @return boolean 类型 是否成功  true: 成功  flase 失败
	 */
	public <K, V extends Comparable<? super V>> Map<K, V> writeLineMap(String fileName, Map<K,V> lineList, String charsetName) {
		BufferedWriter bw = null;
		OutputStreamWriter osWeiter = null;
		File outputFile = new File(fileName);
		if (outputFile.exists()) {
			outputFile.delete();
		}else{
			String path = fileName.substring(0, fileName.lastIndexOf("/"));
			mkDirs(path);
		}
		
		
		File outputFile_temp = new File(fileName + ".temp");
		if (outputFile_temp.exists()) {
			outputFile_temp.delete();
		}
		try {
			osWeiter = new OutputStreamWriter(new FileOutputStream(outputFile_temp), charsetName);
			bw = new BufferedWriter(osWeiter);
			int num = 0;
			for (K line : lineList.keySet()) {
				bw.write(line+"|"+lineList.get(line)+"\n");
				num++;
				if (num >= 500) {
					bw.flush();
					num = 0;
				}
			}
			bw.flush();
			bw.close();
			bw = null;
			boolean renameFlag = outputFile_temp.renameTo(outputFile);
			if (renameFlag == false) {
				logger.error("rename file failed. ---> " + outputFile_temp.getAbsolutePath());
			}
		} catch (UnsupportedEncodingException e1) {
			logger.error("", e1);
		} catch (FileNotFoundException e2) {
			logger.error("", e2);
		} catch (IOException e3) {
			logger.error("", e3);
		} finally {
			if (bw != null) {
				try {
					bw.close();
					bw = null;
				} catch (IOException e) {
				}
			}
			if (osWeiter != null) {
				try {
					osWeiter.close();
					osWeiter = null;
				} catch (IOException e) {
				}
			}
		}
		return lineList;
	}

	/**
	 *  writeLineMap 泛型定义对map进行文件写入
	 *  <p>
	 *      写入字符串到文件，逐行写入
	 *  </p>
	 *
	 * @param is  数据
	 * @param output 路径
	 * @param charsetName  编码
	 * @return boolean 类型 是否成功  true: 成功  flase 失败
	 */
	public boolean writeInputStream(InputStream is, String output, String charsetName) {
		boolean flag = false;
		File file = new File(output + ".temp");
		if (file.exists()) {
			file.delete();
		}
		// System.out.println(dest.getAbsolutePath());
		BufferedWriter bw = null;
		OutputStreamWriter osWeiter = null;
		FileOutputStream fileOS = null;
		//
		BufferedReader br = null;
		InputStreamReader isReader = null;
		try {
			fileOS = new FileOutputStream(file);
			osWeiter = new OutputStreamWriter(fileOS, charsetName);
			bw = new BufferedWriter(osWeiter);
			//
			isReader = new InputStreamReader(is);
			br = new BufferedReader(isReader);
			if (bw != null) {
				int temp = 0;
				char[] cbuf = new char[2048];
				while ((temp = br.read(cbuf)) > 0) { // 当没有读取完时，继续读取
					bw.write(cbuf, 0, temp);
				}
				bw.flush();
			}
		} catch (UnsupportedEncodingException e1) {
			logger.error("", e1);
		} catch (FileNotFoundException e2) {
			logger.error("", e2);
		} catch (IOException e3) {
			logger.error("", e3);
		} finally {
			if (bw != null) {
				try {
					bw.close();
					bw = null;
				} catch (IOException e) {
				}
			}
			if (osWeiter != null) {
				try {
					osWeiter.close();
					osWeiter = null;
				} catch (IOException e) {
				}
			}
			if (fileOS != null) {
				try {
					fileOS.close();
					fileOS = null;
				} catch (IOException e) {
				}
			}
			if (br != null) {
				try {
					br.close();
					br = null;
				} catch (IOException e) {
				}
			}
			if (isReader != null) {
				try {
					isReader.close();
					isReader = null;
				} catch (IOException e) {
				}
			}
		}
		File dest = new File(output.replace(".temp", ""));
		if (dest.exists()) {
			dest.delete();
		}
		boolean renameFlag = file.renameTo(dest);
		if (renameFlag) {
			flag = true;
			// logger.info("rename dat file success. " + dest.getAbsolutePath());
		} else {
			logger.error("rename dat file failed. " + file.getAbsolutePath());
		}
		return flag;
	}





	/**
	 *  updateFileName 移动文件
	 *  <p>
	 *      移动文件到指定目录
	 *  </p>
	 *
	 * @param file  文件
	 * @param path 路径
	 * @return boolean 类型 是否成功  true: 成功  flase 失败
	 */
	public static void updateFileName(File file, String path) {
		try {
			File dofile = new File(path+file.getName().substring(0,file.getName().length()-3));
			file.renameTo(dofile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
