package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class RandCommand extends Command {

    public MoreVanilla loader;

    public RandCommand(String name, MoreVanilla loader) {
        super(name, "Dịch chuyển tới vị trí ngẫu nhiên", "", new String[]{"randtp", "randomtp"});

        this.usage = "§7/rand : Dịch chuyển tới vị trí ngẫu nhiên";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();

        this.setPermission(loader.configFile.getString("permission-randtp"));
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
        this.loader.randomTp((Player) sender);
        return true;
    }
}
