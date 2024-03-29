//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.calf.reader.novel.presenter;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import org.calf.basemvplib.BasePresenterImpl;
import org.calf.basemvplib.impl.IView;
import org.calf.reader.novel.DbHelper;
import org.calf.reader.novel.R;
import org.calf.reader.novel.base.observer.MyObserver;
import org.calf.reader.novel.bean.BookChapterBean;
import org.calf.reader.novel.bean.BookInfoBean;
import org.calf.reader.novel.bean.BookShelfBean;
import org.calf.reader.novel.bean.BookSourceBean;
import org.calf.reader.novel.constant.RxBusTag;
import org.calf.reader.novel.dao.BookSourceBeanDao;
import org.calf.reader.novel.help.BookshelfHelp;
import org.calf.reader.novel.help.DataBackup;
import org.calf.reader.novel.help.DataRestore;
import org.calf.reader.novel.model.WebBookModel;
import org.calf.reader.novel.presenter.contract.MainContract;
import org.calf.reader.novel.utils.RxUtils;
import org.calf.reader.novel.utils.StringUtils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainPresenter extends BasePresenterImpl<MainContract.View> implements MainContract.Presenter {

    @Override
    public void addBookUrl(String bookUrls) {
        bookUrls = bookUrls.trim();
        if (TextUtils.isEmpty(bookUrls)) return;

        String[] urls = bookUrls.split("\\n");

        Observable.fromArray(urls)
                .flatMap(this::addBookUrlO)
                .compose(RxUtils::toSimpleSingle)
                .subscribe(new MyObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean bookShelfBean) {
                        getBook(bookShelfBean);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast(e.getMessage());
                    }
                });
    }

    private Observable<BookShelfBean> addBookUrlO(String bookUrl) {
        return Observable.create(e -> {
            if (StringUtils.isTrimEmpty(bookUrl)) {
                e.onComplete();
                return;
            }
            BookInfoBean temp = DbHelper.getDaoSession().getBookInfoBeanDao().load(bookUrl);
            if (temp != null) {
                e.onError(new Throwable("已在书架中"));
                return;
            } else {
                String baseUrl = StringUtils.getBaseUrl(bookUrl);
                BookSourceBean bookSourceBean = DbHelper.getDaoSession().getBookSourceBeanDao().load(baseUrl);
                if (bookSourceBean == null) {
                    List<BookSourceBean> sourceBeans = DbHelper.getDaoSession().getBookSourceBeanDao().queryBuilder()
                            .where(BookSourceBeanDao.Properties.RuleBookUrlPattern.isNotNull(), BookSourceBeanDao.Properties.RuleBookUrlPattern.notEq("")).list();
                    for (BookSourceBean sourceBean : sourceBeans) {
                        if (bookUrl.matches(sourceBean.getRuleBookUrlPattern())) {
                            bookSourceBean = sourceBean;
                            break;
                        }
                    }
                }
                if (bookSourceBean != null) {
                    BookShelfBean bookShelfBean = new BookShelfBean();
                    bookShelfBean.setTag(bookSourceBean.getBookSourceUrl());
                    bookShelfBean.setNoteUrl(bookUrl);
                    bookShelfBean.setDurChapter(0);
                    bookShelfBean.setGroup(mView.getGroup() % 4);
                    bookShelfBean.setDurChapterPage(0);
                    bookShelfBean.setFinalDate(System.currentTimeMillis());
                    e.onNext(bookShelfBean);
                } else {
                    e.onError(new Throwable("未找到对应书源"));
                    return;
                }
            }
            e.onComplete();
        });
    }

    private void getBook(BookShelfBean bookShelfBean) {
        WebBookModel.getInstance()
                .getBookInfo(bookShelfBean)
                .flatMap(bookShelfBean1 -> WebBookModel.getInstance().getChapterList(bookShelfBean1))
                .flatMap(chapterBeanList -> saveBookToShelfO(bookShelfBean, chapterBeanList))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MyObserver<BookShelfBean>() {
                    @Override
                    public void onNext(BookShelfBean value) {
                        if (value.getBookInfoBean().getChapterUrl() == null) {
                            mView.toast("添加书籍失败");
                        } else {
                            //成功   //发送RxBus
                            RxBus.get().post(RxBusTag.HAD_ADD_BOOK, bookShelfBean);
                            mView.toast("添加书籍成功");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.toast("添加书籍失败" + e.getMessage());
                    }
                });
    }

    /**
     * 保存数据
     */
    private Observable<BookShelfBean> saveBookToShelfO(BookShelfBean bookShelfBean, List<BookChapterBean> chapterBeanList) {
        return Observable.create(e -> {
            BookshelfHelp.saveBookToShelf(bookShelfBean);
            DbHelper.getDaoSession().getBookChapterBeanDao().insertOrReplaceInTx(chapterBeanList);
            e.onNext(bookShelfBean);
            e.onComplete();
        });
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void attachView(@NonNull IView iView) {
        super.attachView(iView);
        RxBus.get().register(this);
    }

    @Override
    public void detachView() {
        RxBus.get().unregister(this);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.IMMERSION_CHANGE)})
    public void initImmersionBar(Boolean immersion) {
        mView.initImmersionBar();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.RECREATE)})
    public void recreate(Boolean recreate) {
        mView.recreate();
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(RxBusTag.AUTO_BACKUP)})
    public void autoBackup(Boolean backup) {
        DataBackup.getInstance().autoSave();
    }
}