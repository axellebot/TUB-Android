package fr.bourgmapper.tub.presentation.view.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.bourgmapper.tub.R;
import fr.bourgmapper.tub.presentation.internal.di.components.DaggerUserActivityComponent;
import fr.bourgmapper.tub.presentation.internal.di.components.UserActivityComponent;
import fr.bourgmapper.tub.presentation.listener.MainNavigationListener;
import fr.bourgmapper.tub.presentation.navigation.MainNavigator;
import fr.bourgmapper.tub.presentation.presenter.MainActivityPresenter;
import fr.bourgmapper.tub.presentation.view.MenuView;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, MainNavigationListener, MaterialDialog.SingleButtonCallback,MenuView {
    private static final String TAG = "MainActivity";

    @Inject
    MainActivityPresenter mainActivityPresenter;

    @BindView(R.id.activity_main_drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.activity_main_navigation_drawer)
    NavigationView navigationView;

    @BindView(R.id.bottom_sheet_main)
    View bottomSheet;

    @BindView(R.id.activity_main_fab_menu)
    FloatingActionButton menuFloatingActionButton;

    private UserActivityComponent userActivityComponent;


    private ActionBarDrawerToggle drawerToggle;
    private BottomSheetBehavior bottomSheetBehavior;
    private MainNavigator navigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //injection
        this.initializeInjector();
        this.userActivityComponent.inject(this);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        navigator = new MainNavigator(this);

        this.initNavigationDrawer();
        this.initBottomSheet();

        navigationView.setNavigationItemSelectedListener(this);

        navigator.displayMapFragment();
        navigator.displayInfoFragmentOverview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mainActivityPresenter.destroy();
    }

    @Override
    public void onBackPressed() {
        navigator.onBackPressed();
    }

    @Override
    public void openMenu() {
        this.drawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void closeMenu() {
        this.drawerLayout.closeDrawer(GravityCompat.START, true);
    }

    @Override
    public Context context() {
        return getApplicationContext();
    }

    private void initNavigationDrawer() {
        this.menuFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    MainActivity.this.closeMenu();
                } else {
                    MainActivity.this.openMenu();
                }
            }
        });

        this.drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        this.drawerLayout.addDrawerListener(drawerToggle);
        this.drawerToggle.syncState();
    }

    private void initBottomSheet() {
        this.bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                checkFABMenu();
                checkStatusBarDim();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                checkFABMenu();
                checkStatusBarDim();
            }
        });
    }

    private void checkStatusBarDim() {
        int diff = getStatusBarHeight() - getBottomSheetPosition();

        if (diff >= 0) {
            dimStatusBar(false);
        } else {
            dimStatusBar(true);
        }
    }

    private void checkFABMenu() {
        int diff = (getFABMenuPosition() + this.menuFloatingActionButton.getHeight()) - getBottomSheetPosition();

        if (diff >= 0) {
            menuFloatingActionButton.hide();
        } else {
            menuFloatingActionButton.show();
        }
    }

    private int getStatusBarHeight() {
        Rect rect = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    private int getBottomSheetPosition() {
        int[] location = new int[2];
        this.bottomSheet.getLocationOnScreen(location);
        int bottomSheetPositionX = location[0];
        int bottomSheetPositionY = location[1];
        return bottomSheetPositionY;
    }

    private int getFABMenuPosition() {
        int[] location = new int[2];
        this.menuFloatingActionButton.getLocationOnScreen(location);
        int menuFloatingActionButtonPositionX = location[0];
        int menuFloatingActionButtonPositionY = location[1];
        return menuFloatingActionButtonPositionY;
    }

    private void dimStatusBar(boolean dim) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    dim ? Color.TRANSPARENT : ContextCompat.getColor(this, R.color.colorPrimary)
            );
        }
    }

    @OnClick(R.id.activity_main_fab_close)
    public void onCloseFABClicked(View view) {
        navigator.onBackPressed();
    }

    public BottomSheetBehavior getBottomSheetBehavior() {
        return bottomSheetBehavior;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_drawer_log_in:
                navigator.navigateToConnectionDialog();
                break;
            case R.id.nav_drawer_contact_us:
                navigator.navigateToContactIntent();
                break;
            case R.id.nav_drawer_share:
                navigator.navigateToShareIntent();
                break;
        }
        return false;
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

    }

    @Override
    public void onStopsButtonSelected() {
        navigator.navigateToStopList();
    }

    @Override
    public void onLinesButtonSelected() {
        navigator.navigateToLineList();
    }

    public void initializeInjector() {
        this.userActivityComponent = DaggerUserActivityComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();
    }


}
