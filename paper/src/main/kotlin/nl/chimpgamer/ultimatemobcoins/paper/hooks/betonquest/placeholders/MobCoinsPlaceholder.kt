package nl.chimpgamer.ultimatemobcoins.paper.hooks.betonquest.placeholders

import org.betonquest.betonquest.api.profile.Profile
import org.betonquest.betonquest.api.quest.placeholder.PlayerPlaceholder

class MobCoinsPlaceholder : PlayerPlaceholder {

    override fun getValue(profile: Profile): String {
        return ""
    }
}