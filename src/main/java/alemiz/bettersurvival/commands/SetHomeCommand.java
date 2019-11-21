package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.BetterSurvival;
import alemiz.bettersurvival.addons.Home;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;

import java.util.Set;

public class SetHomeCommand extends Command {

    protected static final String usage = "§6Set Home Command:\n"+
            "§7/sethome <home - optional> : Save your home location";

    public SetHomeCommand(String name) {
        super(name, "Save your home location", usage);
        this.commandParameters.clear();

        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("home", true)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player)){
            sender.sendMessage("§cThis command can be run only in game!");
        }

        Player player = (Player) sender;

        if (args.length < 1){
            Home.setHome(player, "default");
            return true;
        }

        Home.setHome(player, args[0]);
        return true;
    }
}
