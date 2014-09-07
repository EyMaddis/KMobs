package de.kaleydra.kmobs.skills;

public class InvalidSkillException extends Exception {
	private static final long serialVersionUID = 1L;
	private String message;
	
	public InvalidSkillException(String message){
		this.message = message;
	}
	
	@Override
	public String getMessage(){
		return message;
	}
}
