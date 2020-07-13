package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class SellAllCommand extends Command {

    public SurvivalShop loader;

    public SellAllCommand(String name, SurvivalShop loader) {
        super(name, "Bán tất cả đồ trong túi của bạn", "");

        this.usage = "§7/sellall : Bán tất cả đồ trong túi của bạn";
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

        this.loader.getSellManager().sellAll((Player) sender);
        return true;
    }
}
