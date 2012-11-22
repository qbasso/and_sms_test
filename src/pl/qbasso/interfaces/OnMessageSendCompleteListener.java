/*
 * @author JPorzuczek
 */
package pl.qbasso.interfaces;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving onMessageSendComplete events.
 * The class that is interested in processing a onMessageSendComplete
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addOnMessageSendCompleteListener<code> method. When
 * the onMessageSendComplete event occurs, that object's appropriate
 * method is invoked.
 *
 * @see OnMessageSendCompleteEvent
 */
public interface OnMessageSendCompleteListener {

	/**
	 * Message send complete.
	 *
	 * @param success the success
	 */
	public void messageSendComplete(boolean success);

}
