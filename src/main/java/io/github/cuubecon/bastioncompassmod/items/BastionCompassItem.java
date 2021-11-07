package io.github.cuubecon.bastioncompassmod.items;

import io.github.cuubecon.bastioncompassmod.BastionCompassMod;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.apache.logging.log4j.core.jmx.Server;
import org.apache.logging.log4j.core.util.UuidUtil;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class BastionCompassItem extends Item implements IVanishable
{
    public int ticks = 0;
    private boolean in_trade = false;
    private PlayerEntity player = null;
    private PiglinEntity traitor = null;
    protected final Random rand = new Random();
    public BastionCompassItem(Properties properties) {
        super(properties);
    }


    public static boolean hasLodestone(ItemStack stack) {
        CompoundNBT compoundnbt = stack.getTag();
        return compoundnbt != null && (compoundnbt.contains("LodestoneDimension") || compoundnbt.contains("LodestonePos"));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return super.initCapabilities(stack, nbt);
    }

    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return hasLodestone(stack) || super.hasEffect(stack);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {

            if(!context.getPlayer().isSneaking() && context.getWorld().getDimensionKey() == World.THE_NETHER && context.getWorld() instanceof ServerWorld)
            {
                System.out.println("SEARCH");
                ServerWorld serverWorld = (ServerWorld) context.getWorld();
                BlockPos target = serverWorld.getStructureLocation(Structure.BASTION_REMNANT,context.getPos(), 100, false);
                if(target != null)
                {
                    BastionCompassMod.LOGGER.debug("FOUND: " + target.toString());
                    ItemStack itemstack = context.getItem();
                    CompoundNBT compoundnbt = itemstack.hasTag() ? itemstack.getTag().copy() : new CompoundNBT();
                    itemstack.setTag(compoundnbt);
                    this.write(context.getWorld().getDimensionKey(), target, compoundnbt);
                    return ActionResultType.func_233537_a_(context.getWorld().isRemote);

                }
            }
            else if(!context.getWorld().isRemote() && context.getPlayer().isSneaking())
            {
                BastionCompassMod.LOGGER.debug("remove Pos ");

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
                    // playerIn.sendStatusMessage(ITextComponent.getTextComponentOrEmpty("Your Text here:"+tick), true);

                    if(tick > 60)
                    {
                        CompoundNBT compoundnbt = stack.hasTag() ? stack.getTag().copy() : new CompoundNBT();
                        stack.setTag(compoundnbt);
                        BlockPos target = serverWorld.getStructureLocation(Structure.BASTION_REMNANT, entityIn.getPosition(), 400, false);
                        if(target != null)
                        {
                            BastionCompassMod.LOGGER.debug("FOUND: " + target.toString());
//          this.world.addEntity(new ExperienceOrbEntity(this.world, this.getPosX(), this.getPosY() + 0.5D, this.getPosZ(), i));
                            this.write(worldIn.getDimensionKey(), target, compoundnbt);

                            stack.getTag().remove("ticks");
                            stack.getTag().remove("inTrade");
                            stack.getTag().remove("stage1ticks");

                            playerIn.sendMessage(new StringTextComponent("<ZombifiedPiglin> Hier dein Compass"), Util.DUMMY_UUID);
                          //  piglinEntity.setNoAI(false);
                            return;
                        }
                        else
                        {
                            playerIn.sendMessage(new StringTextComponent("<ZombifiedPiglin> Ich habe keine Festung gefunden"), Util.DUMMY_UUID);
                        }

                        //piglinEntity.setNoAI(false);
                        BastionCompassMod.LOGGER.debug("END OF METHOD: " + target.toString());
                    }
                    stack.getTag().remove("ticks");
                    stack.getTag().putInt("ticks", tick);
                }


            }

        }
    }
}
