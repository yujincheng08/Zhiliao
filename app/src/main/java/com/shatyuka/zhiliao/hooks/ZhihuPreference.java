package com.shatyuka.zhiliao.hooks;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.shatyuka.zhiliao.Helper;
import com.shatyuka.zhiliao.R;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class ZhihuPreference implements IHook {
    final static String modulePackage = "com.shatyuka.zhiliao";

    private static Object preference_zhiliao;

    private static int version_click = 0;
    private static int author_click = 0;

    private static int settings_res_id = 0;
    private static int debug_res_id = 0;

    static Class<?> SettingsFragment;
    static Class<?> DebugFragment;
    static Class<?> Preference;
    static Class<?> SwitchPreference;
    static Class<?> OnPreferenceChangeListener;
    static Class<?> OnPreferenceClickListener;
    static Class<?> PreferenceFragmentCompat;
    static Class<?> PreferenceManager;
    static Class<?> PreferenceInflater;
    static Class<?> PageInfoType;
    static Class<?> ZHIntent;
    static Class<?> MainActivity;
    static Class<?> BasePreferenceFragment;
    static Class<?> PreferenceGroup;
    static Class<?> EditTextPreference;

    static Method findPreference;
    static Method setSummary;
    static Method setIcon;
    static Method setVisible;
    static Method getKey;
    static Method setChecked;
    static Method setOnPreferenceChangeListener;
    static Method setOnPreferenceClickListener;
    static Method setSharedPreferencesName;
    static Method getContext;
    static Method getText;
    static Method addPreferencesFromResource;
    static Method inflate;

    @Override
    public String getName() {
        return "设置入口";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        SettingsFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.preference.SettingsFragment");
        DebugFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.DebugFragment");
        Preference = classLoader.loadClass("androidx.preference.Preference");
        SwitchPreference = classLoader.loadClass("com.zhihu.android.app.ui.widget.SwitchPreference");
        OnPreferenceChangeListener = classLoader.loadClass("androidx.preference.Preference$c");
        OnPreferenceClickListener = classLoader.loadClass("androidx.preference.Preference$d");
        PreferenceFragmentCompat = classLoader.loadClass("androidx.preference.g");
        PreferenceManager = classLoader.loadClass("androidx.preference.j");
        PreferenceInflater = classLoader.loadClass("androidx.preference.i");
        PageInfoType = classLoader.loadClass("com.zhihu.android.data.analytics.PageInfoType");
        ZHIntent = classLoader.loadClass("com.zhihu.android.answer.entrance.AnswerPagerEntance").getMethod("buildIntent", long.class).getReturnType();
        MainActivity = classLoader.loadClass("com.zhihu.android.app.ui.activity.MainActivity");
        BasePreferenceFragment = classLoader.loadClass("com.zhihu.android.app.ui.fragment.BasePreferenceFragment");
        PreferenceGroup = classLoader.loadClass("androidx.preference.PreferenceGroup");
        EditTextPreference = classLoader.loadClass("androidx.preference.EditTextPreference");

        findPreference = SettingsFragment.getMethod("a", CharSequence.class);
        setSummary = Preference.getMethod("a", CharSequence.class);
        setIcon = Preference.getMethod("a", Drawable.class);
        setVisible = Preference.getMethod("c", boolean.class);
        getKey = Preference.getMethod("C");
        setChecked = SwitchPreference.getMethod("g", boolean.class);
        setOnPreferenceChangeListener = Preference.getMethod("a", OnPreferenceChangeListener);
        setOnPreferenceClickListener = Preference.getMethod("a", OnPreferenceClickListener);
        setSharedPreferencesName = PreferenceManager.getMethod("a", String.class);
        getContext = BasePreferenceFragment.getMethod("getContext");
        getText = EditTextPreference.getMethod("i");
        addPreferencesFromResource = PreferenceFragmentCompat.getMethod("b", int.class);
        inflate = PreferenceInflater.getMethod("a", XmlPullParser.class, PreferenceGroup);
    }

    @Override
    public void hook() throws Throwable {
        XposedHelpers.findAndHookMethod(PreferenceInflater, "a", int.class, PreferenceGroup, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XmlResourceParser parser;
                int id = (int) param.args[0];
                if (id == 7355608)
                    parser = Helper.modRes.getXml(R.xml.settings);
                else if (id == debug_res_id)
                    parser = Helper.modRes.getXml(R.xml.preferences_zhihu);
                else
                    return;
                try {
                    param.setResult(inflate.invoke(param.thisObject, parser, param.args[1]));
                } finally {
                    parser.close();
                }
            }
        });

        XposedHelpers.findAndHookMethod(SettingsFragment, "i", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                settings_res_id = (int) param.getResult();
            }
        });
        XposedHelpers.findAndHookMethod(DebugFragment, "i", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                debug_res_id = (int) param.getResult();
            }
        });

        XposedBridge.hookMethod(addPreferencesFromResource, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (((int) param.args[0]) == settings_res_id) {
                    addPreferencesFromResource.invoke(param.thisObject, 7355608);
                }
            }
        });

        XposedHelpers.findAndHookMethod(SettingsFragment, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object thisObject = param.thisObject;
                preference_zhiliao = findPreference.invoke(thisObject, "preference_id_zhiliao");
                setSummary.invoke(preference_zhiliao, "当前版本 " + Helper.modRes.getString(R.string.app_version));
                setOnPreferenceClickListener.invoke(preference_zhiliao, thisObject);
            }
        });

        XposedHelpers.findAndHookMethod(SettingsFragment, "onPreferenceClick", Preference, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] == preference_zhiliao) {
                    Object thisObject = param.thisObject;
                    Method a = thisObject.getClass().getMethod("a", ZHIntent);
                    Object intent = ZHIntent.getConstructors()[0].newInstance(DebugFragment, null, "SCREEN_NAME_NULL", Array.newInstance(PageInfoType, 0));
                    a.invoke(thisObject, intent);
                    param.setResult(false);
                }
            }
        });
        XposedBridge.hookMethod(DebugFragment.getMethod("a", Bundle.class, String.class), new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.thisObject.getClass() == DebugFragment) {
                    Field[] fields = PreferenceFragmentCompat.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getType() == PreferenceManager) {
                            field.setAccessible(true);
                            setSharedPreferencesName.invoke(field.get(param.thisObject), "zhiliao_preferences");
                            return;
                        }
                    }
                }
            }
        });
        XposedHelpers.findAndHookMethod(BasePreferenceFragment, "onViewCreated", View.class, Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.thisObject.getClass() == DebugFragment) {
                    Field[] fields = BasePreferenceFragment.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.getType().getName().equals("com.zhihu.android.app.ui.widget.SystemBar")) {
                            field.setAccessible(true);
                            Object systemBar = field.get(param.thisObject);
                            Object toolbar = systemBar.getClass().getMethod("getToolbar").invoke(systemBar);
                            toolbar.getClass().getMethod("setTitle", CharSequence.class).invoke(toolbar, "知了");
                            break;
                        }
                    }
                }
            }
        });
        XposedHelpers.findAndHookMethod(DebugFragment, "h", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                Object thisObject = param.thisObject;
                Object preference_version = findPreference.invoke(thisObject, "preference_version");
                Object preference_author = findPreference.invoke(thisObject, "preference_author");
                Object preference_help = findPreference.invoke(thisObject, "preference_help");
                Object preference_channel = findPreference.invoke(thisObject, "preference_channel");
                Object preference_telegram = findPreference.invoke(thisObject, "preference_telegram");
                Object preference_sourcecode = findPreference.invoke(thisObject, "preference_sourcecode");
                Object preference_donate = findPreference.invoke(thisObject, "preference_donate");
                Object preference_status = findPreference.invoke(thisObject, "preference_status");
                Object switch_externlink = findPreference.invoke(thisObject, "switch_externlink");
                Object switch_externlinkex = findPreference.invoke(thisObject, "switch_externlinkex");
                Object switch_tag = findPreference.invoke(thisObject, "switch_tag");
                Object switch_livebutton = findPreference.invoke(thisObject, "switch_livebutton");
                Object switch_reddot = findPreference.invoke(thisObject, "switch_reddot");
                Object switch_vipbanner = findPreference.invoke(thisObject, "switch_vipbanner");
                Object switch_vipnav = findPreference.invoke(thisObject, "switch_vipnav");
                Object switch_videonav = findPreference.invoke(thisObject, "switch_videonav");
                Object switch_article = findPreference.invoke(thisObject, "switch_article");
                Object switch_horizontal = findPreference.invoke(thisObject, "switch_horizontal");
                Object switch_nextanswer = findPreference.invoke(thisObject, "switch_nextanswer");

                setOnPreferenceChangeListener.invoke(findPreference.invoke(thisObject, "accept_eula"), thisObject);
                setOnPreferenceClickListener.invoke(switch_externlink, thisObject);
                setOnPreferenceClickListener.invoke(switch_externlinkex, thisObject);
                setOnPreferenceClickListener.invoke(switch_tag, thisObject);
                setOnPreferenceClickListener.invoke(switch_livebutton, thisObject);
                setOnPreferenceClickListener.invoke(switch_reddot, thisObject);
                setOnPreferenceClickListener.invoke(switch_vipbanner, thisObject);
                setOnPreferenceClickListener.invoke(switch_vipnav, thisObject);
                setOnPreferenceClickListener.invoke(switch_videonav, thisObject);
                setOnPreferenceClickListener.invoke(switch_article, thisObject);
                setOnPreferenceClickListener.invoke(switch_horizontal, thisObject);
                setOnPreferenceClickListener.invoke(switch_nextanswer, thisObject);
                setOnPreferenceClickListener.invoke(preference_version, thisObject);
                setOnPreferenceClickListener.invoke(preference_author, thisObject);
                setOnPreferenceClickListener.invoke(preference_help, thisObject);
                setOnPreferenceClickListener.invoke(preference_channel, thisObject);
                setOnPreferenceClickListener.invoke(preference_telegram, thisObject);
                setOnPreferenceClickListener.invoke(preference_sourcecode, thisObject);
                setOnPreferenceClickListener.invoke(preference_donate, thisObject);

                String real_version = null;
                try {
                    real_version = Helper.context.getPackageManager().getResourcesForApplication(modulePackage).getString(R.string.app_version);
                } catch (Exception ignore) {
                }
                String loaded_version = Helper.modRes.getString(R.string.app_version);
                setSummary.invoke(preference_version, loaded_version);
                if (real_version == null || loaded_version.equals(real_version)) {
                    setVisible.invoke(preference_status, false);
                } else {
                    setOnPreferenceClickListener.invoke(preference_status, thisObject);
                    Object category_eula = findPreference.invoke(thisObject, "category_eula");
                    Object category_ads = findPreference.invoke(thisObject, "category_ads");
                    Object category_misc = findPreference.invoke(thisObject, "category_misc");
                    Object category_ui = findPreference.invoke(thisObject, "category_ui");
                    Object category_swap_answers = findPreference.invoke(thisObject, "category_swap_answers");
                    Object category_filter = findPreference.invoke(thisObject, "category_filter");
                    setVisible.invoke(category_eula, false);
                    setVisible.invoke(category_ads, false);
                    setVisible.invoke(category_misc, false);
                    setVisible.invoke(category_ui, false);
                    setVisible.invoke(category_swap_answers, false);
                    setVisible.invoke(category_filter, false);
                    return null;
                }

                setIcon.invoke(preference_status, Helper.modRes.getDrawable(R.drawable.ic_refresh));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_mainswitch"), Helper.modRes.getDrawable(R.drawable.ic_toggle_on));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_launchad"), Helper.modRes.getDrawable(R.drawable.ic_ad_units));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_feedad"), Helper.modRes.getDrawable(R.drawable.ic_table_rows));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_answerlistad"), Helper.modRes.getDrawable(R.drawable.ic_format_list));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_commentad"), Helper.modRes.getDrawable(R.drawable.ic_comment));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_sharead"), Helper.modRes.getDrawable(R.drawable.ic_share));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_answerad"), Helper.modRes.getDrawable(R.drawable.ic_notes));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_searchad"), Helper.modRes.getDrawable(R.drawable.ic_search));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_video"), Helper.modRes.getDrawable(R.drawable.ic_play_circle));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_marketcard"), Helper.modRes.getDrawable(R.drawable.ic_vip));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_club"), Helper.modRes.getDrawable(R.drawable.ic_group));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_goods"), Helper.modRes.getDrawable(R.drawable.ic_local_mall));
                setIcon.invoke(switch_externlink, Helper.modRes.getDrawable(R.drawable.ic_link));
                setIcon.invoke(switch_externlinkex, Helper.modRes.getDrawable(R.drawable.ic_link));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_colormode"), Helper.modRes.getDrawable(R.drawable.ic_color));
                setIcon.invoke(switch_tag, Helper.modRes.getDrawable(R.drawable.ic_label));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_statusbar"), Helper.modRes.getDrawable(R.drawable.ic_fullscreen));
                setIcon.invoke(switch_livebutton, Helper.modRes.getDrawable(R.drawable.ic_live_tv));
                setIcon.invoke(switch_reddot, Helper.modRes.getDrawable(R.drawable.ic_mark_chat_unread));
                setIcon.invoke(switch_vipbanner, Helper.modRes.getDrawable(R.drawable.ic_vip_banner));
                setIcon.invoke(switch_vipnav, Helper.modRes.getDrawable(R.drawable.ic_vip_nav));
                setIcon.invoke(switch_videonav, Helper.modRes.getDrawable(R.drawable.ic_play_circle));
                setIcon.invoke(findPreference.invoke(thisObject, "switch_hotbanner"), Helper.modRes.getDrawable(R.drawable.ic_whatshot));
                setIcon.invoke(switch_article, Helper.modRes.getDrawable(R.drawable.ic_article));
                setIcon.invoke(switch_horizontal, Helper.modRes.getDrawable(R.drawable.ic_swap_horiz));
                setIcon.invoke(switch_nextanswer, Helper.modRes.getDrawable(R.drawable.ic_circle_down));
                setIcon.invoke(findPreference.invoke(thisObject, "edit_title"), Helper.regex_title != null ? Helper.modRes.getDrawable(R.drawable.ic_check) : Helper.modRes.getDrawable(R.drawable.ic_close));
                setIcon.invoke(findPreference.invoke(thisObject, "edit_author"), Helper.regex_author != null ? Helper.modRes.getDrawable(R.drawable.ic_check) : Helper.modRes.getDrawable(R.drawable.ic_close));
                setIcon.invoke(findPreference.invoke(thisObject, "edit_content"), Helper.regex_content != null ? Helper.modRes.getDrawable(R.drawable.ic_check) : Helper.modRes.getDrawable(R.drawable.ic_close));
                setIcon.invoke(preference_version, Helper.modRes.getDrawable(R.drawable.ic_info));
                setIcon.invoke(preference_author, Helper.modRes.getDrawable(R.drawable.ic_person));
                setIcon.invoke(preference_help, Helper.modRes.getDrawable(R.drawable.ic_help));
                setIcon.invoke(preference_channel, Helper.modRes.getDrawable(R.drawable.ic_rss_feed));
                setIcon.invoke(preference_telegram, Helper.modRes.getDrawable(R.drawable.ic_telegram));
                setIcon.invoke(preference_sourcecode, Helper.modRes.getDrawable(R.drawable.ic_github));
                setIcon.invoke(preference_donate, Helper.modRes.getDrawable(R.drawable.ic_monetization));

                if (Helper.prefs.getBoolean("accept_eula", false)) {
                    Object category_eula = findPreference.invoke(thisObject, "category_eula");
                    setVisible.invoke(category_eula, false);
                } else {
                    Object switch_main = findPreference.invoke(param.thisObject, "switch_mainswitch");
                    setChecked.invoke(switch_main, false);
                }
                return null;
            }
        });
        XposedHelpers.findAndHookMethod(DebugFragment, "onPreferenceClick", "androidx.preference.Preference", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                Object preference = param.args[0];
                switch ((String) getKey.invoke(preference)) {
                    case "preference_status":
                        System.exit(0);
                        break;
                    case "preference_version":
                        version_click++;
                        if (version_click == 5) {
                            Toast.makeText(Helper.context, "点我次数再多，更新也不会变快哦", Toast.LENGTH_SHORT).show();
                            version_click = 0;
                        }
                        break;
                    case "preference_author":
                        author_click++;
                        if (author_click == 5) {
                            Toast.makeText(Helper.context, Helper.modRes.getStringArray(R.array.click_author)[new Random().nextInt(4)], Toast.LENGTH_SHORT).show();
                            author_click = 0;
                        }
                        break;
                    case "preference_help":
                        Uri uri_help = Uri.parse("https://github.com/shatyuka/Zhiliao/wiki");
                        Intent intent_help = new Intent(Intent.ACTION_VIEW, uri_help);
                        ((Context) getContext.invoke(param.thisObject)).startActivity(intent_help);
                        break;
                    case "preference_channel":
                        Uri uri_channel = Uri.parse("https://t.me/zhiliao");
                        Intent intent_channel = new Intent(Intent.ACTION_VIEW, uri_channel);
                        ((Context) getContext.invoke(param.thisObject)).startActivity(intent_channel);
                        break;
                    case "preference_telegram":
                        Uri uri_telegram = Uri.parse("https://t.me/joinchat/OibCWxbdCMkJ2fG8J1DpQQ");
                        Intent intent_telegram = new Intent(Intent.ACTION_VIEW, uri_telegram);
                        ((Context) getContext.invoke(param.thisObject)).startActivity(intent_telegram);
                        break;
                    case "preference_sourcecode":
                        Uri uri_sourcecode = Uri.parse("https://github.com/shatyuka/Zhiliao");
                        Intent intent_sourcecode = new Intent(Intent.ACTION_VIEW, uri_sourcecode);
                        ((Context) getContext.invoke(param.thisObject)).startActivity(intent_sourcecode);
                        break;
                    case "preference_donate":
                        Uri uri_donate = Uri.parse("https://github.com/shatyuka/Zhiliao/wiki/Donate");
                        Intent intent_donate = new Intent(Intent.ACTION_VIEW, uri_donate);
                        ((Context) getContext.invoke(param.thisObject)).startActivity(intent_donate);
                        break;
                    case "switch_externlink":
                        Object switch_externlinkex = findPreference.invoke(param.thisObject, "switch_externlinkex");
                        setChecked.invoke(switch_externlinkex, false);
                        break;
                    case "switch_externlinkex":
                        Object switch_externlink = findPreference.invoke(param.thisObject, "switch_externlink");
                        setChecked.invoke(switch_externlink, false);
                        break;
                    case "switch_tag":
                    case "switch_livebutton":
                    case "switch_reddot":
                    case "switch_vipbanner":
                    case "switch_vipnav":
                    case "switch_videonav":
                    case "switch_article":
                    case "switch_horizontal":
                    case "switch_nextanswer":
                        Toast.makeText(Helper.context, "重启知乎生效", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
        XposedHelpers.findAndHookMethod(DebugFragment, "a", "androidx.preference.Preference", Object.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                if ((boolean) param.args[1]) {
                    Object switch_main = findPreference.invoke(param.thisObject, "switch_mainswitch");
                    setChecked.invoke(switch_main, true);
                    Object category_eula = findPreference.invoke(param.thisObject, "category_eula");
                    setVisible.invoke(category_eula, false);
                }
                return true;
            }
        });
        XposedHelpers.findAndHookMethod(EditTextPreference, "a", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object thisObject = param.thisObject;
                switch ((String) getKey.invoke(thisObject)) {
                    case "edit_title":
                        Helper.regex_title = Helper.compileRegex((String) getText.invoke(thisObject));
                        setIcon.invoke(thisObject, Helper.regex_title != null ? Helper.modRes.getDrawable(R.drawable.ic_check) : Helper.modRes.getDrawable(R.drawable.ic_close));
                        break;
                    case "edit_author":
                        Helper.regex_author = Helper.compileRegex((String) getText.invoke(thisObject));
                        setIcon.invoke(thisObject, Helper.regex_author != null ? Helper.modRes.getDrawable(R.drawable.ic_check) : Helper.modRes.getDrawable(R.drawable.ic_close));
                        break;
                    case "edit_content":
                        Helper.regex_content = Helper.compileRegex((String) getText.invoke(thisObject));
                        setIcon.invoke(thisObject, Helper.regex_content != null ? Helper.modRes.getDrawable(R.drawable.ic_check) : Helper.modRes.getDrawable(R.drawable.ic_close));
                        break;
                }
            }
        });

        XposedHelpers.findAndHookMethod(Dialog.class, "dismiss", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                Dialog dialog = (Dialog) param.thisObject;
                if (dialog.isShowing()) {
                    View view = dialog.getWindow().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) Helper.context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getRootView().getWindowToken(), 0);
                    }
                }
            }
        });
    }
}
