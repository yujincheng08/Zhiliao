package com.shatyuka.zhiliao.hooks;

import android.os.Build;
import android.view.Window;

import com.shatyuka.zhiliao.Helper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class ColorMode implements IHook {
    @Override
    public String getName() {
        return "禁止切换色彩模式";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {

    }

    @Override
    public void hook() throws Throwable {
        if (Build.VERSION.SDK_INT >= 26) {
            XposedHelpers.findAndHookMethod(Window.class, "setColorMode", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_colormode", false)) {
                        param.setResult(null);
                    }
                }
            });
        }
    }
}
