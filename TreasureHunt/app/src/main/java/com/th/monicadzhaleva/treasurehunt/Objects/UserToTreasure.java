package com.th.monicadzhaleva.treasurehunt;

/**
 * Created by md471 on 08/11/17.
 */

public class UserToTreasure {
    private String treasureName;
    private boolean collected;

    public UserToTreasure()
    {

    }
    public UserToTreasure(boolean collected) {
        this.collected = collected;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }

    public String getTreasureName() {
        return treasureName;
    }

    public void setTreasureName(String treasureName) {
        this.treasureName = treasureName;
    }
}
