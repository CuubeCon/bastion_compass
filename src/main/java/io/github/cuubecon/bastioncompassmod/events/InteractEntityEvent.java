package io.github.cuubecon.bastioncompassmod.events;

import io.github.cuubecon.bastioncompassmod.BastionCompassMod;
import io.github.cuubecon.bastioncompassmod.items.BastionCompassItem;
import io.github.cuubecon.bastioncompassmod.items.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

import static net.minecraft.util.Hand.MAIN_HAND;
import static net.minecraft.util.Hand.OFF_HAND;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
public class InteractEntityEvent
{
    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event)
    {
        PlayerEntity player = event.getPlayer();
        ItemStack usedItem;

        if(event.getHand() == MAIN_HAND)
        {
            usedItem = player.getHeldItemMainhand();
        } else {
            usedItem = player.getHeldItemOffhand();
        }

        if(!player.world.isRemote) {
            if (usedItem.getItem() == ModItems.BASTION_COMPASS.get() && !BastionCompassItem.hasLodestone(usedItem))
            {
                Entity target = event.getTarget();
                ServerWorld serverWorld = (ServerWorld)  event.getWorld();

                if(target instanceof ZombifiedPiglinEntity)
                {
                    BastionCompassMod.LOGGER.debug("RIGHTCLICK WITH BASTION COMPASS");
                    ZombifiedPiglinEntity piglin = (ZombifiedPiglinEntity) target;
                    if(!piglin.getTags().contains("trader") && !piglin.getTags().contains("canTrade") && !piglin.getTags().contains("tradeCompass"))
                    {
                        String piglinUUID = piglin.getUniqueID().toString();
                        CompoundNBT compoundnbt = usedItem.hasTag() ? usedItem.getTag().copy() : new CompoundNBT();
                        usedItem.setTag(compoundnbt);
                        compoundnbt.putString("targetUUID", piglinUUID);
                        player.sendMessage(new StringTextComponent("<ZombifiedPiglin> Findest du den Weg zu meinen ehemaligen Freunden nicht? Wenn du mir einen Crossbow gibst, helfe ich dir"), Util.DUMMY_UUID);

                        //chatnachricht
                        piglin.addTag("tradeCompass");

                       // piglin.addPotionEffect(new EffectInstance(Effects.GLOWING,400, 0, true,true));


                    }
                    else if(!piglin.getTags().contains("trader") && !piglin.getTags().contains("canTrade") && piglin.getTags().contains("tradeCompass"))
                    {
                        if(player.inventory.hasItemStack(new ItemStack(Items.CROSSBOW)))
                        {
                            for(ItemStack stack : player.inventory.mainInventory)
                            {
                                if (stack.getItem() == Items.CROSSBOW)
                                {
                                    stack.shrink(1);
                                    break;
                                }
                            }
                            player.sendStatusMessage(new StringTextComponent("<ZombifiedPiglin> Einen Moment ..."), true);

                            String piglinUUID = piglin.getUniqueID().toString();
                            CompoundNBT compoundnbt = usedItem.hasTag() ? usedItem.getTag().copy() : new CompoundNBT();
                            usedItem.setTag(compoundnbt);
                            compoundnbt.putBoolean("inTrade", true);
                            compoundnbt.putInt("ticks", 0);
                            compoundnbt.putString("targetUUID", piglinUUID);
                            piglin.removeTag("tradeCompass");
                            piglin.addTag("trader");
                            //piglin.setNoAI(true);
                        }
                        else
                        {
                            player.sendStatusMessage(new StringTextComponent("<ZombifiedPiglin> Ich brauche einen Crossbow"), true);

                        }


                    }
                    else if(piglin.getTags().contains("trader"))
                    {
                        player.sendStatusMessage(new StringTextComponent("<ZombifiedPiglin> Ich habe schon getraded"), true);
                    }






                }


            }
        }
    }
}
