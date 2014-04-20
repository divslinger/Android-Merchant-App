package info.blockchain.merchant;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
//import android.util.Log;

import info.blockchain.merchant.tabsswipe.TabsPagerAdapter;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

	private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;

    private String[] tabs = null;

    private static int SETTINGS_ACTIVITY 	= 1;
    private static int PIN_ACTIVITY 		= 2;
    private static int RESET_PIN_ACTIVITY 	= 3;
    private static int ABOUT_ACTIVITY 	= 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    
	    tabs = new String[2];
	    tabs[0] = getString(R.string.tab_payment);
	    tabs[1] = getString(R.string.tab_transactions);

        viewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);

        actionBar = getActionBar();
//        actionBar.hide();
//        actionBar.setTitle("");
        actionBar.setDisplayOptions(actionBar.getDisplayOptions() ^ ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setLogo(R.drawable.masthead);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF1B8AC7")));
//        actionBar.show();
        
        for (String tab : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab).setTabListener(this));

            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
     
                @Override
                public void onPageSelected(int position) {
                    actionBar.setSelectedNavigationItem(position);
                }
     
                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) { ; }
     
                @Override
                public void onPageScrollStateChanged(int arg0) { ; }
            });
        }

        // no PIN ?, then create one
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String pin = prefs.getString("pin", "");
        if(pin.equals("")) {
        	doPIN();
        }

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.action_settings:
	    		doSettings(false);
	    		return true;
	    	case R.id.action_newpin:
	            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	            String pin = prefs.getString("pin", "");
	            if(pin.equals("")) {
		    		doPIN();
	            }
	            else {
		    		resetPIN();
	            }
	    		return true;
	    	case R.id.action_about:
	    		doAbout();
	    		return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == SETTINGS_ACTIVITY && resultCode == RESULT_OK) {
			;
		}
		else if(requestCode == PIN_ACTIVITY && resultCode == RESULT_OK) {
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String strOtherCurrency = prefs.getString("ocurrency", "");
    		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
    		intent.putExtra("ocurrency", strOtherCurrency);
    		startActivityForResult(intent, SETTINGS_ACTIVITY);
		}
		else if(requestCode == RESET_PIN_ACTIVITY && resultCode == RESULT_OK) {
			doPIN();
		}
		else {
			;
		}
		
	}

	@Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) { ; }
 
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) { viewPager.setCurrentItem(tab.getPosition()); }
 
    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) { ; }
    
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);

        View view = this.getCurrentFocus();

        if (view instanceof EditText) {
            View w = this.getCurrentFocus();
            int scrcoords[] = new int[2];
            w.getLocationOnScreen(scrcoords);
            float x = event.getRawX() + w.getLeft() - scrcoords[0];
            float y = event.getRawY() + w.getTop() - scrcoords[1];
            
            if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) { 
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
            }
        }

        return ret;
    }

    private void doSettings(final boolean create)	{
    	if(create)	{
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String strOtherCurrency = prefs.getString("ocurrency", "");
    		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
    		intent.putExtra("ocurrency", strOtherCurrency);
    		startActivityForResult(intent, SETTINGS_ACTIVITY);
    	}
    	else	{
    		enterPIN();
    	}
    }

    private void doPIN()	{
		Intent intent = new Intent(MainActivity.this, PinActivity.class);
		intent.putExtra("create", true);
		startActivityForResult(intent, PIN_ACTIVITY);
    }

    private void enterPIN()	{
		Intent intent = new Intent(MainActivity.this, PinActivity.class);
		intent.putExtra("create", false);
		startActivityForResult(intent, PIN_ACTIVITY);
    }

    private void resetPIN()	{
		Intent intent = new Intent(MainActivity.this, PinActivity.class);
		intent.putExtra("create", false);
		startActivityForResult(intent, RESET_PIN_ACTIVITY);
    }

    private void doAbout()	{
    	Intent intent = new Intent(MainActivity.this, AboutActivity.class);
		startActivityForResult(intent, ABOUT_ACTIVITY);
    }

}
