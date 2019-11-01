package org.calf.reader.novel.presenter.contract;


import com.google.android.material.snackbar.Snackbar;
import org.calf.basemvplib.impl.IPresenter;
import org.calf.basemvplib.impl.IView;
import org.calf.reader.novel.bean.ReplaceRuleBean;

import java.util.List;

public interface ReplaceRuleContract {
    interface Presenter extends IPresenter {

        void saveData(List<ReplaceRuleBean> replaceRuleBeans);

        void delData(ReplaceRuleBean replaceRuleBean);

        void delData(List<ReplaceRuleBean> replaceRuleBeans);

        void importDataSLocal(String uri);

        void importDataS(String text);
    }

    interface View extends IView {

        void refresh();

        Snackbar getSnackBar(String msg, int length);

    }

}
