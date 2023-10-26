package com.crimes_collection.luna

import lunalib.lunaSettings.LunaSettings

class Settings {
    companion object {
        val trapMod: Boolean
            get() = LunaSettings.getBoolean("auto_continue_inator", "trap_mode") ?: false
    }
}