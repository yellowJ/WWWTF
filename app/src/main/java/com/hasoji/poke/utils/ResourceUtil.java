package com.hasoji.poke.utils;

import android.content.Context;

/**
 * Created by A on 2016/9/12.
 */
public class ResourceUtil {
    public static int getResourceId(Context paramContext, String paramString1, String paramString2) {
        return paramContext.getResources().getIdentifier(paramString2, paramString1, paramContext.getPackageName());
    }
}
