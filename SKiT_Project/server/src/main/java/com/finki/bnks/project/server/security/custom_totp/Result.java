package com.finki.bnks.project.server.security.custom_totp;

public class Result {
    private final boolean valid;
    private final long shift;

    public Result(boolean valid, long shift){
        this.valid = valid;
        this.shift = shift;
    }

    public boolean isValid(){
        return this.valid;
    }

    public long getShift(){
        return this.shift;
    }
}
