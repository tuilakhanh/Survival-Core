package alemiz.bettersurvival.utils;

import alemiz.bettersurvival.BetterSurvival;
import alemiz.bettersurvival.addons.clans.Clan;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI;

public abstract class Placeholders {

    static {
        PlaceholderAPI api = PlaceholderAPI.getInstance();
        BetterSurvival s = BetterSurvival.getInstance();

        api.visitorSensitivePlaceholder("clanname", s::getClanName);
    }
    private Placeholders() {
    }
}
