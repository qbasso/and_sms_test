/*
 * @author JPorzuczek
 */
package pl.qbasso.models;

import android.os.Bundle;

// TODO: Auto-generated Javadoc
/**
 * The Class ActionModel.
 *
 * @author Jakub.Porzuczek
 */
public class ActionModel {

	/** The title. */
	private String title;
	
	/** The res id. */
	private int resId;
	
	/** The action id. */
	private int actionId;
	
	/** The action data. */
	private Bundle actionData;

	/**
	 * Instantiates a new action model.
	 *
	 * @param title the title
	 * @param resId the res id
	 * @param actionId the action id
	 * @param b the b
	 */
	public ActionModel(String title, int resId, int actionId, Bundle b) {
		this.title = title;
		this.resId = resId;
		this.actionId = actionId;
		this.actionData = b;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		this.resId = resId;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	public Bundle getActionData() {
		return actionData;
	}

	public void setActionData(Bundle actionData) {
		this.actionData = actionData;
	}

}
