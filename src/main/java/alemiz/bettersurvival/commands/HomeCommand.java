package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.Home;
import cn.nukkit.Player;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParameter;


public class HomeCommand extends Command {

    public Home loader;

    public HomeCommand(String name, Home loader) {
        super(name, "Dịch chuyển đến home", "");
        this.commandParameters.clear();

        this.usage = "§7/home <home - optional> : Dịch chuyển đến home";
        this.setUsage(getUsageMessage());

        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("home", true)
        });

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

        Player player = (Player) sender;

        if (args.length < 1){
            loader.teleportToHome(player, "default");
            return true;
        }

        loader.teleportToHome(player, args[0]);
        return true;
    }
}
