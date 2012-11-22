/*
 * @author JPorzuczek
 */
package pl.qbasso.interfaces;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving smsDraftAvailable events.
 * The class that is interested in processing a smsDraftAvailable
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addSmsDraftAvailableListener<code> method. When
 * the smsDraftAvailable event occurs, that object's appropriate
 * method is invoked.
 *
 * @see SmsDraftAvailableEvent
 */
public interface SmsDraftAvailableListener {
	
	/**
	 * Draft text available.
	 *
	 * @param text the text
	 * @param position the position
	 */
	public void draftTextAvailable(String text, int position);
}
