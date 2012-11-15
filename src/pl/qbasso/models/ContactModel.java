package pl.qbasso.models;

public class ContactModel {

	private String displayName;
	private String phoneNumber;
	
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
