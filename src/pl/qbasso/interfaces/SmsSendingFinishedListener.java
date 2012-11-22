/*
 * @author JPorzuczek
 */
package pl.qbasso.interfaces;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving smsSendingFinished events.
 * The class that is interested in processing a smsSendingFinished
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addSmsSendingFinishedListener<code> method. When
 * the smsSendingFinished event occurs, that object's appropriate
 * method is invoked.
 *
 * @see SmsSendingFinishedEvent
 */
public interface SmsSendingFinishedListener {
	
	/**
	 * On sms sent.
	 */
	public void onSmsSent();
}
