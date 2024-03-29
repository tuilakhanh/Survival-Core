package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.Troller;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class InvseeCommand extends Command {

    public Troller loader;

    public InvseeCommand(String name, Troller loader) {
        super(name, "Xem khô đồ của người chơi khác", "");

        this.usage = "§7/invsee <player>: Xem khô đồ của người chơi khác";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, true)
        });

        this.ignoreInHelpTexts = true;

        this.setPermission(loader.configFile.getString("permission-invsee"));
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

        if (args.length < 1){
            sender.sendMessage(getUsageMessage());
            return true;
        }

        this.loader.shopPlayerInv((Player) sender, args[0]);
        return true;
    }
}
