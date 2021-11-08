package io.github.cuubecon.bastioncompassmod.events;

import io.github.cuubecon.bastioncompassmod.items.BastionCompassItem;
import io.github.cuubecon.bastioncompassmod.items.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.util.Hand.MAIN_HAND;

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
                    ZombifiedPiglinEntity piglin = (ZombifiedPiglinEntity) target;
                    if(!piglin.getTags().contains("trader") &&  !piglin.getTags().contains("tradeCompass"))
                    {
                        String piglinUUID = piglin.getUniqueID().toString();
                        CompoundNBT compoundnbt = usedItem.hasTag() ? usedItem.getTag().copy() : new CompoundNBT();
                        usedItem.setTag(compoundnbt);
                        compoundnbt.putString("targetUUID", piglinUUID);
                        player.sendMessage(new TranslationTextComponent("dialog.bastioncompassmod.firstMessage"), Util.DUMMY_UUID);

                        piglin.addTag("tradeCompass");

                       // piglin.addPotionEffect(new EffectInstance(Effects.GLOWING,400, 0, true,true));


                    }
                    else if(!piglin.getTags().contains("trader") &&  piglin.getTags().contains("tradeCompass"))
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
                            player.sendMessage(new TranslationTextComponent("dialog.bastioncompassmod.inTrade"), Util.DUMMY_UUID);

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
                            player.sendMessage(new TranslationTextComponent("dialog.bastioncompassmod.needCrossbow"), Util.DUMMY_UUID);
                        }
                    }
                    else if(piglin.getTags().contains("trader"))
                    {
                        player.sendMessage(new TranslationTextComponent("dialog.bastioncompassmod.alreadyTraded"), Util.DUMMY_UUID);
                    }
                }
            }
        }
    }
}
