package earth2b2t.i18n;

import earth2b2t.i18n.bungeecord.ActionLocation;
import earth2b2t.i18n.bungeecord.CachedLanguageProvider;
import earth2b2t.i18n.bungeecord.ChatLocation;
import earth2b2t.i18n.bungeecord.SubTitleLocation;
import earth2b2t.i18n.bungeecord.TitleLocation;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.WeakHashMap;

public class BungeeCordI18n extends PropertiesI18n {

    /* This has to be WeakHashMap, as memory-leak issues might arise on plugin unloading */
    private static final WeakHashMap<Plugin, BungeeCordI18n> cached = new WeakHashMap<>();
    private static final Collection<Location> LOCATIONS;
    private static final Location DEFAULT_LOCATION = new ChatLocation();

    static {
        LOCATIONS = Collections.unmodifiableCollection(Arrays.asList(
                DEFAULT_LOCATION, new TitleLocation(), new SubTitleLocation(), new ActionLocation()
        ));
    }

    private final Plugin plugin;

    private BungeeCordI18n(Plugin plugin) throws IOException {
        super(plugin.getDataFolder(), plugin.getClass(), LOCATIONS, DEFAULT_LOCATION);
        this.plugin = plugin;
    }

    @Override
    public LanguageProvider newLanguageProvider() {
        return CachedLanguageProvider.create(plugin,
                new RemoteLanguageProviderAdapter(new FileLanguageProvider(plugin.getDataFolder().toPath().resolve("lang/players"))));
    }

    public String plain(CommandSender sender, String key, Object... args) {
        if (sender instanceof ProxiedPlayer) {
            return plain(((ProxiedPlayer) sender).getUniqueId(), key, args);
        } else {
            return plain(key, args);
        }
    }

    public void print(CommandSender sender, String key, Object... args) {
        if (sender instanceof ProxiedPlayer) {
            print(((ProxiedPlayer) sender).getUniqueId(), key, args);
        } else {
            sender.sendMessage(plain(key, args));
        }
    }

    public static BungeeCordI18n get(Class<?> c) {
        File pluginFile;
        try {
            pluginFile = new File(c.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Plugin plugin = ProxyServer.getInstance().getPluginManager().getPlugins().stream()
                .filter(it -> {
                    try {
                        return it.getFile().getCanonicalPath().equals(pluginFile.getCanonicalPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .findAny()
                .orElse(null);

        if (plugin == null) {
            throw new IllegalArgumentException("Provided class is not a part of any plugin: " + c.getCanonicalName());
        }
        return get(plugin);
    }

    public static BungeeCordI18n get(Plugin plugin) {
        BungeeCordI18n i18n = cached.get(plugin);
        if (i18n != null) return i18n;
        try {
            i18n = new BungeeCordI18n(plugin);
            i18n.getLanguageProvider();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        cached.put(plugin, i18n);
        return i18n;
    }

}
