package com.th.monicadzhaleva.treasurehunt;


/**
 * Created by monicadzhaleva on 09/10/2017.
 */

public class User {
    private int id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String avatar;
    private String clan;
    private int level=1;
    private int experience=0;

    public User()
    {
    }

    public User(int id, String username, String password, String firstName, String lastName, String avatar, int level, int experience)
    {
        this.id=id;
        this.username=username;
        this.password=password;
        this.firstName=firstName;
        this.lastName=lastName;
        this.avatar = avatar;
        this.level=level;
        this.experience=experience;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getLevel() { return level; }

    public void setLevel(int level) { this.level = level; }

    public int getExperience() { return experience; }

    public void setExperience(int experience) { this.experience = experience; }


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getClan() {
        return clan;
    }

    public void setClan(String clan) {
        this.clan = clan;
    }
}
