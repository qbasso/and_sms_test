package pl.qbasso.models;

import android.os.Bundle;

/**
 * @author Jakub.Porzuczek
 *
 */
public class ActionModel {

	private String title;
	private int resId;
	private int actionId;
	private Bundle actionData;

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
