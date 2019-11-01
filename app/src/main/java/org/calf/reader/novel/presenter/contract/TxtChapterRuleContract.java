package org.calf.reader.novel.presenter.contract;

import com.google.android.material.snackbar.Snackbar;
import org.calf.basemvplib.impl.IPresenter;
import org.calf.basemvplib.impl.IView;
import org.calf.reader.novel.bean.TxtChapterRuleBean;

import java.util.List;

public interface TxtChapterRuleContract {
    interface Presenter extends IPresenter {

        void saveData(List<TxtChapterRuleBean> txtChapterRuleBeans);

        void delData(TxtChapterRuleBean txtChapterRuleBean);

        void delData(List<TxtChapterRuleBean> txtChapterRuleBeans);

        void importDataSLocal(String uri);

        void importDataS(String text);
    }

    interface View extends IView {

        void refresh();

        Snackbar getSnackBar(String msg, int length);

    }
}
