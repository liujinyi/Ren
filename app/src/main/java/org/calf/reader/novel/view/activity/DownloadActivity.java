package org.calf.reader.novel.view.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.calf.basemvplib.impl.IPresenter;
import org.calf.reader.novel.R;
import org.calf.reader.novel.base.MBaseActivity;
import org.calf.reader.novel.bean.DownloadBookBean;
import org.calf.reader.novel.service.DownloadService;
import org.calf.reader.novel.utils.theme.ThemeStore;
import org.calf.reader.novel.view.adapter.DownloadAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadActivity extends MBaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.ll_content)
    LinearLayout llContent;

    private DownloadAdapter adapter;
    private DownloadReceiver receiver;

    public static void startThis(Activity activity) {
        Intent intent = new Intent(activity, DownloadActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    /**
     * P层绑定   若无则返回null;
     */
    @Override
    protected IPresenter initInjector() {
        return null;
    }

    /**
     * 布局载入  setContentView()
     */
    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.activity_recycler_vew);
        ButterKnife.bind(this);
        this.setSupportActionBar(toolbar);
        setupActionBar();
    }

    /**
     * 数据初始化
     */
    @Override
    protected void initData() {
        receiver = new DownloadReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.addDownloadAction);
        filter.addAction(DownloadService.removeDownloadAction);
        filter.addAction(DownloadService.progressDownloadAction);
        filter.addAction(DownloadService.obtainDownloadListAction);
        filter.addAction(DownloadService.finishDownloadAction);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void bindView() {
        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DownloadAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        DownloadService.obtainDownloadList(this);
    }

    //设置ToolBar
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.download_offline);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_download, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_cancel:
                DownloadService.cancelDownload(this);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class DownloadReceiver extends BroadcastReceiver {

        WeakReference<DownloadActivity> ref;

        public DownloadReceiver(DownloadActivity activity) {
            this.ref = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadAdapter adapter = ref.get().adapter;
            if (adapter == null || intent == null) {
                return;
            }
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case DownloadService.addDownloadAction:
                        DownloadBookBean downloadBook = intent.getParcelableExtra("downloadBook");
                        adapter.addData(downloadBook);
                        break;
                    case DownloadService.removeDownloadAction:
                        downloadBook = intent.getParcelableExtra("downloadBook");
                        adapter.removeData(downloadBook);
                        break;
                    case DownloadService.progressDownloadAction:
                        downloadBook = intent.getParcelableExtra("downloadBook");
                        adapter.upData(downloadBook);
                        break;
                    case DownloadService.finishDownloadAction:
                        adapter.upDataS(null);
                        break;
                    case DownloadService.obtainDownloadListAction:
                        ArrayList<DownloadBookBean> downloadBooks = intent.getParcelableArrayListExtra("downloadBooks");
                        adapter.upDataS(downloadBooks);
                        break;

                }
            }
        }
    }
}
