package com.matyrobbrt.simpleminers.recipe;

import com.matyrobbrt.simpleminers.Registration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FortuneUpgradeRecipe extends CustomRecipe {
    private final Predicate<ItemStack> isEnchanted;

    public FortuneUpgradeRecipe(ResourceLocation pId) {
        super(pId);
        this.isEnchanted = stack -> {
            ListTag enchantments = EnchantedBookItem.getEnchantments(stack);

            for (int i = 0; i < enchantments.size(); ++i) {
                CompoundTag enchantmentNbt = enchantments.getCompound(i);

                if (ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchantmentNbt.getString("id"))) == Enchantments.BLOCK_FORTUNE &&
                        enchantmentNbt.getShort("lvl") >= 1) {
                    return true;
                }
            }
            return false;
        };
    }

    @Override
    public boolean matches(CraftingContainer pContainer, Level pLevel) {
        if (pContainer.getItem(1).is(Tags.Items.DUSTS_REDSTONE) && pContainer.getItem(7).is(Tags.Items.DUSTS_REDSTONE)) {
            if (pContainer.getItem(3).is(Items.DIAMOND_PICKAXE) && pContainer.getItem(4).is(Registration.UPGRADE_BASE.get())) {
                final var book = pContainer.getItem(5);
                return book.is(Items.ENCHANTED_BOOK) && isEnchanted.test(book);
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer pContainer) {
        return Registration.FORTUNE_UPGRADE.get().getDefaultInstance();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth == 3 && pHeight == 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.FORTUNE_UPGRADE_RECIPE.get();
    }
}
