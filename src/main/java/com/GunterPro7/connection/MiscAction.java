package com.GunterPro7.connection;

public enum MiscAction {
    AUDIO_CABLE_POST(0),
    AUDIO_CABLE_FETCH(1),
    AUDIO_CABLE_NEW(8),
    AUDIO_CABLE_REMOVE(2),
    AUDIO_CABLE_IS_FREE(7),

    MUSIC_BOX_GET(4),
    MUSIC_BOX_PLAY(9),
    MUSIC_BOX_STOP(10),
    MUSIC_BOX_INNER_UPDATE(3),
    MUSIC_BOX_INNER_EXIT(13),

    MUSIC_CONTROLLER_GET(6),
    MUSIC_CONTROLLER_INNER_UPDATE(5),
    MUSIC_CONTROLLER_PLAY(11),
    MUSIC_CONTROLLER_STOP(12),
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
