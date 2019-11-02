package org.calf.reader.novel.help;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.calf.reader.novel.BuildConfig;
import org.calf.reader.novel.DbHelper;
import org.calf.reader.novel.MApplication;
import org.calf.reader.novel.R;
import org.calf.reader.novel.bean.BookShelfBean;
import org.calf.reader.novel.bean.BookSourceBean;
import org.calf.reader.novel.bean.ReplaceRuleBean;
import org.calf.reader.novel.bean.SearchHistoryBean;
import org.calf.reader.novel.bean.TxtChapterRuleBean;
import org.calf.reader.novel.model.BookSourceManager;
import org.calf.reader.novel.model.ReplaceRuleManager;
import org.calf.reader.novel.model.TxtChapterRuleManager;
import org.calf.reader.novel.utils.FileUtils;
import org.calf.reader.novel.utils.RxUtils;
import org.calf.reader.novel.utils.XmlUtils;
import org.calf.reader.novel.utils.ZipUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by GKF on 2018/1/30.
 * 数据备份
 */

public class DataBackup {

    public static DataBackup getInstance() {
        return new DataBackup();
    }

    public void autoSave() {
        Single.create((SingleOnSubscribe<Boolean>) e -> {
            long currentTime = System.currentTimeMillis();
            if (!BuildConfig.DEBUG) {
                File file = new File(FileUtils.getSdCardPath() + File.separator + "YueDu" + File.separator + "autoSave" + File.separator + "myBookShelf.json");
                if (file.exists()) {
                    if (currentTime - file.lastModified() < TimeUnit.DAYS.toMillis(1)) {
                        e.onSuccess(false);
                        return;
                    }
                }
                DocumentHelper.createDirIfNotExist(FileUtils.getSdCardPath(), "YueDu");
                String dirPath = FileUtils.getSdCardPath() + File.separator + "YueDu";
                DocumentHelper.createDirIfNotExist(dirPath, "autoSave");
                dirPath += File.separator + "autoSave";
                backupConfig(dirPath);
                backupBookShelf(dirPath);
                backupBookSource(dirPath);
                backupSearchHistory(dirPath);
                backupReplaceRule(dirPath);
                backupTxtChapterRule(dirPath);
                upload(dirPath);
                e.onSuccess(true);
            }
            e.onSuccess(false);
        }).compose(RxUtils::toSimpleSingle)
                .subscribe();
    }

    public void run() {
        Single.create((SingleOnSubscribe<Boolean>) e -> {
            DocumentHelper.createDirIfNotExist(FileUtils.getSdCardPath(), "YueDu");
            String dirPath = FileUtils.getSdCardPath() + File.separator + "YueDu";
            backupConfig(dirPath);
            backupBookShelf(dirPath);
            backupBookSource(dirPath);
            backupSearchHistory(dirPath);
            backupReplaceRule(dirPath);
            backupTxtChapterRule(dirPath);
            upload(dirPath);
            e.onSuccess(true);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Boolean value) {
                        if (value) {
                            Toast.makeText(MApplication.getInstance(), R.string.backup_success, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MApplication.getInstance(), R.string.backup_fail, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Toast.makeText(MApplication.getInstance(), R.string.backup_fail, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void upload(String dirPath) {
        List<String> filePaths = new ArrayList<>();
        filePaths.add(dirPath + File.separator + "config.xml");
        filePaths.add(dirPath + File.separator + "myBookShelf.json");
        filePaths.add(dirPath + File.separator + "myBookSource.json");
        filePaths.add(dirPath + File.separator + "myBookSearchHistory.json");
        filePaths.add(dirPath + File.separator + "myBookReplaceRule.json");
        filePaths.add(dirPath + File.separator + "myTxtChapterRule.json");
        String zipFilePath = FileHelp.getCachePath() + File.separator + "backup" + ".zip";
        try {
            FileHelp.deleteFile(zipFilePath);
            if (ZipUtils.zipFiles(filePaths, zipFilePath)) {
//                if (WebDavHelp.initWebDav()) {
//                    new WebDavFile(WebDavHelp.getWebDavUrl() + "YueDu").makeAsDir();
//                    String putUrl = WebDavHelp.getWebDavUrl() + "YueDu/backup" + TimeUtils.date2String(TimeUtils.getNowDate(), new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())) + ".zip";
//                    WebDavFile webDavFile = new WebDavFile(putUrl);
//                    webDavFile.upload(zipFilePath);
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Handler(Looper.getMainLooper())
                    .post(() -> Toast
                            .makeText(MApplication.getInstance(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                            .show());
        }
    }

    private void backupBookShelf(String file) {
        List<BookShelfBean> bookShelfList = BookshelfHelp.getAllBook();
        if (bookShelfList != null && bookShelfList.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String bookshelf = gson.toJson(bookShelfList);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookShelf.json", file);
            if (docFile != null) {
                DocumentHelper.writeString(bookshelf, docFile);
            }
        }
    }

    private void backupBookSource(String file) {
        List<BookSourceBean> bookSourceList = BookSourceManager.getAllBookSource();
        if (bookSourceList != null && bookSourceList.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(bookSourceList);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookSource.json", file);
            if (docFile != null) {
                DocumentHelper.writeString(str, docFile);
            }
        }
    }

    private void backupSearchHistory(String file) {
        List<SearchHistoryBean> searchHistoryBeans = DbHelper.getDaoSession().getSearchHistoryBeanDao()
                .queryBuilder().list();
        if (searchHistoryBeans != null && searchHistoryBeans.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(searchHistoryBeans);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookSearchHistory.json", file);
            if (docFile != null) {
                DocumentHelper.writeString(str, docFile);
            }
        }
    }

    private void backupReplaceRule(String file) {
        List<ReplaceRuleBean> replaceRuleBeans = ReplaceRuleManager.getAll().blockingGet();
        if (replaceRuleBeans != null && replaceRuleBeans.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(replaceRuleBeans);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myBookReplaceRule.json", file);
            if (docFile != null) {
                DocumentHelper.writeString(str, docFile);
            }
        }
    }

    private void backupTxtChapterRule(String file) {
        List<TxtChapterRuleBean> ruleBeans = TxtChapterRuleManager.getAll();
        if (ruleBeans != null && ruleBeans.size() > 0) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting()
                    .create();
            String str = gson.toJson(ruleBeans);
            DocumentFile docFile = DocumentHelper.createFileIfNotExist("myTxtChapterRule.json", file);
            if (docFile != null) {
                DocumentHelper.writeString(str, docFile);
            }
        }
    }

    private void backupConfig(String file) {
        SharedPreferences pref = MApplication.getConfigPreferences();
        try (FileOutputStream out = new FileOutputStream(file + File.separator + "config.xml")) {
            XmlUtils.writeMapXml(pref.getAll(), out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
