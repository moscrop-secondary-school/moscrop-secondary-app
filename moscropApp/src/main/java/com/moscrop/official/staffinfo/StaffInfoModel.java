package com.moscrop.official.staffinfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by ivon on 04/04/15.
 */
public class StaffInfoModel {

    private String namePrefix;
    private String firstName;
    private String lastName;
    private String[] rooms;     // separator: "; "
    private String department;
    private String email;
    private String[] sites;     // separator: " "

    public StaffInfoModel(String namePrefix, String firstName, String lastName, String[] rooms, String department, String email, String[] sites) {
        this.namePrefix = namePrefix;
        this.firstName = firstName;
        this.lastName = lastName;
        this.rooms = rooms;
        this.department = department;
        this.email = email;
        this.sites = sites;
    }

    public static String[] roomsStringToArray(String rooms) {
        return splitStringBySemicolonSpace(rooms);
    }

    public static String roomsArrayToString(String[] rooms) {
        return formStringWithSemicolonSpaceAsSeparator(rooms);
    }

    public static String[] sitesStringToArray(String sites) {
        return splitStringBySpace(sites);
    }

    public static String sitesArrayToString(String[] sites) {
        return formStringWithSpaceAsSeparator(sites);
    }

    private static String[] splitStringBySemicolonSpace(String s) {
        return cleanArray(s.split("; "));
    }

    private static String[] splitStringBySpace(String s) {
        return cleanArray(s.split(" "));
    }

    private static String[] cleanArray(String[] a) {
        List<String> list = new ArrayList<String>(Arrays.asList(a));
        list.removeAll(Collections.singleton(null));
        list.removeAll(Collections.singleton(""));
        return list.toArray(new String[list.size()]);
    }

    private static String formStringWithSemicolonSpaceAsSeparator(String[] a) {
        String s = "";
        for (int i=0; i<a.length; i++) {
            s += a[i];
            if (i < a.length-1) {
                s += "; ";
            }
        }
        return s;
    }

    private static String formStringWithSpaceAsSeparator(String[] a) {
        String s = "";
        for (int i=0; i<a.length; i++) {
            s += a[i];
            if (i < a.length-1) {
                s += " ";
            }
        }
        return s;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return namePrefix + ". " + getFirstName().charAt(0) + ". " + getLastName();
    }

    public String[] getRooms() {
        return rooms;
    }

    public String getDepartment() {
        return department;
    }

    public String getEmail() {
        return email;
    }

    public String[] getSites() {
        return sites;
    }

    @Override
    public int hashCode() {
        return namePrefix.hashCode() + firstName.hashCode() + lastName.hashCode()
                + Arrays.hashCode(rooms) + department.hashCode() + email.hashCode() + Arrays.hashCode(sites);
    }

}
