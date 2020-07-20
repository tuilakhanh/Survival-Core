package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class SellHandCommand extends Command {

    public SurvivalShop loader;

    public SellHandCommand(String name, SurvivalShop loader) {
        super(name, "Bán đồ trên tay", "");

        this.usage = "§7/sellhand : Bán tất cả đồ trên tay của bạn";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.loader = loader;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player)){
            sender.sendMessage("§cThis command can be run only in game!");
            return true;
        }

        this.loader.getSellManager().sellHand((Player) sender);
        return true;
    }
}
