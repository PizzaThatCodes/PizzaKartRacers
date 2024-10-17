package me.pizzathatcodes.pizzakartracers.utils;

import me.pizzathatcodes.pizzakartracers.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class worldUtils {
    public static void unloadWorld(World world) {
        if(world != null) {
            for(Player p : world.getPlayers()) {
                p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }
            Bukkit.getServer().unloadWorld(world, false);
            Bukkit.getLogger().info(world.getName() + " unloaded!");
        }
    }

    public static void deleteWorld(File path) {

        if(Main.isDisabling()) {

            if(path.exists()) {
                File files[] = path.listFiles();
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteWorld(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }

            path.delete();

        } else {

            new BukkitRunnable() {
                @Override
                public void run() {
                    if(path.exists()) {
                        File files[] = path.listFiles();
                        for(int i=0; i<files.length; i++) {
                            if(files[i].isDirectory()) {
                                deleteWorld(files[i]);
                            } else {
                                files[i].delete();
                            }
                        }
                    }

                    path.delete();

                }
            }.runTaskTimerAsynchronously(Main.getInstance(), 0, 20L);

        }

//        if(path.exists()) {
//            File files[] = path.listFiles();
//            for(int i=0; i<files.length; i++) {
//                if(files[i].isDirectory()) {
//                    deleteWorld(files[i]);
//                } else {
//                    files[i].delete();
//                }
//            }
//        }
//
//        return(path.delete());
    }

    public static void copyWorld(Path source, Path target, Runnable onComplete) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executor.submit(() -> {
            try {
                Files.walk(source)
                        .forEach(path -> {
                            Path targetPath = target.resolve(source.relativize(path));
                            try {
                                if (Files.isDirectory(path)) {
                                    Files.createDirectories(targetPath);
                                } else {
                                    try (FileChannel sourceChannel = FileChannel.open(path, StandardOpenOption.READ);
                                         FileChannel targetChannel = FileChannel.open(targetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                                        targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                if (onComplete != null) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            onComplete.run();
                        }
                    }.runTask(Main.getInstance());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                executor.shutdown();
            }
        });
    }
}
