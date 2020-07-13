package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.MoreVanilla;
import cn.nukkit.Player;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.command.CommandSender;

import java.util.List;

public class NearCommand extends Command {

    public MoreVanilla loader;

    public NearCommand(String name, MoreVanilla loader) {
        super(name, "Danh sách nhưng người chơi đang ở gần bạn", "");
        this.commandParameters.clear();

        this.usage = "§7/near <bán kính>: Danh sách người chơi ở gần bạn";
        this.setUsage(getUsageMessage());

        this.setPermission(loader.configFile.getString("permission-near"));
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

        int radius = 8;
        if (args.length >= 1){
            try {
                radius = Integer.parseInt(args[0]);
            }catch (Exception e){
                player.sendMessage("§cVui lòng nhập bán kính là số!");
                return true;
            }
        }

        List<Player> players = this.loader.getNearPlayers(player.clone(), radius);
        players.remove(player);

        String pplayers = "";
        for (Player pplayer : players){
            if (pplayer.getName().equals(player.getName())) continue;
            pplayers += pplayer.getName() + ", ";
        }

        String message = this.loader.configFile.getString("nearMessage");
        message = message.replace("{players}", (pplayers.equals("")? "Not found" : pplayers.substring(0, pplayers.length()-2)));
        player.sendMessage(message);
        return true;
    }
}
