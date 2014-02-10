package info.blockchain.merchant;

import com.dm.zbar.android.scanner.ZBarConstants;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.widget.Toast;

import info.blockchain.merchant.tabsswipe.TabsPagerAdapter;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    private String[] tabs = {
    		"Settings",
    		"Payment",
    		"Transactions"
    		};
    private boolean firstLaunch = false;
	private CurrencyExchange ce = null;
	private Activity thisActivity = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
 
        viewPager.setAdapter(mAdapter);
//        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        

        for (String tab : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab).setTabListener(this));

            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
     
                @Override
                public void onPageSelected(int position) {
                    actionBar.setSelectedNavigationItem(position);
                }
     
                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {
                }
     
                @Override
                public void onPageScrollStateChanged(int arg0) {
                }
            });
        }
        
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        firstLaunch = prefs.getBoolean("firstLaunch", true);
        if(firstLaunch) {
	        SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstLaunch", false);
            editor.commit();
        }
        if(!firstLaunch) {
            viewPager.setCurrentItem(1, true);
        }
        
        ce = CurrencyExchange.getInstance();
        
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK)	{
			// Scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT)
			// Type of the scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
			Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();
			Toast.makeText(this, "Scan Result Type = " + data.getIntExtra(ZBarConstants.SCAN_RESULT_TYPE, 0), Toast.LENGTH_SHORT).show();
			// The value of type indicates one of the symbols listed in Advanced Options below.
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(thisActivity);
			SharedPreferences.Editor editor = prefs.edit();
            editor.putString("receiving_address", data.getStringExtra(ZBarConstants.SCAN_RESULT));
            editor.commit();
			
        } else if(resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
 
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }
 
    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

}
