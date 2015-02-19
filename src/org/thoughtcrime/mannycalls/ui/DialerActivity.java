/*
 * Copyright (C) 2011 Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.thoughtcrime.mannycalls.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.thoughtcrime.mannycalls.Constants;
import org.thoughtcrime.mannycalls.R;
import org.thoughtcrime.mannycalls.directory.DirectoryUpdateReceiver;
import org.thoughtcrime.mannycalls.gcm.GCMRegistrarHelper;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import org.thoughtcrime.mannycalls.util.PeriodicActionUtils;

import java.util.List;

/**
 * The base dialer activity.  A tab container for the contacts, call log, and favorites tab.
 * @author Moxie Marlinspike
 */

public class DialerActivity extends SherlockFragmentActivity {

  public static final int    MISSED_CALL     = 1;
  public static final String CALL_LOG_ACTION = "org.thoughtcrime.redphone.ui.DialerActivity";

  private static final int CALL_LOG_TAB_INDEX = 1;

  private ViewPager viewPager;
  private Button home_btn;

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    ActionBar actionBar = this.getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    actionBar.setDisplayShowHomeEnabled(false);
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setDisplayUseLogoEnabled(false);

    checkForFreshInstall();
    setContentView(R.layout.dialer_activity);
    home_btn = (Button)findViewById(R.id.home_icon_color);

    setupViewPager();
    setupTabs();
    onClikListeners();

    GCMRegistrarHelper.registerClient(this, false);
  }

  @Override
  public void onNewIntent(Intent intent) {
    setIntent(intent);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (getIntent().getAction() != null &&
        getIntent().getAction().equals(CALL_LOG_ACTION))
    {
      getIntent().setAction(null);
      getSupportActionBar().setSelectedNavigationItem(CALL_LOG_TAB_INDEX);
    }
  }

  private void setupViewPager() {
    viewPager = (ViewPager) findViewById(R.id.pager);
    viewPager.setAdapter(new DialerPagerAdapter(getSupportFragmentManager()));
    viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int i, float v, int i2) {
      }
      @Override
      public void onPageSelected(int i) {
        getSupportActionBar().setSelectedNavigationItem(i);
      }

      @Override
      public void onPageScrollStateChanged(int i) {
      }
    });
  }

  private void setupTabs() {
    int[] icons = new int[] { R.drawable.last_calls_ico, R.drawable.dial_ico, R.drawable.contacts_ico, R.drawable.favorites_ico };
    for (int i = 0; i < icons.length; i++) {
      ActionBar.Tab tab = getSupportActionBar().newTab();
      tab.setIcon(icons[i]);

      final int tabIndex = i;
      tab.setTabListener(new TabListener() {
        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
          viewPager.setCurrentItem(tabIndex);
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
      });
      getSupportActionBar().addTab(tab);
    }
  }
  private void checkForFreshInstall() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

    if (preferences.getBoolean(Constants.VERIFYING_PREFERENCE, false)) {
      Log.w("DialerActivity", "Verification underway...");
      startActivity(new Intent(this, RegistrationProgressActivity.class));
      finish();
    }

    if (!preferences.getBoolean(Constants.REGISTERED_PREFERENCE, false)) {
      Log.w("DialerActivity", "Not registered and not verifying...");
      startActivity(new Intent(this, CreateAccountActivity.class));
      finish();
    }
    PeriodicActionUtils.scheduleUpdate(this, DirectoryUpdateReceiver.class);
  }

//  @Override
//  public boolean onCreateOptionsMenu(Menu menu) {
//    MenuInflater inflater = this.getSupportMenuInflater();
//    inflater.inflate(R.menu.dialer_options_menu, menu);
//    return true;
//  }

//  @Override
//  public boolean onOptionsItemSelected(MenuItem item) {
//    switch (item.getItemId()) {
//    case R.id.resetPasswordItem: launchResetPasswordActivity();  return true;
//    case R.id.aboutItem:         launchAboutActivity();          return true;
//    case R.id.settingsItem:      launchPreferencesActivity();    return true;
//    }
//    return false;
//  }

//  private void launchPreferencesActivity() {
//    startActivity(new Intent(this, ApplicationPreferencesActivity.class));
//  }
//
//  private void launchResetPasswordActivity() {
//    startActivity(new Intent(this, CreateAccountActivity.class));
//    finish();
//  }
//
//  private void launchAboutActivity() {
//    startActivity(new Intent(this, AboutActivity.class));
//  }

  private static class DialerPagerAdapter extends FragmentPagerAdapter {
    public DialerPagerAdapter(FragmentManager fm) {
      super(fm);
    }
      @Override
      public Fragment getItem(int i) {
          switch (i) {
              case 0:
                  return new RecentCallListActivity();
              case 1:
                  return new DialPadActivity();
              case 2:
                  return new ContactsListActivity();
              case 3:
                  ContactsListActivity fragment = new ContactsListActivity();
                  Bundle args = new Bundle();
                  args.putBoolean("favorites", true);
                  fragment.setArguments(args);
                  return fragment;
              default:
                  return new RecentCallListActivity();
          }
      }
      @Override
      public int getCount() {
        return 4;
    }
  }

  public void onClikListeners(){
      home_btn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              int flags = PackageManager.GET_META_DATA | PackageManager.GET_SHARED_LIBRARY_FILES | PackageManager.GET_UNINSTALLED_PACKAGES;
              List<ApplicationInfo> appsList = getApplicationContext().getPackageManager().getInstalledApplications(flags);
              ApplicationInfo appInfo = null;
              for (int i = 0; i < appsList.size(); i++)
                  if (appsList.get(i).toString().contains("com.mannywilson")) {appInfo = appsList.get(i); break; }
              try {
                  if (appInfo != null) {
                      Intent intent = new Intent();
                      intent.setClassName(appInfo.packageName, appInfo.packageName+".account.SplashScreenActivity");
                      startActivity(intent);
                  }
              }catch (ActivityNotFoundException e) {
                  Log.v("WelcomeScreenActivity.class", e.toString());
              } catch (Exception e) {
                  Log.v("WelcomeScreenActivity.class", e.toString());
              }
          }
      });
  }

}
