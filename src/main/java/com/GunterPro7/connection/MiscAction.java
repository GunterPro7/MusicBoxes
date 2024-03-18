package com.GunterPro7.connection;

public enum MiscAction {
    AUDIO_CABLE_POST(0),
    AUDIO_CABLE_FETCH(1),
    AUDIO_CABLE_REMOVE(2),

    MUSIC_BOX_INNER_UPDATE(3),
    MUSIC_BOX_GET(4),
    ;

    public final int id;

    MiscAction(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MiscAction valueOf(int id) {
        for (MiscAction action : values()) {
            if (action.id == id) {
                return action;
            }
        }

        return null;
    }
}
