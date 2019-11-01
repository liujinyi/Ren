package org.calf.reader.novel.presenter.contract;

import org.calf.basemvplib.impl.IPresenter;
import org.calf.basemvplib.impl.IView;
import org.calf.reader.novel.widget.recycler.expandable.bean.RecyclerViewData;

import java.util.List;

public interface FindBookContract {
    interface Presenter extends IPresenter {

        void initData();

    }

    interface View extends IView {

        void upData(List<RecyclerViewData> group);

    }
}
