package com.matyrobbrt.simpleminers;

import com.matyrobbrt.simpleminers.item.UpgradeItem;
import com.matyrobbrt.simpleminers.menu.MinerMenu;
import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.miner.upgrade.MinerUpgradeType;
import com.matyrobbrt.simpleminers.recipe.FortuneUpgradeRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SimpleMiners.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SimpleMiners.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SimpleMiners.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SimpleMiners.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, SimpleMiners.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SimpleMiners.MOD_ID);

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<MenuType<MinerMenu>> MINER_MENU = MENU_TYPES.register("miner", () -> IForgeMenuType.create((windowId, inv, data) ->
            new MinerMenu(
                    (MinerBE) Minecraft.getInstance().level.getBlockEntity(data.readBlockPos()),
                    windowId, inv
            )));

    public static final RegistryObject<RecipeSerializer<FortuneUpgradeRecipe>> FORTUNE_UPGRADE_RECIPE = RECIPE_SERIALIZERS.register("fortune_upgrade", () -> new SimpleRecipeSerializer<>(FortuneUpgradeRecipe::new));

    private static final Rarity RED_RARITY = Rarity.create("simpleminers:red", it -> it.withColor(0xFF2608));
    private static final Rarity GREEN_RARITY = Rarity.create("simpleminers:green", it -> it.withColor(0x38FF4F));
    private static final Rarity PURPLE_RARITY = Rarity.create("simpleminers:purple", it -> it.withColor(0x891A89));
    private static final Rarity LIGHT_BLUE_RARITY = Rarity.create("simpleminers:light_blue", it -> it.withColor(0x00FFFF));

    public static final RegistryObject<Item> UPGRADE_BASE = ITEMS.register("upgrade_base", () -> new Item(new Item.Properties().tab(SimpleMiners.ITEM_TAB)));
    public static final RegistryObject<Item> CATALYST_BASE = ITEMS.register("catalyst_base", () -> new Item(new Item.Properties().tab(SimpleMiners.ITEM_TAB)));

    public static final RegistryObject<UpgradeItem> SPEED_UPGRADE = ITEMS.register("speed_upgrade", () -> new UpgradeItem(new Item.Properties().tab(SimpleMiners.ITEM_TAB).rarity(RED_RARITY), MinerUpgradeType.SPEED));
    public static final RegistryObject<UpgradeItem> ENERGY_UPGRADE = ITEMS.register("energy_upgrade", () -> new UpgradeItem(new Item.Properties().tab(SimpleMiners.ITEM_TAB).rarity(GREEN_RARITY), MinerUpgradeType.ENERGY));
    public static final RegistryObject<UpgradeItem> PRODUCTION_UPGRADE = ITEMS.register("production_upgrade", () -> new UpgradeItem(new Item.Properties().tab(SimpleMiners.ITEM_TAB).rarity(PURPLE_RARITY), MinerUpgradeType.PRODUCTION));
    public static final RegistryObject<UpgradeItem> FORTUNE_UPGRADE = ITEMS.register("fortune_upgrade", () -> new UpgradeItem(new Item.Properties().tab(SimpleMiners.ITEM_TAB).rarity(LIGHT_BLUE_RARITY), MinerUpgradeType.FORTUNE));
}
