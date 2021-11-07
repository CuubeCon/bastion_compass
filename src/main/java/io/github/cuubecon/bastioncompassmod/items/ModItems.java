package io.github.cuubecon.bastioncompassmod.items;

import io.github.cuubecon.bastioncompassmod.BastionCompassMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BastionCompassMod.MOD_ID);

    public static final RegistryObject<Item> BASTION_COMPASS = ITEMS.register("bastion_compass",
            () -> new BastionCompassItem(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(1).maxDamage(2)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
