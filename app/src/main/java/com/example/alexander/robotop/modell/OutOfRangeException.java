package com.example.alexander.robotop.modell;

public class OutOfRangeException extends Exception{

	private static final long serialVersionUID = 1L;

	public OutOfRangeException()
	{
	}

	public OutOfRangeException( String s )
	{
		super( s );
	}
}
