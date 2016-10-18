package com.looklook.xinghongfei.looklook;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.util.SimpleArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.looklook.xinghongfei.looklook.Activity.AboutActivity;
import com.looklook.xinghongfei.looklook.Activity.BaseActivity;
import com.looklook.xinghongfei.looklook.fragment.MeiziFragment;
import com.looklook.xinghongfei.looklook.fragment.TopNewsFragment;
import com.looklook.xinghongfei.looklook.fragment.ZhihuFragment;
import com.looklook.xinghongfei.looklook.util.AnimUtils;
import com.looklook.xinghongfei.looklook.util.SharePreferenceUtil;
import com.looklook.xinghongfei.looklook.util.ViewUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends BaseActivity {


    MenuItem currentMenuItem;
    Fragment currentFragment;

    @InjectView(R.id.fragment_container)
    FrameLayout mFragmentContainer;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.nav_view)
    NavigationView navView;
    @InjectView(R.id.drawer)
    DrawerLayout drawer;
    int nevigationId;

    SimpleArrayMap<Integer, String> mTitleArrayMap = new SimpleArrayMap<>();

    int mainColor;
    long exitTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            animateToolbar();
        }
        addfragmentsAndTitle();

//        setStatusColor();

//      没有发现任何作用....
        drawer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        /**
         * 选择Fragment
         */

        if (savedInstanceState == null) {
            nevigationId = SharePreferenceUtil.getNevigationItem(this);
            if (nevigationId != -1) {
                currentMenuItem = navView.getMenu().findItem(nevigationId);
            }
            if (currentMenuItem==null){
                currentMenuItem = navView.getMenu().findItem(R.id.zhihuitem);
            }
            if (currentMenuItem != null) {
                currentMenuItem.setChecked(true);
                // TODO: 16/8/17 add a fragment and set toolbar title
                Fragment fragment = getFragmentById(currentMenuItem.getItemId());
                String title = mTitleArrayMap.get((Integer) currentMenuItem.getItemId());
                if (fragment != null) {
                    switchFragment(fragment, title);
                }
            }
        } else {
            if (currentMenuItem!=null){
                Fragment fragment = getFragmentById(currentMenuItem.getItemId());
                String title = mTitleArrayMap.get((Integer) currentMenuItem.getItemId());
                if (fragment != null) {
                    switchFragment(fragment, title);
                }
            }else {
                switchFragment(new ZhihuFragment(), " ");
                currentMenuItem=navView.getMenu().findItem(R.id.zhihuitem);

            }
        }

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                if (currentMenuItem != item && currentMenuItem != null) {
                    currentMenuItem.setChecked(false);
                    int id = item.getItemId();
                    SharePreferenceUtil.putNevigationItem(MainActivity.this, id);
                    currentMenuItem = item;
                    currentMenuItem.setChecked(true);
                    switchFragment(getFragmentById(currentMenuItem.getItemId()),
                            mTitleArrayMap.get(currentMenuItem.getItemId()));
                }
                drawer.closeDrawer(GravityCompat.END, true);
                return true;
            }
        });

    /**
    *  onApplyWindowInsets这个方法用于自定义 fitsSystemWindows，
     * 用于调整状态栏,导航栏与自定义的控件的关系
     */
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
        drawer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // inset the toolbar down by the status bar height
                ViewGroup.MarginLayoutParams lpToolbar = (ViewGroup.MarginLayoutParams) toolbar
                        .getLayoutParams();
                lpToolbar.topMargin += insets.getSystemWindowInsetTop();
                lpToolbar.rightMargin += insets.getSystemWindowInsetRight();
                toolbar.setLayoutParams(lpToolbar);

                // inset the grid top by statusbar+toolbar & the bottom by the navbar (don't clip)
                mFragmentContainer.setPadding(mFragmentContainer.getPaddingLeft(),
                        insets.getSystemWindowInsetTop() + ViewUtils.getActionBarSize
                                (MainActivity.this),
                        mFragmentContainer.getPaddingRight() + insets.getSystemWindowInsetRight(), // landscape
                        mFragmentContainer.getPaddingBottom() + insets.getSystemWindowInsetBottom());

                // we place a background behind the status bar to combine with it's semi-transparent
                // color to get the desired appearance.  Set it's height to the status bar height
                View statusBarBackground = findViewById(R.id.status_bar_background);
                FrameLayout.LayoutParams lpStatus = (FrameLayout.LayoutParams)
                        statusBarBackground.getLayoutParams();
                lpStatus.height = insets.getSystemWindowInsetTop();
                statusBarBackground.setLayoutParams(lpStatus);

                // inset the filters list for the status bar / navbar
                // need to set the padding end for landscape case

                // clear this listener so insets aren't re-applied
                drawer.setOnApplyWindowInsetsListener(null);

                return insets.consumeSystemWindowInsets();
            }
        });
        }


//      设置导航栏文字,icon的颜色
        int[][] state = new int[][]{
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_checked}  // pressed
        };

        int[] color = new int[]{
                Color.BLACK,Color.BLACK};
        int[] iconcolor = new int[]{
                Color.GRAY,Color.BLACK};
        navView.setItemTextColor(new ColorStateList(state, color));
        navView.setItemIconTintList(new ColorStateList(state, iconcolor));

    }
    private void setStatusColor(){
        Bitmap bm = BitmapFactory.decodeResource(getResources(),
                R.drawable.nav_icon);
        Palette palette = Palette.generate(bm);
        if (palette.getLightVibrantSwatch() != null) {
            mainColor = palette.getLightVibrantSwatch().getRgb();
            getWindow().setStatusBarColor(palette.getLightVibrantSwatch().getRgb());
            toolbar.setBackgroundColor(palette.getLightVibrantSwatch().getRgb());
        }
    }

    private Fragment getFragmentById(int id) {
        Fragment fragment = null;
        switch (id) {
            case R.id.zhihuitem:
                fragment = new ZhihuFragment();
                break;
            case R.id.topnewsitem:
                fragment=new TopNewsFragment();
                break;
            case R.id.meiziitem:
                fragment=new MeiziFragment();
                break;

        }
        return fragment;
    }

//    key为菜单子项 value为String字符
    private void addfragmentsAndTitle() {
        mTitleArrayMap.put(R.id.zhihuitem, getResources().getString(R.string.zhihu));
        mTitleArrayMap.put(R.id.topnewsitem, getResources().getString(R.string.topnews));
        mTitleArrayMap.put(R.id.meiziitem, getResources().getString(R.string.meizi));

    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            if((System.currentTimeMillis()- exitTime)>2000){
                Toast.makeText(MainActivity.this, "再点一次，退出", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }else{
                super.onBackPressed();
            }
        }
    }

    private void switchFragment(Fragment fragment, String title) {

        if (currentFragment == null || !currentFragment.getClass().getName().equals(fragment.getClass().getName()))
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                    .commit();
        currentFragment = fragment;

    }

/**
 * 进入程序时ToolBar逐渐显示的动画效果
 */
    private void animateToolbar() {
        // this is gross but toolbar doesn't expose it's children to animate them :(
        View t = toolbar.getChildAt(0);
        if (t != null && t instanceof TextView) {
            TextView title = (TextView) t;

            // fade in and space out the title.  Animating the letterSpacing performs horribly so
            // fake it by setting the desired letterSpacing then animating the scaleX ¯\_(ツ)_/¯
            title.setAlpha(0f);
            title.setScaleX(0.8f);

            title.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .setStartDelay(500)
                    .setDuration(900)
                    .setInterpolator(AnimUtils.getFastOutSlowInInterpolator(this)).start();
        }
        View amv = toolbar.getChildAt(1);
        if (amv != null & amv instanceof ActionMenuView) {
            ActionMenuView actions = (ActionMenuView) amv;
            popAnim(actions.getChildAt(0), 500, 200); // filter
            popAnim(actions.getChildAt(1), 700, 200); // overflow
        }
    }

    private void popAnim(View v, int startDelay, int duration) {
        if (v != null) {
            v.setAlpha(0f);
            v.setScaleX(0f);
            v.setScaleY(0f);

            v.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(startDelay)
                    .setDuration(duration)
                    .setInterpolator(AnimationUtils.loadInterpolator(this,
                            android.R.interpolator.overshoot)).start();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_open:
                    drawer.openDrawer(GravityCompat.END);
                    break;
                case R.id.menu_about:
                    goAboutActivity();
                    break;
            }
            return true;
        }};

    private  void goAboutActivity(){
        Intent intent=new Intent(this, AboutActivity.class);
                this.startActivity(intent);
    }


    //    when recycle view scroll bottom,need loading more date and show the more view.
    //    此接口用于当RecyclerView滑到底部时加载数据并且显示更多视图
    public interface LoadingMore {

        void loadingStart();

        void loadingfinish();
    }

}



