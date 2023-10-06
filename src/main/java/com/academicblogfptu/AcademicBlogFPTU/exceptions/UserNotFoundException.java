package com.academicblogfptu.AcademicBlogFPTU.exceptions;

public class UserNotFoundException extends RuntimeException{

    //Ưu tiên jj đó
    private static final long serialVerisionUID = 1;
    public UserNotFoundException(String message){
        super(message);
    }
}
