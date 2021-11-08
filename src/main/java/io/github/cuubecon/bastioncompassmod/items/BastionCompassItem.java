package io.github.cuubecon.bastioncompassmod.items;

import io.github.cuubecon.bastioncompassmod.BastionCompassMod;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

public class BastionCompassItem extends Item implements IVanishable
{

    public BastionCompassItem(Properties properties) {
        super(properties);
    }


    public static boolean hasLodestone(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getTag();
        return compoundnbt != null && (compoundnbt.contains("LodestoneDimension") || compoundnbt.contains("LodestonePos"));
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return hasLodestone(stack) || super.hasEffect(stack);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {

            if(!context.getWorld().isRemote() && context.getPlayer().isSneaking())
            {
                if(hasLodestone(context.getItem()))
                {
                    CompoundNBT compoundnbt = context.getItem().getOrCreateTag();
                    compoundnbt.remove("LodestonePos");
                    compoundnbt.remove("LodestoneDimension");
                    compoundnbt.putBoolean("LodestoneTracked", false);
                    context.getItem().damageItem(1, context.getPlayer(), (entity) -> {
                        entity.sendBreakAnimation(EquipmentSlotType.MAINHAND);
                    });
                }
            }
        return super.onItemUse(context);
    }

    private void write(RegistryKey<World> lodestoneDimension, BlockPos pos, CompoundNBT nbt)
    {
        nbt.put("LodestonePos", NBTUtil.writeBlockPos(pos));
        World.CODEC.encodeStart(NBTDynamicOps.INSTANCE, lodestoneDimension).resultOrPartial(BastionCompassMod.LOGGER::error).ifPresent((p_234668_1_) -> {
            nbt.put("LodestoneDimension", p_234668_1_);
        });
        nbt.putBoolean("LodestoneTracked", true);
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

        if(!worldIn.isRemote && entityIn instanceof  PlayerEntity)
        {
            if(stack.hasTag())
            {
                ServerWorld serverWorld = (ServerWorld) worldIn;
              //  UUID piglin = UUID.fromString(stack.getTag().getString("targetUUID"));
               // ZombifiedPiglinEntity piglinEntity = (ZombifiedPiglinEntity) serverWorld.getEntityByUuid(piglin).getEntity();


                PlayerEntity playerIn = (PlayerEntity) entityIn;
                boolean trade = stack.getTag().getBoolean("inTrade");
                int tick = stack.getTag().getInt("ticks");
                if(trade)
                {
                    tick++;

                    if(tick > 60)
                    {
                        CompoundNBT compoundnbt = stack.hasTag() ? stack.getTag().copy() : new CompoundNBT();
                        stack.setTag(compoundnbt);
                        BlockPos target = serverWorld.getStructureLocation(Structure.BASTION_REMNANT, entityIn.getPosition(), 400, false);
                        if(target != null)
                        {
                            BastionCompassMod.LOGGER.debug("FOUND: " + target.toString());
                            worldIn.addEntity(new ExperienceOrbEntity(worldIn, playerIn.getPosX(), playerIn.getPosY() + 0.5D, playerIn.getPosZ(), 20));
                            this.write(worldIn.getDimensionKey(), target, compoundnbt);

                            stack.getTag().remove("ticks");
                            stack.getTag().remove("inTrade");
                            stack.getTag().remove("stage1ticks");

                            playerIn.sendMessage(new TranslationTextComponent("dialog.bastioncompassmod.tradeFinished"), Util.DUMMY_UUID);
                          //  piglinEntity.setNoAI(false);
                            return;
                        }
                        else
                        {
                            playerIn.sendMessage(new StringTextComponent("dialog.bastioncompassmod.notFound"), Util.DUMMY_UUID);
                        }

                        //piglinEntity.setNoAI(false);
                    }
                    stack.getTag().remove("ticks");
                    stack.getTag().putInt("ticks", tick);
                }


            }

        }
    }
}
