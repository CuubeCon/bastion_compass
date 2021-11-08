package io.github.cuubecon.bastioncompassmod;

import io.github.cuubecon.bastioncompassmod.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BastionCompassMod.MOD_ID)
public class BastionCompassMod
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "bastioncompassmod";

    public BastionCompassMod() {
        // Register the setup method for modloading
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the doClientStuff method for modloading
        modEventBus.addListener(this::doClientStuff);

        ModItems.register(modEventBus);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        makeCompass();
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }



    private void makeCompass()
    {
        ItemModelsProperties.registerProperty(ModItems.BASTION_COMPASS.get(), new ResourceLocation("angle"), new IItemPropertyGetter() {
            private final Angle field_239439_a_ = new Angle();
            private final Angle field_239440_b_ = new Angle();

            public float call(ItemStack p_call_1_, @Nullable ClientWorld p_call_2_, @Nullable LivingEntity p_call_3_) {
                Entity entity = (Entity)(p_call_3_ != null ? p_call_3_ : p_call_1_.getAttachedEntity());
                if (entity == null) {
                    return 0.0F;
                } else {
                    if (p_call_2_ == null && entity.world instanceof ClientWorld) {
                        p_call_2_ = (ClientWorld)entity.world;
                    }
                    CompoundNBT compoundnbt = p_call_1_.getTag();
                    //System.out.println(compoundnbt);
                    BlockPos blockpos = CompassItem.hasLodestone(p_call_1_) ? this.func_239442_a_(p_call_2_, p_call_1_.getOrCreateTag()) : this.func_239444_a_(p_call_2_);
                   // System.out.println("BLOCKPOS: " + blockpos);
                    long i = p_call_2_.getGameTime();
                    if (blockpos != null && !(entity.getPositionVec().squareDistanceTo((double)blockpos.getX() + 0.5D, entity.getPositionVec().getY(), (double)blockpos.getZ() + 0.5D) < (double)1.0E-5F)) {
                        boolean flag = p_call_3_ instanceof PlayerEntity && ((PlayerEntity)p_call_3_).isUser();
                        double d1 = 0.0D;
                        if (flag) {
                            d1 = (double)p_call_3_.rotationYaw;
                        } else if (entity instanceof ItemFrameEntity) {
                            d1 = this.func_239441_a_((ItemFrameEntity)entity);
                        } else if (entity instanceof ItemEntity) {
                            d1 = (double)(180.0F - ((ItemEntity)entity).getItemHover(0.5F) / ((float)Math.PI * 2F) * 360.0F);
                        } else if (p_call_3_ != null) {
                            d1 = (double)p_call_3_.renderYawOffset;
                        }

                        d1 = MathHelper.positiveModulo(d1 / 360.0D, 1.0D);
                        double d2 = this.func_239443_a_(Vector3d.copyCentered(blockpos), entity) / (double)((float)Math.PI * 2F);
                        double d3;
                        if (flag) {
                            if (this.field_239439_a_.func_239448_a_(i)) {
                                this.field_239439_a_.func_239449_a_(i, 0.5D - (d1 - 0.25D));
                            }

                            d3 = d2 + this.field_239439_a_.field_239445_a_;
                        } else {
                            d3 = 0.5D - (d1 - 0.25D - d2);
                        }

                        return MathHelper.positiveModulo((float)d3, 1.0F);
                    } else {
                        if (this.field_239440_b_.func_239448_a_(i)) {
                            this.field_239440_b_.func_239449_a_(i, Math.random());
                        }

                        double d0 = this.field_239440_b_.field_239445_a_ + (double)((float)p_call_1_.hashCode() / 2.14748365E9F);
                        return MathHelper.positiveModulo((float)d0, 1.0F);
                    }
                }
            }

            @Nullable
            private BlockPos func_239444_a_(ClientWorld p_239444_1_) {
                return p_239444_1_.getDimensionType().isNatural() ? p_239444_1_.func_239140_u_() : null;
            }

            @Nullable
            private BlockPos func_239442_a_(World p_239442_1_, CompoundNBT p_239442_2_) {
                boolean flag = p_239442_2_.contains("LodestonePos");
                boolean flag1 = p_239442_2_.contains("LodestoneDimension");
                if (flag && flag1) {
                    Optional<RegistryKey<World>> optional = CompassItem.getLodestoneDimension(p_239442_2_);
                    if (optional.isPresent() && p_239442_1_.getDimensionKey() == optional.get()) {
                       //System.out.println(p_239442_2_.getCompound("LoadstonePos"));
                        return NBTUtil.readBlockPos(p_239442_2_.getCompound("LodestonePos"));
                    }
                }

                return null;
            }

            private double func_239441_a_(ItemFrameEntity p_239441_1_) {
                Direction direction = p_239441_1_.getHorizontalFacing();
                int i = direction.getAxis().isVertical() ? 90 * direction.getAxisDirection().getOffset() : 0;
                return (double)MathHelper.wrapDegrees(180 + direction.getHorizontalIndex() * 90 + p_239441_1_.getRotation() * 45 + i);
            }

            private double func_239443_a_(Vector3d p_239443_1_, Entity p_239443_2_) {
                return Math.atan2(p_239443_1_.getZ() - p_239443_2_.getPosZ(), p_239443_1_.getX() - p_239443_2_.getPosX());
            }
        });
    }
    @OnlyIn(Dist.CLIENT)
    static class Angle {
        private double field_239445_a_;
        private double field_239446_b_;
        private long field_239447_c_;

        private Angle() {
        }

        private boolean func_239448_a_(long p_239448_1_) {
            return this.field_239447_c_ != p_239448_1_;
        }

        private void func_239449_a_(long p_239449_1_, double p_239449_3_) {
            this.field_239447_c_ = p_239449_1_;
            double d0 = p_239449_3_ - this.field_239445_a_;
            d0 = MathHelper.positiveModulo(d0 + 0.5D, 1.0D) - 0.5D;
            this.field_239446_b_ += d0 * 0.1D;
            this.field_239446_b_ *= 0.8D;
            this.field_239445_a_ = MathHelper.positiveModulo(this.field_239445_a_ + this.field_239446_b_, 1.0D);
        }
    }
}
