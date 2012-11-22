/*
 * @author JPorzuczek
 */
package pl.qbasso.models;

// TODO: Auto-generated Javadoc
/**
 * The Class ContactModel.
 */
public class ContactModel {

	/** The display name. */
	private String displayName;
	
	/** The phone number. */
	private String phoneNumber;
	
	/**
	 * Instantiates a new contact model.
	 *
	 * @param displayName the display name
	 * @param phoneNumber the phone number
	 */
	public ContactModel(String displayName, String phoneNumber) {
		this.displayName = displayName;
		this.phoneNumber = phoneNumber;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
}
