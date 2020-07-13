package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

public class TradeCommand extends Command {

    public BetterEconomy loader;

    public TradeCommand(String name, BetterEconomy loader) {
        super(name, "Trade với người chơi khác", "");

        this.usage = "§7/trade setup <price> : Thiết lập trade với ItemFrame\n" +
                "§7/trade <on|off> : Bật chế độ trade";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("action", CommandParamType.STRING, false),
                new CommandParameter("value", CommandParamType.STRING, false)
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
            player.sendMessage(this.getUsageMessage());
            return true;
        }

        switch (args[0]){
            case "setup":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    break;
                }

                try {
                    int value = Integer.parseInt(args[1]);
                    this.loader.addTraderCreator(player, value);
                }catch (NumberFormatException e){
                    player.sendMessage("§c»§r§7Vui lòng nhập giá trị là số!");
                    break;
                }
                break;
            case "on":
                this.loader.addTrader(player);
                player.sendMessage("§6»§7Chế độ trade đã được bật!");
                break;
            case "off":
                this.loader.removeTrader(player);
                player.sendMessage("§6»§7Đã tắt chế độ trade!");
                break;
            default:
                player.sendMessage(this.getUsageMessage());
                break;
        }
        return true;
    }
}
