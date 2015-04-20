package com.example.alexander.robotop.modell;

public class NoSpaceToMoveException extends Exception{

	private static final long serialVersionUID = 1L;

	public NoSpaceToMoveException()
	{
	}

	public NoSpaceToMoveException( String s )
	{
		super( s );
	}
}