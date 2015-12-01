package info.blockchain.merchant.tabsswipe;
 
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
 
public class TabsPagerAdapter extends FragmentPagerAdapter {
 
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
    public Fragment getItem(int index) {
 
        switch (index) {
        	case 0:
        		return new PaymentFragment();
        	case 1:
        		return new TransactionsFragment();
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        return 2;
    }
 
}
