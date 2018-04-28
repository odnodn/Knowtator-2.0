package edu.ucdenver.ccp.knowtator.events;

import edu.ucdenver.ccp.knowtator.model.Profile;

public class ProfileChangeEvent extends ChangeEvent<Profile> {

	public ProfileChangeEvent(Profile oldProfile, Profile newProfile) {
		super(oldProfile, newProfile);
	}
}