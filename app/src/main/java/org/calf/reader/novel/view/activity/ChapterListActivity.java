package org.calf.reader.novel.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.hwangjr.rxbus.RxBus;
import org.calf.basemvplib.BitIntentDataManager;
import org.calf.basemvplib.impl.IPresenter;
import org.calf.reader.novel.R;
import org.calf.reader.novel.base.BaseTabActivity;
import org.calf.reader.novel.base.MBaseActivity;
import org.calf.reader.novel.bean.BookChapterBean;
import org.calf.reader.novel.bean.BookShelfBean;
import org.calf.reader.novel.help.ReadBookControl;
import org.calf.reader.novel.utils.ColorUtil;
import org.calf.reader.novel.utils.theme.ATH;
import org.calf.reader.novel.utils.theme.MaterialValueHelper;
import org.calf.reader.novel.utils.theme.ThemeStore;
import org.calf.reader.novel.view.fragment.BookmarkFragment;
import org.calf.reader.novel.view.fragment.ChapterListFragment;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ChapterListActivity extends BaseTabActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ReadBookControl readBookControl = ReadBookControl.getInstance();
    private SearchView searchView;
    private BookShelfBean bookShelf;
    private List<BookChapterBean> chapterBeanList;

    public static void startThis(MBaseActivity activity, BookShelfBean bookShelf, List<BookChapterBean> chapterBeanList) {
        Intent intent = new Intent(activity, ChapterListActivity.class);
        String key = String.valueOf(System.currentTimeMillis());
        String bookKey = "book" + key;
        intent.putExtra("bookKey", bookKey);
        BitIntentDataManager.getInstance().putData(bookKey, bookShelf.clone());
        String chapterListKey = "chapterList" + key;
        intent.putExtra("chapterListKey", chapterListKey);
        BitIntentDataManager.getInstance().putData(chapterListKey, chapterBeanList);
        activity.startActivity(intent);
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setOrientation(readBookControl.getScreenDirection());
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (bookShelf != null) {
            String key = String.valueOf(System.currentTimeMillis());
            String bookKey = "book" + key;
            getIntent().putExtra("bookKey", bookKey);
            BitIntentDataManager.getInstance().putData(bookKey, bookShelf.clone());
            String chapterListKey = "chapterList" + key;
            getIntent().putExtra("chapterListKey", chapterListKey);
            BitIntentDataManager.getInstance().putData(chapterListKey, chapterBeanList);
        }
    }

    @Override
    protected void onDestroy() {
        RxBus.get().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onCreateActivity() {
        getWindow().getDecorView().setBackgroundColor(ThemeStore.backgroundColor(this));
        setContentView(R.layout.activity_chapterlist);
        ButterKnife.bind(this);
        setupActionBar();
        mTlIndicator.setSelectedTabIndicatorColor(ThemeStore.accentColor(this));
        mTlIndicator.setTabTextColors(ColorUtil.isColorLight(ThemeStore.primaryColor(this)) ? Color.BLACK : Color.WHITE,
                ThemeStore.accentColor(this));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initData() {
        String bookKey = getIntent().getStringExtra("bookKey");
        bookShelf = (BookShelfBean) BitIntentDataManager.getInstance().getData(bookKey);
        String chapterListKey = getIntent().getStringExtra("chapterListKey");
        chapterBeanList = (List<BookChapterBean>) BitIntentDataManager.getInstance().getData(chapterListKey);
    }

    /**************abstract***********/
    @Override
    protected List<Fragment> createTabFragments() {
        ChapterListFragment chapterListFragment = new ChapterListFragment();
        BookmarkFragment bookmarkFragment = new BookmarkFragment();
        return Arrays.asList(chapterListFragment, bookmarkFragment);
    }

    @Override
    protected List<String> createTabTitles() {
        return Arrays.asList(getString(R.string.chapter_list), getString(R.string.bookmark));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_view, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        searchView = (SearchView) search.getActionView();
        ATH.setTint(searchView, MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(ThemeStore.primaryColor(this))));
        searchView.setMaxWidth(getResources().getDisplayMetrics().widthPixels);
        searchView.onActionViewCollapsed();
        searchView.setOnCloseListener(() -> {
            mTlIndicator.setVisibility(VISIBLE);
            return false;
        });
        searchView.setOnSearchClickListener(view -> mTlIndicator.setVisibility(GONE));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mTlIndicator.getSelectedTabPosition() == 1) {
                    ((BookmarkFragment) mFragmentList.get(1)).startSearch(newText);
                } else {
                    ((ChapterListFragment) mFragmentList.get(0)).startSearch(newText);
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mTlIndicator.getVisibility() != VISIBLE) {
            searchViewCollapsed();
        }
        finish();
    }

    //设置ToolBar
    private void setupActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public BookShelfBean getBookShelf() {
        return bookShelf;
    }

    public List<BookChapterBean> getChapterBeanList() {
        return chapterBeanList;
    }

    public void searchViewCollapsed() {
        searchView.onActionViewCollapsed();
        mTlIndicator.setVisibility(VISIBLE);
    }

}
