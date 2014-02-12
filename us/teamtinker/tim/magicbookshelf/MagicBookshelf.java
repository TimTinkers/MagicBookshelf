package us.teamtinker.tim.magicbookshelf;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Tim Clancy
 * @version 2.11.14
 */

//To do:
//Add in the option to use flatfile, have the configuration auto-generate (but publish this with its own config),
//Add in the owner for flatfile as scanning for "o<coordinate>" and then attach any resulting names to the block.
//Add in the block protection features (all tied to the configuration file), add in the ability to choose what item works
//as the tool for withdrawing, add in the ability to configure the amount to withdraw per punch, or to use a command to withdraw,
//or to use a faux-slider GUI to withdraw based upon percent, and then make a video tutorial for all of this.
public class MagicBookshelf extends JavaPlugin implements Listener {

    private ExperienceManager experienceManager;
    static final Logger log = Logger.getLogger("Minecraft");
    private FileConfiguration configuration;
    private String connectionURL;
    private String username;
    private String password;
    private String filePath;
    private boolean usingSQL;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        //myPlugin = Bukkit.getPluginManager().getPlugin(this.getName());
        try {
            configuration = getConfig();
            //Currently unused configuration code.
            //File config = new File("plugins" + File.separator + myPlugin.getName() + File.separator + "config.yml");
            //config.mkdir();
            //saveConfig();
        } catch (Exception exception) {
        }
        if (initializePlugin()) {
            log.info("[MagicBookshelf] Enabled Properly.");
        } else {
            log.info("[MagicBookshelf] Failed to Enable. Error 0x01.");
        }
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onDeposit(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && event.getClickedBlock().getType().equals(Material.BOOKSHELF)) {
            Player player = event.getPlayer();
            this.experienceManager = new ExperienceManager(player);
            if (event.getPlayer().hasPermission("magicbookshelf.store")) {
                int xp = experienceManager.getCurrentExp();
                int x = event.getClickedBlock().getX();
                int y = event.getClickedBlock().getY();
                int z = event.getClickedBlock().getZ();
                String worldName = event.getClickedBlock().getWorld().getName();
                if (xp > 0) {
                    player.sendMessage(ChatColor.GREEN + "Your experience has been stored.");
                    experienceManager.setExp(0);
                    if (usingSQL) {
                        Database magicBookshelves = new Database(connectionURL, username, password);
                        String query = "INSERT INTO `magicbookshelf`.`bookshelves` (`shelf_x`, `shelf_y`, `shelf_z`, `shelf_xp`, `shelf_world`)"
                                + "VALUES ('" + x + "', '" + y + "', '" + z + "', '" + xp + "', '" + worldName + "') "
                                + "ON DUPLICATE KEY UPDATE shelf_xp = shelf_xp + " + xp + ";";
                        magicBookshelves.update(query);
                        magicBookshelves.close();
                        //Currently unused query.
                        //query = "INSERT INTO `magicbookshelf`.`owners` (`owners_shelf_id`, `owners_name`) VALUES ('1', 'tim');";
                        //magicBookshelves.query(query);
                    } else {
                        TextFile shelves = new TextFile(filePath);
                        if (shelves.hasShelfCoordinate(x, y, z, worldName)) {
                            int preXP = shelves.getShelfXP(x, y, z, worldName);
                            if (shelves.remove(x, y, z, worldName) == true) {
                                shelves.insert(x, y, z, worldName, preXP + xp);
                            } else {
                                log.info("Error 0x02.");
                                //Mostly solved by using the System.gc() to actually clear things.
                                //Still crops up occasionally, believe it has to do with speed of writing.
                                //Just in case it does crop up, return the xp and allow the player to try again.
                                experienceManager.setExp(xp);
                            }
                        } else {
                            shelves.insert(x, y, z, worldName, xp);
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.GREEN + "You have no experience to store.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to store.");
            }
        }
    }

    @EventHandler
    public void onWithdraw(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                && event.getClickedBlock().getType().equals(Material.BOOKSHELF)) {
            Player player = event.getPlayer();
            this.experienceManager = new ExperienceManager(player);
            if (player.hasPermission("magicbookshelf.withdraw")) {
                int x = event.getClickedBlock().getX();
                int y = event.getClickedBlock().getY();
                int z = event.getClickedBlock().getZ();
                String worldName = event.getClickedBlock().getWorld().getName();
                if (usingSQL) {
                    try {
                        Database magicBookshelves = new Database(connectionURL, username, password);
                        String query = "SELECT shelf_xp "
                                + "FROM `magicbookshelf`.`bookshelves` "
                                + "WHERE shelf_x = " + x + " AND shelf_y = " + y + " AND shelf_z = " + z + " AND shelf_world = \"" + worldName + "\";";
                        ResultSet resultSet = magicBookshelves.query(query);
                        if (resultSet.last() == true) {
                            int xp = resultSet.getInt(1);
                            if (xp == 0) {
                                magicBookshelves.close();
                                player.sendMessage(ChatColor.LIGHT_PURPLE + "There is no experience here.");
                            } else {
                                player.sendMessage(ChatColor.LIGHT_PURPLE + "Withdrawing your experience.");
                                experienceManager.changeExp(xp);
                                query = "UPDATE `magicbookshelf`.`bookshelves` "
                                        + "SET `shelf_xp`='0' "
                                        + "WHERE shelf_x = " + x + " AND shelf_y = " + y + " "
                                        + "AND shelf_z = " + z + " AND shelf_world = \"" + worldName + "\";";
                                magicBookshelves.update(query);
                                magicBookshelves.close();
                            }
                        }
                    } catch (SQLException ex) {
                    }
                } else {
                    TextFile shelves = new TextFile(filePath);
                    if (!shelves.hasShelfCoordinate(x, y, z, worldName)
                            || shelves.getShelfXP(x, y, z, worldName) == 0) {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "There is no experience here.");
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "Withdrawing your experience.");
                        int xp = shelves.getShelfXP(x, y, z, worldName);
                        experienceManager.changeExp(xp);
                        shelves.remove(x, y, z, worldName);
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to withdraw.");
            }
        }
    }

    private boolean initializePlugin() {
        boolean result = true;
        try {
            if (configuration.getBoolean("USE_SQL")) {
                usingSQL = true;
                if (configuration.getBoolean("USE_FULL_URL")) {
                    connectionURL = "jdbc:mysql://" + configuration.getString("FULL_URL");
                } else {
                    connectionURL = "jdbc:mysql://"
                            + configuration.getString("DATABASE_ADDRESS") + ":"
                            + configuration.getString("DATABASE_PORT") + "/"
                            + configuration.getString("DATABASE_NAME");
                }
                username = configuration.getString("DATABASE_USERNAME");
                password = configuration.getString("DATABASE_USERNAME_PASSWORD");
            } else {
                filePath = configuration.getString("FILEPATH");
            }
        } catch (Exception e) {
            result = false;
        }
        return result;
    }
}