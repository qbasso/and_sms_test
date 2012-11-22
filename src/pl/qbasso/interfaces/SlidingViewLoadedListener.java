/*
 * @author JPorzuczek
 */
package pl.qbasso.interfaces;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving slidingViewLoaded events.
 * The class that is interested in processing a slidingViewLoaded
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addSlidingViewLoadedListener<code> method. When
 * the slidingViewLoaded event occurs, that object's appropriate
 * method is invoked.
 *
 * @see SlidingViewLoadedEvent
 */
public interface SlidingViewLoadedListener {
	
	/**
	 * On view loaded.
	 */
	public void onViewLoaded();
}
