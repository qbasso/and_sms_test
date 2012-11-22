/*
 * @author JPorzuczek
 */
package pl.qbasso.interfaces;

import android.os.Bundle;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving actionClick events.
 * The class that is interested in processing a actionClick
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addActionClickListener<code> method. When
 * the actionClick event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ActionClickEvent
 */
public interface ActionClickListener {

	/**
	 * On item click.
	 *
	 * @param pos the pos
	 * @param b the b
	 */
	void onItemClick(int pos, Bundle b);

}
