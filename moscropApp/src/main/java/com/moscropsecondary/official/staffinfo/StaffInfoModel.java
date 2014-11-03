package com.moscropsecondary.official.staffinfo;

/**
 * Created by ivon on 9/28/14.
 */
public class StaffInfoModel {

    private final String name;
    private final String email;
    private final String[] subjects;
    private final int[] rooms;
    private final String site;
    private final boolean isDepartmentHead;
    private final int teacherID;
    private boolean bookmarked;

    public StaffInfoModel(String name, String email, String[] subjects,
            int[] rooms, String site, boolean isDepartmentHead, int teacherID) {    // TODO add bookmark when that feature is implemented

        this.name = name;
        this.email = email;
        this.subjects = subjects;
        this.rooms = rooms;
        this.site = site;
        this.isDepartmentHead = isDepartmentHead;
        this.teacherID = teacherID;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String[] getSubjects() {
        return subjects;
    }

    public int[] getRooms() {
        return rooms;
    }

    public String getSite() {
        return site;
    }

    public boolean isDepartmentHead() {
        return isDepartmentHead;
    }

    public int getTeacherID() {
        return teacherID;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean b) {
        bookmarked = b;
    }
}
