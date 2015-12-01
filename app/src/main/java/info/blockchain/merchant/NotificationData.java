package info.blockchain.merchant;

import android.content.Context;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

public class NotificationData {

    public static NotificationManager mNotificationManager;
    public int notification_id = 0;
    private Context context = null;

	private String contentMarquee = null;
	private String contentTitle = null;
	private String contentText = null;
	private int drawable = 0;
	private Class intentClass = null;

    public NotificationData(Context ctx, String title, String marquee, String text, int draw, Class cls, int id) {
        context = ctx;
    	contentMarquee = marquee;
    	contentTitle = title;
    	contentText = text;
    	drawable = draw;
    	intentClass = cls;
    	notification_id = id;
    }

    public void clearNotification() {
        mNotificationManager.cancel(notification_id);
    }

    public void setNotification() {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification notifyDetails = new Notification(drawable, contentMarquee, System.currentTimeMillis());

        Intent notifyIntent = new Intent(context,  intentClass);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notifyIntent, android.content.Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        notifyDetails.setLatestEventInfo(context, contentTitle, contentText, intent);
        mNotificationManager.notify(notification_id, notifyDetails);
    }
}
