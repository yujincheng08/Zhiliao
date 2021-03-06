package com.shatyuka.zhiliao.hooks;

import android.view.View;
import android.view.ViewGroup;

import com.shatyuka.zhiliao.Helper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public class NextAnswer implements IHook {
    static Class<?> NextContentAnimationView;

    @Override
    public String getName() {
        return "移除下一个回答按钮";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        if (Helper.packageInfo.versionCode > 2614) {
            NextContentAnimationView = classLoader.loadClass("com.zhihu.android.mix.widget.NextContentAnimationView");
        }
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_nextanswer", false)) {
            XposedHelpers.findAndHookMethod(Helper.AnswerPagerFragment, "setupNextAnswerBtn", XC_MethodReplacement.returnConstant(null));

            if (Helper.packageInfo.versionCode > 2614) {
                XposedHelpers.findAndHookMethod(ViewGroup.class, "addView", View.class, ViewGroup.LayoutParams.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (param.args[0].getClass() == NextContentAnimationView)
                            ((View) param.args[0]).setVisibility(View.GONE);
                    }
                });
            }
        }
    }
}
