package com.franzliszt.magicmusic.route.nav.container

sealed class ContainerEvent {
    object ChangePlayStatus:ContainerEvent()
    object Next:ContainerEvent()
    object Prior:ContainerEvent()
    object Logout:ContainerEvent()
    data class UIMode(val isDark:Boolean):ContainerEvent()
}