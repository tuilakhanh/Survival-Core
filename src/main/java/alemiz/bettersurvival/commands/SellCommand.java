package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class SellCommand extends Command {

    public SurvivalShop loader;

    public SellCommand(String name, SurvivalShop loader) {
        super(name, "Mở ui sell", "");

        this.usage = "§7/sell : Mở ui sell";
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
            sender.sendMessage("§cLệnh này chỉ được sử dụng trong game!");
            return true;
        }

        this.loader.getSellManager().sendForm((Player) sender);
        return true;
    }
}
