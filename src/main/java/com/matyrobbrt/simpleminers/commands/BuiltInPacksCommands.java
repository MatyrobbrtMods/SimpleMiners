package com.matyrobbrt.simpleminers.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.matyrobbrt.simpleminers.util.pack.BuiltInPacksRepository;
import com.matyrobbrt.simpleminers.util.pack.SimpleMinersRepositorySource;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static net.minecraft.commands.Commands.LEVEL_OWNERS;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BuiltInPacksCommands {
    public static void register(LiteralArgumentBuilder<CommandSourceStack> dispatcher) {
        dispatcher.then(literal("builtinPacks")
                .requires(it -> it.hasPermission(LEVEL_OWNERS))
                .then(literal("list")
                        .executes(BuiltInPacksCommands::onList))
                .then(literal("extract")
                        .then(argument("name", StringArgumentType.word())
                                .executes(ctx -> onExtract(ctx, false))
                                .then(argument("overwrite", BoolArgumentType.bool())
                                        .executes(ctx -> onExtract(ctx, BoolArgumentType.getBool(ctx, "overwrite"))))))
                .then(literal("enable")
                        .then(argument("name", StringArgumentType.word())
                                .executes(BuiltInPacksCommands::onEnable)))
                .then(literal("disable")
                        .then(argument("name", StringArgumentType.word())
                                .executes(BuiltInPacksCommands::onDisable))));
    }

    private static int onList(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final List<Component> messages = new ArrayList<>();

        try {
            messages.add(Component.literal("Built-in packs:"));
            BuiltInPacksRepository.instance.list().forEach(pack -> {
                final var name = FilenameUtils.removeExtension(pack.name());
                MutableComponent component = Component.literal(name)
                        .withStyle(ChatFormatting.GOLD)
                        .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, name)));
                if (pack.isDisabled()) {
                    component = component.append(" (disabled)");
                } else if (pack.isOverridden()) {
                    component = component.append(" (overridden)");
                }

                if (new File(SimpleMinersRepositorySource.INSTANCE.directory, name).exists()) {
                    component = component.append(" - ")
                            .append(Component.literal("overwrite")
                                    .withStyle(ChatFormatting.RED)
                                    .withStyle(style -> style.withClickEvent(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/simpleminers builtinPacks extract " + name + " true")
                                    )));
                } else {
                    component = component.append(" - ")
                            .append(Component.literal("extract")
                                    .withStyle(ChatFormatting.AQUA)
                                    .withStyle(style -> style.withClickEvent(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/simpleminers builtinPacks extract " + name)
                                    )));
                }

                if (pack.isDisabled()) {
                    component = component.append(" - ")
                            .append(Component.literal("enable")
                                    .withStyle(ChatFormatting.GREEN)
                                    .withStyle(style -> style.withClickEvent(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/simpleminers builtinPacks enable " + name)
                                    )));
                } else {
                    component = component.append(" - ")
                            .append(Component.literal("disable")
                                    .withStyle(ChatFormatting.GREEN)
                                    .withStyle(style -> style.withClickEvent(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/simpleminers builtinPacks disable " + name)
                                    )));
                }

                messages.add(component);
            });
        } catch (IOException e) {
            context.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Encountered exception executing command: ")
                    .append(Component.literal(e.toString()).withStyle(ChatFormatting.RED)));
            return Command.SINGLE_SUCCESS;
        }

        for (final var message : messages) {
            context.getSource().getPlayerOrException().sendSystemMessage(message, false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int onExtract(CommandContext<CommandSourceStack> context, boolean overWrite) throws CommandSyntaxException {
        final String packName = StringArgumentType.getString(context, "name");
        final Path packPath = BuiltInPacksRepository.instance.getPack(packName);

        try {
            if (!Files.exists(packPath)) {
                context.getSource().sendFailure(Component.literal("Unknown builtIn pack: " + packPath));
                return Command.SINGLE_SUCCESS;
            }
            final var targetPackPath = SimpleMinersRepositorySource.INSTANCE.directory.toPath()
                    .resolve(packName);

            if (Files.exists(targetPackPath) && !overWrite) {
                context.getSource().getPlayerOrException().sendSystemMessage(Component.literal("Pack had been extracted already. Would you like to ")
                        .append(Component.literal("overwrite")
                                .withStyle(ChatFormatting.AQUA)
                                .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/simpleminers builtinPacks extract " + packName + " true"
                                ))))
                        .append("?"));
                return Command.SINGLE_SUCCESS;
            }

            FileUtils.deleteDirectory(targetPackPath.toFile());
            Files.createDirectories(targetPackPath);

            try (final ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(packPath))) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if (entry.isDirectory()) continue;

                    final var target = targetPackPath.resolve(entry.getName());
                    Files.createDirectories(target.getParent());
                    Files.write(target, zipInputStream.readAllBytes());
                }
            }
            context.getSource().sendSuccess(Component.literal("Successfully extracted pack!"), true);
        } catch (IOException e) {
            context.getSource().sendFailure(
                    Component.literal("Encountered exception executing command: ")
                            .append(Component.literal(e.toString()).withStyle(ChatFormatting.RED))
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int onDisable(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final String packName = StringArgumentType.getString(context, "name");

        try {
            final var json = readConfig();
            final var disabledMods = StreamSupport.stream(
                    GsonHelper.getAsJsonArray(json, "disabled", new JsonArray()).spliterator(), false
            ).map(JsonElement::getAsString).collect(Collectors.toCollection(ArrayList::new));
            if (disabledMods.contains(packName)) {
                context.getSource().sendFailure(Component.literal("Pack is already disabled!"));
                return Command.SINGLE_SUCCESS;
            }

            disabledMods.add(packName);
            final var jsonArray = new JsonArray();
            disabledMods.forEach(jsonArray::add);
            json.add("disabled", jsonArray);

            writeConfig(json);
            context.getSource().sendSuccess(Component.literal("Pack disabled successfully! (Effects will only be visible after a restart)"), false);
        } catch (IOException e) {
            context.getSource().sendFailure(
                    Component.literal("Encountered exception executing command: ")
                            .append(Component.literal(e.toString()).withStyle(ChatFormatting.RED))
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int onEnable(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final String packName = StringArgumentType.getString(context, "name");

        try {
            final var json = readConfig();
            final var disabledMods = StreamSupport.stream(
                    GsonHelper.getAsJsonArray(json, "disabled", new JsonArray()).spliterator(), false
            ).map(JsonElement::getAsString).collect(Collectors.toCollection(ArrayList::new));
            if (!disabledMods.contains(packName)) {
                context.getSource().sendFailure(Component.literal("Pack is already enabled!"));
                return Command.SINGLE_SUCCESS;
            }

            disabledMods.remove(packName);
            final var jsonArray = new JsonArray();
            disabledMods.forEach(jsonArray::add);
            json.add("disabled", jsonArray);

            writeConfig(json);
            context.getSource().sendSuccess(Component.literal("Pack enabled successfully! (Effects will only be visible after a restart)"), false);
        } catch (IOException e) {
            context.getSource().sendFailure(
                    Component.literal("Encountered exception executing command: ")
                            .append(Component.literal(e.toString()).withStyle(ChatFormatting.RED))
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static JsonObject readConfig() throws IOException {
        try (final var reader = Files.newBufferedReader(SimpleMinersRepositorySource.INSTANCE.directory.toPath().resolve("builtins.json"))) {
            return BuiltInPacksRepository.GSON.fromJson(reader, JsonObject.class);
        }
    }

    private static void writeConfig(JsonObject jsonObject) throws IOException {
        try (final var writer = Files.newBufferedWriter(SimpleMinersRepositorySource.INSTANCE.directory.toPath().resolve("builtins.json"))) {
            BuiltInPacksRepository.GSON.toJson(jsonObject, writer);
        }
    }
}
