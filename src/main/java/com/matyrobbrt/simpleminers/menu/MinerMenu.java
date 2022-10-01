package com.matyrobbrt.simpleminers.menu;

import com.matyrobbrt.simplegui.inventory.SimpleMenu;
import com.matyrobbrt.simpleminers.Registration;
import com.matyrobbrt.simpleminers.miner.MinerBE;
import com.matyrobbrt.simpleminers.network.SimpleMinersNetwork;
import com.matyrobbrt.simpleminers.network.SyncUpgradesPacket;
import com.matyrobbrt.simpleminers.util.cap.SlotItemHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MinerMenu extends SimpleMenu {
    public final MinerBE be;

    private final UpgradesDataSlot upgradesDataSlot;

    public MinerMenu(MinerBE be, int pContainerId, Inventory inv) {
        super(Registration.MINER_MENU.get(), pContainerId, inv);
        this.be = be;
        this.upgradesDataSlot = new UpgradesDataSlot(be.upgrades);

        addPlayerInventory(inv);

        addSlots(be.itemHandler);
        addSlots(be.catalysts);

        addSlot(be.upgradesIn.createContainerSlot());
        addSlot(be.upgradesOut.createContainerSlot());

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return be.progress;
            }

            @Override
            public void set(int i) {
                be.progress = i;
            }
        });
        setupEnergyData();
    }

    protected void addSlots(SlotItemHandler slotItemHandler) {
        //noinspection ConstantConditions
        slotItemHandler.slots().forEach(slot -> addSlot(slot.createContainerSlot()));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private void setupEnergyData() {
        if (be.energy == null) return;

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return be.energy.getMaxEnergyStored() & 0xffff;
            }

            @Override
            public void set(int value) {
                final var cap = be.energy.getMaxEnergyStored() & 0xffff0000;
                be.energy.setCapacity(cap + (value & 0xffff));
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (be.energy.getMaxEnergyStored() >> 16) & 0xffff;
            }

            @Override
            public void set(int value) {
                final int capacity = be.energy.getMaxEnergyStored() & 0x0000ffff;
                be.energy.setCapacity(capacity | (value << 16));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return be.energy.getEnergyStored() & 0xffff;
            }

            @Override
            public void set(int value) {
                final int energyStored = be.energy.getEnergyStored() & 0xffff0000;
                be.energy.setAmount(energyStored + (value & 0xffff));
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (be.energy.getEnergyStored() >> 16) & 0xffff;
            }

            @Override
            public void set(int value) {
                final int energyStored = be.energy.getEnergyStored() & 0x0000ffff;
                be.energy.setAmount(energyStored | (value << 16));
            }
        });

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return be.energy.maxReceive & 0xffff;
            }

            @Override
            public void set(int value) {
                final int energyStored = be.energy.maxReceive & 0xffff0000;
                be.energy.maxReceive = (energyStored + (value & 0xffff));
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (be.energy.maxReceive >> 16) & 0xffff;
            }

            @Override
            public void set(int value) {
                final int energyStored = be.energy.maxReceive & 0x0000ffff;
                be.energy.maxReceive = (energyStored | (value << 16));
            }
        });
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (upgradesDataSlot.checkAndClearUpdateFlag()) {
            SimpleMinersNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) inv.player), new SyncUpgradesPacket(
                    containerId, be.upgrades.getUpgrades()
            ));
        }
    }
}
