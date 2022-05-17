package org.apache.camel.salesforce.dto;

public class Contact {

	private String lastName;
	private String screenName;

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setTwitterScreenName__c(String screenName) {
		this.screenName = screenName;
	}
}
