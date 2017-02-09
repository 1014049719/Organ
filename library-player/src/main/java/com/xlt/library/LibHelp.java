package com.xlt.library;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.github.junrar.util.CPU;
import com.github.junrar.util.ContextUtils;
import com.hostzi.blenderviking.extractarchiveandroid.ExtractFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;


/**
 * Created by zjh on 2016/3/25.
 */
public class LibHelp {
    private LibHelp(){}
    public static LibHelp instance;
    /**
     * cpu支持的指令集
     */
    private static String cpuSupport;

    private static final String ARMEABI_V5 = "armeabi";
    private static final String ARMEABI_V7A = "armeabi_v7a";
    private static final String X86 = "x86";
    private static final String NONE = "";
    private static final String ARMEABI_64 = "arm64_v8a";

    private OnInitListener onInitListener;
    private final int INIT_SUCCESS = 1;
    private final int INIT_ERROR = 2;
    private static int libsResId;
    public  String appDataDir;

    private static final String PROC_CPU_INFO_PATH = "/proc/cpuinfo";


    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if (onInitListener == null){
                return;
            }
            if(msg.what == INIT_SUCCESS){
                onInitListener.onSuccess();
            }else if(msg.what == INIT_ERROR){
                onInitListener.onError();
            }
        }
    };



    static {
       int cpu = CPU.getFeature();
        if ((cpu & CPU.FEATURE_ARM_NEON) > 0){
            cpuSupport = ARMEABI_V7A;
            libsResId = R.raw.armeabi;
        } else if ((cpu & CPU.FEATURE_ARM_VFPV3) > 0 && (cpu & CPU.FEATURE_ARM_V7A) > 0){
            cpuSupport = ARMEABI_V7A;
            libsResId = R.raw.armeabi;
        } else if ((cpu & CPU.FEATURE_ARM_VFP) > 0 && (cpu & CPU.FEATURE_ARM_V6) > 0){
            cpuSupport = ARMEABI_V5;
            libsResId = R.raw.armeabi;
        } else if ((cpu & CPU.FEATURE_ARM_V6) > 0){
            cpuSupport = ARMEABI_V5;
            libsResId = R.raw.armeabi;
        } else if ((cpu & CPU.FEATURE_X86) > 0){
            cpuSupport = X86;
            libsResId = R.raw.x86;
        } else{
            cpuSupport = ARMEABI_V5;
            libsResId = R.raw.armeabi;
        }

        if (isCPUInfo64()){
            //加载64位的so
            cpuSupport = ARMEABI_64;
            libsResId = R.raw.arm64_v8a;
        }
    }



    public static LibHelp getInstance(){
        if(instance == null){
            instance = new LibHelp();
        }
        return instance;
    }

    /**
     * 初始化so库,主要将so库重7z中解压出来
     */
    public void initializeLib(Context context,OnInitListener onInitListener){
        this.onInitListener = onInitListener;
        appDataDir = ContextUtils.getDataLibsDir(context);
        if(isNeedInit()){
            String libFilePath = copyRawFileToAppLibs(context,libsResId);
            compress7zFile(context,libFilePath);
        }
    }

    /**
     * 是否需要初始化
     * @return
     */
    private boolean isNeedInit(){
        File file = new File(appDataDir);
        if(file.isDirectory() && file.list().length > 0){
            handler.sendEmptyMessage(INIT_SUCCESS);
            return false;
        }
        return true;
    }

    /**
     * 检查是否有so库
     * @param context
     * @return
     */
    public boolean checkHasLibs(Context context){
        File file = new File(ContextUtils.getDataLibsDir(context));
        if(file.isDirectory() && file.list().length > 0){
            return true;
        }
        return false;
    }


    /**
     * 解压7Z文件
     * @param context
     * @param inputPath
     */
    public void compress7zFile(Context context,String inputPath){
        final File inputFile = new File(inputPath);
        if (!inputFile.exists()){
            return;
        }
        ExtractFile ef = null;
        try {
            ef = new ExtractFile(context,inputPath,appDataDir);
            ef.setListener(new ExtractFile.OnCompress7zListener() {
                @Override
                public void onSuccess() {
                    delErrorFile(inputFile);
                    handler.sendEmptyMessage(INIT_SUCCESS);
                }

                @Override
                public void onError() {
                    delErrorFile(inputFile);
                    handler.sendEmptyMessage(INIT_ERROR);
                }
            });
            ef.exec();
        } catch (IOException e) {
            e.printStackTrace();
            delErrorFile(inputFile);
            handler.sendEmptyMessage(INIT_ERROR);
        }
    }

    private void delErrorFile(File inputFile) {
        if(inputFile.exists()){
            inputFile.delete();
        }
    }

    /**
     * 将raw中7z压缩文件拷贝到应用目录的libs目录下,等待解压
     * @param context
     */
    public String copyRawFileToAppLibs(Context context, int resId){
        String libFilePath = ContextUtils.getDataLibsDir(context) + cpuSupport+".7z";
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = context.getResources().openRawResource(resId);
            fos = new FileOutputStream(libFilePath);

            int len;
            byte[] buff = new byte[1024];
            while((len = is.read(buff)) != -1){
                fos.write(buff,0,len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(is != null){
                try {
                    is.close();
                    is = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fos != null){
                try {
                    fos.close();
                    fos = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return libFilePath;
    }


    public interface OnInitListener{
        void onSuccess();
        void onError();
    }

    /**
     * Read the first line of "/proc/cpuinfo" file, and check if it is 64 bit.
     */
    private static boolean isCPUInfo64() {
        File cpuInfo = new File(PROC_CPU_INFO_PATH);
        if (cpuInfo != null && cpuInfo.exists()) {
            InputStream inputStream = null;
            BufferedReader bufferedReader = null;
            try {
                inputStream = new FileInputStream(cpuInfo);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 512);
                String line = bufferedReader.readLine();
                if (line != null && line.length() > 0 && line.toLowerCase(Locale.US).contains("arch64")) {
                    return true;
                } else {
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
