package com.matyrobbrt.simpleminers.miner;

import com.matyrobbrt.simplegui.inventory.slot.InventorySlot;
import com.matyrobbrt.simpleminers.menu.CatalystVirtualSlot;
import com.matyrobbrt.simpleminers.miner.upgrade.UpgradeHolder;
import com.matyrobbrt.simpleminers.util.NBTGroup;
import com.matyrobbrt.simpleminers.util.Translations;
import com.matyrobbrt.simpleminers.util.Utils;
import com.matyrobbrt.simpleminers.util.cap.SlotItemHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("NullableProblems")
@ParametersAreNonnullByDefault
public class MinerBlock extends BaseEntityBlock {
    @SuppressWarnings("rawtypes")
    public static final BlockEntityTicker TICKER = (level, blockPos, blockState, be) -> ((MinerBE) be).serverTick();

    private final MinerType minerType;
    private final Supplier<BlockEntityType<?>> beType;
    public MinerBlock(Properties pProperties, MinerType minerType, Supplier<BlockEntityType<?>> beType) {
        super(pProperties);
        this.minerType = minerType;
        this.beType = beType;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MinerBE(beType.get(), blockPos, blockState, minerType);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            NetworkHooks.openScreen((ServerPlayer) pPlayer, getMenuProvider(pState, pLevel, pPos), pPos);
            return InteractionResult.CONSUME;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide() ? null : TICKER;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);

        if (!pStack.hasTag() || pStack.getTag() == null) return;
        final CompoundTag tag = pStack.getTag().getCompound("beData");
        if (tag.contains("energy", Tag.TAG_INT)) {
            pTooltip.add(Translations.ITEM_STORED_ENERGY.get(Component.literal(Utils.getCompressedCount(tag.getInt("energy"))).withStyle(ChatFormatting.GOLD)));
        }

        if (tag.contains("upgrades", Tag.TAG_COMPOUND)) {
            final CompoundTag upgradesSubTag = tag.getCompound("upgrades");
            if (upgradesSubTag.contains("data", Tag.TAG_COMPOUND)) {
                final UpgradeHolder holder = UpgradeHolder.make(null);
                holder.deserializeNBT(upgradesSubTag.getCompound("data"));
                if (!holder.getUpgrades().isEmpty()) {
                    pTooltip.add(Translations.ITEM_STORED_UPGRADES.get(
                            Utils.join(Component.literal(", "), holder.getUpgrades().entrySet()
                                            .stream().map(it -> it.getValue() <= 1 ? it.getKey().createStack().getDisplayName() :
                                                    Component.literal(String.valueOf(it.getValue())).withStyle(ChatFormatting.AQUA).append(" x ").append(it.getKey().createStack().getDisplayName()))
                                            .iterator(),
                                    Function.identity())
                    ));
                }
            }
        }

        if (tag.contains("catalysts", Tag.TAG_COMPOUND)) {
            final var lensBuilder = new SlotItemHandler.Builder();
            for (int slotY = 0; slotY < 2; slotY++) {
                for (int slotX = 0; slotX < 4; slotX++) {
                    lensBuilder.add(CatalystVirtualSlot.at(6 + slotX * 18, 12 + 8 + slotY * 18, null));
                }
            }
            final var catalysts = lensBuilder.build();
            catalysts.deserializeNBT(tag.getCompound("catalysts"));

            final List<ItemStack> stacks = catalysts.slots()
                    .stream().map(InventorySlot::getStack)
                    .filter(Predicate.not(ItemStack::isEmpty))
                    .toList();

            if (!stacks.isEmpty()) {
                pTooltip.add(Translations.ITEM_STORED_CATALYSTS.get(
                        Utils.join(Component.literal(", "), stacks.stream()
                                        .map(ItemStack::getDisplayName).iterator(), Function.identity())
                ));
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pLevel.getBlockEntity(pPos) instanceof MinerBE miner && !pLevel.isClientSide) {
            miner.dropContents(miner.itemHandler);
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        if (pBuilder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof MinerBE miner) {
            final CompoundTag beData = saveGroup(miner).serializeNBT();

            List<ItemStack> drops = super.getDrops(pState, pBuilder);
            if (drops.isEmpty()) drops = List.of(asItem().getDefaultInstance());

            drops.stream().filter(it -> it.getItem() == asItem())
                    .findFirst().ifPresent(item -> item.getOrCreateTag().put("beData", beData));
            return drops;
        }
        return super.getDrops(pState, pBuilder);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        if (pLevel.getBlockEntity(pPos) instanceof MinerBE miner && !pLevel.isClientSide) {
            final NBTGroup nbtGroup = saveGroup(miner);
            //noinspection ConstantConditions
            if (pStack.hasTag() && pStack.getTag().contains("beData", Tag.TAG_COMPOUND)) {
                nbtGroup.deserializeNBT(pStack.getOrCreateTag().getCompound("beData"));
            }
        }
    }

    private NBTGroup saveGroup(MinerBE miner) {
        return NBTGroup.make()
                .add("catalysts", miner.catalysts)
                .add("energy", miner.energy)
                .grouped("upgrades", group -> group
                        .add("data", miner.upgrades)
                        .add("inSlot", miner.upgradesIn)
                        .add("outSlot", miner.upgradesOut));
    }
}
