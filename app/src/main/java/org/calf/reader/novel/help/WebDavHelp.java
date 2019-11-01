package org.calf.reader.novel.help;

import android.text.TextUtils;

import org.calf.reader.novel.MApplication;
import org.calf.reader.novel.utils.StringUtils;
import org.calf.reader.novel.utils.web_dav.http.HttpAuth;

import org.calf.reader.novel.constant.AppConstant;

public class WebDavHelp {

    public static String getWebDavUrl() {
        String url = MApplication.getConfigPreferences().getString("web_dav_url", AppConstant.DEFAULT_WEB_DAV_URL);
        if (TextUtils.isEmpty(url)) url = AppConstant.DEFAULT_WEB_DAV_URL;
        assert url != null;
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    public static boolean initWebDav() {
        String account = MApplication.getConfigPreferences().getString("web_dav_account", "");
        String password = MApplication.getConfigPreferences().getString("web_dav_password", "");
        if (!StringUtils.isTrimEmpty(account) && !StringUtils.isTrimEmpty(password)) {
            HttpAuth.setAuth(account, password);
            return true;
        }
        return false;
    }

    private WebDavHelp() {

    }
}
