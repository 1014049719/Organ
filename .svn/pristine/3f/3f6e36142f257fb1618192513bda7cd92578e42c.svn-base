package org.vudroid.core;

import android.content.Context;

import com.github.junrar.util.ContextUtils;

public class VuDroidLibraryLoader
{
    private static boolean alreadyLoaded = false;

    public static void load(Context context)
    {
        if (alreadyLoaded)
        {
            return;
        }
        System.load(ContextUtils.getDataLibsDir(context)+"libvudroid.so");
        alreadyLoaded = true;
    }
}
