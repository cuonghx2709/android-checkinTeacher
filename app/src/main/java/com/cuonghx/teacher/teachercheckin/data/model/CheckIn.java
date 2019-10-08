package com.cuonghx.teacher.teachercheckin.data.model;

public class CheckIn {
    public String totalStudent;
    public String attendStudent;

    public CheckIn(String totalStudent, String attendStudent) {
        this.totalStudent = totalStudent;
        this.attendStudent = attendStudent;
    }

    public CheckIn() { }
}
