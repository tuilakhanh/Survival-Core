package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.Server;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.Player;

public class FlyCommand extends Command {

    public MoreVanilla loader;

    public FlyCommand(String name, MoreVanilla loader) {
        super(name, "Bật/tắt fly", "");

        this.usage = "§7/fly <player - optional> : Bật/tắt fly";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, true)
        });

        this.setPermission(loader.configFile.getString("permission-fly"));
        this.loader = loader;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player)  && args.length < 1){
            sender.sendMessage("§cLệnh này chỉ được sử dụng trong game!");
            return true;
        }

        if (!(sender instanceof Player)){
            if (args.length < 1){
                sender.sendMessage(usage);
                return true;
            }

            Player player = Server.getInstance().getPlayer(args[0]);
            this.loader.fly(player, "console");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 1){
            this.loader.fly(player, player.getName());
            return true;
        }
        return true;
    }
}
