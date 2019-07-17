package com.bitcoin.merchant.app.screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bitcoin.merchant.app.R;
import com.bitcoin.merchant.app.database.DBControllerV3;
import com.bitcoin.merchant.app.util.DateUtil;
import com.bitcoin.merchant.app.util.MonetaryUtil;
import com.bitcoin.merchant.app.util.PrefsUtil;
import com.crashlytics.android.Crashlytics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.CLIPBOARD_SERVICE;

public class TransactionsHistoryFragment extends Fragment {
    private static final String TAG = "TransactionsHistory";
    private static String merchantXpub = null;
    private List<ContentValues> mListItems;
    private TransactionAdapter adapter = null;
    private boolean push_notifications = false;
    private Timer timer = null;
    private ListView listView = null;
    private SwipeRefreshLayout swipeLayout = null;
    private Activity thisActivity = null;
    private volatile boolean ready;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ready = true;
        View rootView = inflater.inflate(getResources().getLayout(R.layout.fragment_transaction), container, false);
        initListView(rootView);
        merchantXpub = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
        push_notifications = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_PUSH_NOTIFS, false);
        swipeLayout = rootView.findViewById(R.id.swipe_container);
        swipeLayout.setProgressViewEndTarget(false, (int) (getResources().getDisplayMetrics().density * (72 + 20)));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new GetDataTask().execute();
            }
        });
        swipeLayout.setColorScheme(R.color.blockchain_darker_green,
                R.color.blockchain_green,
                R.color.blockchain_darkest_green);
        thisActivity = getActivity();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        ready = false;
        super.onDestroyView();
    }

    private void initListView(View rootView) {
        mListItems = new ArrayList<>();
        adapter = new TransactionAdapter();
        listView = rootView.findViewById(R.id.txList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showTransactionMenu(id);
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            merchantXpub = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
            push_notifications = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_PUSH_NOTIFS, false);
            if (push_notifications) {
                if (timer == null) {
                    timer = new Timer();
                    try {
                        timer.scheduleAtFixedRate(new TimerTask() {
                            public void run() {
                                new GetDataTask().execute();
                            }
                        }, 500L, 1000L * 60L * 2L);
                    } catch (IllegalStateException ise) {
                        ;
                    } catch (IllegalArgumentException iae) {
                        ;
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        merchantXpub = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_MERCHANT_RECEIVER, "");
        push_notifications = PrefsUtil.getInstance(getActivity()).getValue(PrefsUtil.MERCHANT_KEY_PUSH_NOTIFS, false);
        new GetDataTask().execute();
    }

    private void showTransactionMenu(final long item) {
        final ContentValues val = mListItems.get((int) item);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.ic_launcher);
        builder.setItems(new CharSequence[]{
                        "View address",
                        "View transaction",
                        "Copy transaction"
                },
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0: {
                                String address = val.getAsString("iad");
                                openExplorer("https://explorer.bitcoin.com/bch/address/" + address);
                                break;
                            }
                            case 1: {
                                String tx = val.getAsString("tx");
                                openExplorer("https://explorer.bitcoin.com/bch/tx/" + tx);
                                break;
                            }
                            case 2: {
                                copyToClipboard(val.getAsString("tx"));
                                break;
                            }
                        }
                    }
                });
        builder.create().show();
    }

    private void openExplorer(String uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    private void copyToClipboard(String text) {
        final ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            ClipData clipData = ClipData.newPlainText("Source Text", text);
            clipboardManager.setPrimaryClip(clipData);
        }
    }

    protected boolean isSafe() {
        if (mListItems == null || thisActivity == null || adapter == null) {
            return false; // view not yet created
        }
        return !(this.isRemoving() || this.getActivity() == null || this.isDetached() || !this.isAdded() || this.getView() == null);
    }

    public void addTx(ContentValues val) {
        try {
            if (!isSafe()) {
                return;
            }
            mListItems.add(0, val);
            thisActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isSafe()) {
                        return;
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "addTx", e);
            Crashlytics.logException(e);
        }
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... params) {
            if (!ready) {
                return null;
            }
            thisActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });
            if ((merchantXpub != null) && (merchantXpub.length() > 0)) {
                // get updated list from database
                try {
                    ArrayList<ContentValues> vals = new DBControllerV3(getActivity()).getAllPayments();
                    if (vals.size() > 0) {
                        mListItems.clear();
                        mListItems.addAll(vals);
                        thisActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "getAllPayments", e);
                    Crashlytics.logException(e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (ready) {
                thisActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                });
            }
            super.onPostExecute(result);
        }
    }

    private class TransactionAdapter extends BaseAdapter {
        private LayoutInflater inflater = null;
        private SimpleDateFormat sdf = null;

        TransactionAdapter() {
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mListItems.size();
        }

        @Override
        public String getItem(int position) {
            return mListItems.get(position).getAsString("iad");
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = inflater.inflate(R.layout.list_item_transaction, parent, false);
            } else {
                view = convertView;
            }
            ContentValues vals = mListItems.get(position);
            String date_str = DateUtil.getInstance().formatted(vals.getAsLong("ts"));
            SpannableStringBuilder ds = new SpannableStringBuilder(date_str);
            int idx = date_str.indexOf("@");
            if (idx != -1) {
                ds.setSpan(new StyleSpan(Typeface.NORMAL), 0, idx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                ds.setSpan(new RelativeSizeSpan(0.75f), idx, date_str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            TextView tvDate = view.findViewById(R.id.tv_date);
            tvDate.setText(ds);
            tvDate.setAlpha(0.7f);
            // display coin value
            long amount = Math.abs(vals.getAsLong("amt"));
            SpannableStringBuilder coinSpan = new SpannableStringBuilder(getActivity().getResources().getString(R.string.bitcoin_currency_symbol));
            coinSpan.setSpan(new RelativeSizeSpan((float) 0.75), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            TextView coinAmount = view.findViewById(R.id.tv_amount_coin);
            String displayValue = MonetaryUtil.getInstance(getActivity()).getDisplayAmountWithFormatting(amount);
            coinAmount.setText(displayValue + " " + coinSpan);
            // display fiat value
            SpannableStringBuilder fiatSpan = new SpannableStringBuilder(vals.getAsString("famt").subSequence(0, 1));
            fiatSpan.setSpan(new RelativeSizeSpan((float) 0.75), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            TextView fiatAmount = view.findViewById(R.id.tv_amount_fiat);
            fiatAmount.setText(fiatSpan + " " + vals.getAsString("famt").substring(1));
            if (vals.getAsLong("amt") < 0L) {
                ImageView ivStatus = (ImageView) view.findViewById(R.id.iv_status);
                ivStatus.setImageResource(R.drawable.ic_warning_black_18dp);
            }
            return view;
        }
    }
}
