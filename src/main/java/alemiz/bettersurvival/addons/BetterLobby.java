package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.Command;
import alemiz.bettersurvival.utils.LevelDecoration;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.block.ItemFrameDropItemEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.FloatingTextParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.network.protocol.SetLocalPlayerAsInitializedPacket;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.DummyBossBar;

import java.util.*;

public class BetterLobby extends Addon {

    private String joinMessage = "";
    private String quitMessage = "";
    private int broadcastInterval = 1200;
    private List<String> broadcastMessages = new ArrayList<>();

    private int nextMessage = 0;

    private Map<String, Long> bossBars = new HashMap<>();
    private List<FloatingTextParticle> particles = new ArrayList<>();

    private boolean protectSpawn = true;

    private FloatingTextParticle rulesParticle = null;

    public BetterLobby(String path){
        super("betterlobby", path);
    }

    @Override
    public void postLoad() {
        this.broadcastMessages = configFile.getStringList("broadcast");
        this.broadcastInterval = configFile.getInt("broadcastInterval");
        this.joinMessage = configFile.getString("joinMessage");
        this.quitMessage = configFile.getString("quitMessage");

        this.protectSpawn = configFile.getBoolean("safeSpawn", true);

        this.particles = createHelpParticles();
        this.loadBroadcaster();

        if (configFile.getBoolean("enableRules")){
            this.rulesParticle = this.createRulesParticles();
        }

        String motd = configFile.getString("motd");
        if (!motd.equals("")){
            plugin.getServer().getNetwork().setName(motd);
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);
            configFile.set("motd", "TuiLaKhanh Survival");

            configFile.set("broadcast", Arrays.asList("§eDid you find hacker? Use §b/report§e to report him!", "§eDo people actually read these?", "§aCheck out our youtube channel §cCubeMC Official§a!", "§bVote for us and get §eSubscriber §brank!", "§2Tips for commands can be found on §a/spawn§2!"));
            configFile.set("broadcastInterval", 1800);
            configFile.set("joinMessage", "§8[§6+§8] {player}");
            configFile.set("quitMessage", "§8[§4-§8] {player}");

            configFile.set("bossBar", false);
            configFile.set("bossBarText", "TuiLaKhanh §cSurvival");
            configFile.set("bossBarSize", 100);

            configFile.set("helpParticlePos", new ArrayList<>());
            configFile.set("helpParticleMaxLines", 10);
            configFile.set("helpParticleTitle", "§d<-- §5Available Commands §d-->");
            configFile.set("helpParticleIncludedCommands", Arrays.asList("§7/kill : Kill yourself", "§7/lobby : Go back to server lobby", "§7/spawn : Go to spawn"));

            configFile.set("enableRules", true);
            configFile.set("rulesPos", "0,0,0");
            configFile.set("rulesTitle", "§d<-- §5Game Rules §d-->");
            configFile.set("rulesText", new ArrayList<>());

            configFile.set("safeSpawn", true);
            configFile.save();
        }
    }
    public void loadBroadcaster(){
        Task task = new Task() {
            @Override
            public void onRun(int i) {
                String message = broadcastMessages.get(nextMessage);

                for (Player player : Server.getInstance().getOnlinePlayers().values()){
                    player.sendMessage(message.replace("{player}", player.getName()));
                }

                if (nextMessage >= (broadcastMessages.size() - 1)){
                    nextMessage = 0;
                }else nextMessage++;
            }
        };
        plugin.getServer().getScheduler().scheduleRepeatingTask(task, broadcastInterval);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        AddEntityPacket packet = new AddEntityPacket();
        packet.type = 93;
        packet.entityRuntimeId = Entity.entityCount++;
        packet.yaw = (float) player.getYaw();
        packet.pitch = (float) player.getPitch();
        packet.x = (float) player.getX();
        packet.y = (float) player.getY();
        packet.z = (float) player.getZ();

        for (Player pplayer : player.getLevel().getPlayers().values()){
            pplayer.dataPacket(packet);
        }

        String message = joinMessage.replace("{player}", player.getName());
        event.setJoinMessage(message);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        LevelDecoration[] decorations = {
                new LevelDecoration(player.clone(), LevelEventPacket.EVENT_SOUND_ENDERMAN_TELEPORT),
                new LevelDecoration(player.clone(), LevelEventPacket.EVENT_PARTICLE_ENDERMAN_TELEPORT)
        };
        LevelDecoration.sendDecoration(decorations, player.getLevel().getPlayers());


        if (configFile.getBoolean("bossBar")){
            this.bossBars.remove(player.getName());
        }

        String message = quitMessage.replace("{player}", player.getName());
        event.setQuitMessage(message);
    }

    @EventHandler
    public void onInitialize(DataPacketReceiveEvent event){
        if (!(event.getPacket() instanceof SetLocalPlayerAsInitializedPacket)) return;
        Player player = event.getPlayer();

        if (configFile.getBoolean("bossBar")){
            player.createBossBar(buildBossBar(player));
        }

        this.sendParticles(player);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event){
        if (this.protectSpawn){
            Position explodePos = event.getEntity().getPosition();
            if (!isSafeSpawn(explodePos)) return;

            event.setBlockList(new ArrayList<Block>());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event){
        if (!(event.getEntity() instanceof Player)) return;

        if (this.protectSpawn){
            Player player = (Player) event.getEntity();
            if (!isSafeSpawn(player.clone())) return;

            event.setCancelled();
        }
    }

    @EventHandler
    public void onItemFrame(ItemFrameDropItemEvent event){
        if (!this.protectSpawn || event.getPlayer().isOp() || !isSafeSpawn(event.getItemFrame())) return;
        event.setCancelled();
    }

    public boolean isSafeSpawn(Position position){
        if (position.level != plugin.getServer().getDefaultLevel()) return false;
        return plugin.getServer().getDefaultLevel().isInSpawnRadius(position);
    }

    public DummyBossBar buildBossBar(Player player){
        if (!configFile.getBoolean("bossBar")){
            return new DummyBossBar.Builder(player).build();
        }

        DummyBossBar.Builder builder = new DummyBossBar.Builder(player);
        builder.text(configFile.getString("bossBarText"));
        builder.length(configFile.getInt("bossBarSize"));
        builder.color(BlockColor.RED_BLOCK_COLOR);

        DummyBossBar bossBar = builder.build();
        this.bossBars.put(player.getName(), bossBar.getBossBarId());
        return bossBar;
    }

    public void setHelpParticlesCoords(List<String> positions){
        this.configFile.set("helpParticlePos", positions);
        this.configFile.save();
    }

    public List<String> generateHelpParticleTexts(){
        List<String> commands = new ArrayList<>(this.configFile.getStringList("helpParticleIncludedCommands"));

        for (Addon addon : Addon.getAddons().values()){
            for (Command command : addon.getCommands().values()){
                if (command.ignoreInHelpTexts) continue;
                commands.addAll(Arrays.asList(command.usage.split("\n")));
            }
        }
        return commands;
    }

    public List<FloatingTextParticle> createHelpParticles(){
        List<Vector3> positions = new ArrayList<>();
        for (String pos : this.configFile.getStringList("helpParticlePos")){
            String[] data = pos.split(",");
            positions.add(new Vector3(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2])));
        }

        List<String> helptexts = this.generateHelpParticleTexts();
        int maxLines = this.configFile.getInt("helpParticleMaxLines", 10);
        String title = this.configFile.getString("helpParticleTitle");

        List<FloatingTextParticle> particles = new ArrayList<FloatingTextParticle>();
        int lastLine = 0;

        for (Vector3 pos : positions){
            FloatingTextParticle particle = new FloatingTextParticle(pos, title);

            List<String> particleText = new ArrayList<>();
            for (int i = ((lastLine == 0)?0 : lastLine+1); i <= (lastLine+maxLines); i++){
                if (i >= helptexts.size()) continue;

                particleText.add(helptexts.get(i));
            }
            lastLine = lastLine+10;

            particle.setText(String.join("\n", particleText));
            particles.add(particle);
        }
        return particles;
    }

    public FloatingTextParticle createRulesParticles(){
        String[] pos = this.configFile.getString("rulesPos").split(",");
        if (pos.length <= 1) return null;

        List<String> rules = this.configFile.getStringList("rulesText");
        if (rules.isEmpty()) return null;

        String title = this.configFile.getString("rulesTitle");
        Vector3 position = new Vector3(Double.parseDouble(pos[0]), Double.parseDouble(pos[1]), Double.parseDouble(pos[2]));

        StringBuilder rulesText = new StringBuilder();
        for (String rule : rules){
            rulesText.append("§7- ").append(rule).append("\n");
        }

        return new FloatingTextParticle(position, title, rulesText.toString());
    }

    public void sendParticles(Player player){
        if (player == null) return;

        for (FloatingTextParticle particle : this.particles){
            player.getLevel().addParticle(particle, player);
        }

        if (this.rulesParticle != null){
            player.getLevel().addParticle(this.rulesParticle, player);
        }
    }


    public Map<String, Long> getBossBars() {
        return bossBars;
    }
}
