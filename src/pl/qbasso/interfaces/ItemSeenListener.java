/*
 * @author JPorzuczek
 */
package pl.qbasso.interfaces;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving itemSeen events.
 * The class that is interested in processing a itemSeen
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addItemSeenListener<code> method. When
 * the itemSeen event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ItemSeenEvent
 */
public interface ItemSeenListener {
	
	/**
	 * On item seen.
	 *
	 * @param adapterId the adapter id
	 * @param messageId the message id
	 */
	public void onItemSeen(int adapterId, long messageId);
}
