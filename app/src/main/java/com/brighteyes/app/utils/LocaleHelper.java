package com.brighteyes.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/**
 * 语言切换帮助类
 * 支持在应用内切换语言
 */
public class LocaleHelper {

    private static final String PREF_NAME = "BrightEyesPrefs";
    private static final String KEY_LANGUAGE = "app_language";

    // 语言代码
    public static final String LANG_SYSTEM = "system";  // 跟随系统
    public static final String LANG_CHINESE = "zh";
    public static final String LANG_ENGLISH = "en";
    public static final String LANG_FRENCH = "fr";
    public static final String LANG_SPANISH = "es";
    public static final String LANG_JAPANESE = "ja";
    public static final String LANG_KOREAN = "ko";

    /**
     * 获取支持的语言列表
     */
    public static String[] getSupportedLanguages() {
        return new String[] {
            LANG_SYSTEM,
            LANG_CHINESE,
            LANG_ENGLISH,
            LANG_FRENCH,
            LANG_SPANISH,
            LANG_JAPANESE,
            LANG_KOREAN
        };
    }

    /**
     * 获取语言显示名称
     */
    public static String getLanguageDisplayName(Context context, String langCode) {
        switch (langCode) {
            case LANG_SYSTEM:
                return context.getString(com.brighteyes.app.R.string.language_follow_system);
            case LANG_CHINESE:
                return context.getString(com.brighteyes.app.R.string.language_chinese);
            case LANG_ENGLISH:
                return context.getString(com.brighteyes.app.R.string.language_english);
            case LANG_FRENCH:
                return context.getString(com.brighteyes.app.R.string.language_french);
            case LANG_SPANISH:
                return context.getString(com.brighteyes.app.R.string.language_spanish);
            case LANG_JAPANESE:
                return context.getString(com.brighteyes.app.R.string.language_japanese);
            case LANG_KOREAN:
                return context.getString(com.brighteyes.app.R.string.language_korean);
            default:
                return langCode;
        }
    }

    /**
     * 保存选中的语言
     */
    public static void setLanguage(Context context, String langCode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply();
    }

    /**
     * 获取当前选中的语言
     */
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANG_SYSTEM);
    }

    /**
     * 应用语言设置到Context
     * 在Activity的attachBaseContext中调用
     */
    public static Context applyLanguage(Context context) {
        String langCode = getLanguage(context);
        return updateResources(context, langCode);
    }

    /**
     * 更新资源配置
     */
    private static Context updateResources(Context context, String langCode) {
        Locale locale;

        if (LANG_SYSTEM.equals(langCode)) {
            // 跟随系统语言
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = Resources.getSystem().getConfiguration().getLocales().get(0);
            } else {
                locale = Resources.getSystem().getConfiguration().locale;
            }
        } else {
            locale = new Locale(langCode);
        }

        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(new LocaleList(locale));
        }
        configuration.setLocale(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            return context.createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            return context;
        }
    }

    /**
     * 获取当前语言对应的索引
     */
    public static int getLanguageIndex(Context context) {
        String currentLang = getLanguage(context);
        String[] languages = getSupportedLanguages();
        for (int i = 0; i < languages.length; i++) {
            if (languages[i].equals(currentLang)) {
                return i;
            }
        }
        return 0; // 默认返回系统语言
    }

    /**
     * 根据索引获取语言代码
     */
    public static String getLanguageByIndex(int index) {
        String[] languages = getSupportedLanguages();
        if (index >= 0 && index < languages.length) {
            return languages[index];
        }
        return LANG_SYSTEM;
    }
}
