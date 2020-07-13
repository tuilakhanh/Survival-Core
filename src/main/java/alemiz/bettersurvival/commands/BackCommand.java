package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class BackCommand extends Command {

    public MoreVanilla loader;

    public BackCommand(String name, MoreVanilla loader) {
        super(name, "Dịch chuyển bạn đến điểm đã chết", "");

        this.usage = "§7/back : Dịch chuyển bạn đến điểm đã chết";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();

        this.setPermission(loader.configFile.getString("permission-back"));
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
        this.loader.back((Player) sender);
        return true;
    }
}
